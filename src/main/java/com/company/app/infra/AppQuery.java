package com.company.app.infra;

import java.io.Serializable;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.persistence.Column;
import javax.persistence.EntityManager;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.Transient;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.LazyInitializationException;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.StatelessSession;
import org.hibernate.criterion.MatchMode;
import org.hibernate.proxy.HibernateProxy;
import org.hibernate.transform.Transformers;

import com.company.app.model.BaseEntity;

/**
 * 
 * Class Resposible for great some HQL's dynamically
 * @author adriano-fonseca
 *
 * @param <ED>
 */
final class AppQuery<ED extends BaseEntity<? extends Serializable>> {
  
  private final Log logger = LogFactory.getLog(getClass());
  private EntityManager em;
  
  public AppQuery(EntityManager em) {
    this.em = em;
  }
  
  final public List<ED> list(AppQueryVO iseQuery) {
    if (!iseQuery.isCount()) {
      Query query = buildQuery(iseQuery);
      @SuppressWarnings("unchecked")
      List<ED> list = query.list();
      return list;
    }
    return null;    
  }

  final public Number count(AppQueryVO iseQuery) {
    if (iseQuery.isCount()) {
      Query query = buildQuery(iseQuery);
      Object result = query.uniqueResult();
      if (result != null)
        return ((Number) result).longValue();
    }
    return new Long(0);
  }
  
  /**
   * Assembly a query HQL by the Entity ED and by {@link AppQueryVO} provided
   * 
   * @param ed
   * @param iseQuery
   * @return
   */
  private final Query buildQuery(AppQueryVO iseQuery) {
    long iniMontaQuery = System.currentTimeMillis();
    @SuppressWarnings("unchecked")
		ED ed = (ED) iseQuery.getEntity();
    if (HibernateProxy.class.isAssignableFrom(ed.getClass()))
      throw new IllegalArgumentException("ED é uma instância de HibernateProxy(javassist). Objeto LAZY!");
    List<String> joins = iseQuery.getJoins();
    List<String> wheres = iseQuery.getWheres();
    Map<String, Object> params = iseQuery.getParameters();
    List<String> projections = iseQuery.getProjections();
    boolean resultTransform = false;
    
    /*where clauses are assembly from the simple attributes different from null in the entity*/
    Map<String,Map<String,Object>> whereObj = new HashMap<String,Map<String,Object>>();
    /*Joins montados a partir dos atributos não nulos do ed*/
    Map<String,String> joinsObj = new HashMap<String,String>();
    buildWhereClauseAndJoinsFromEd(ed, AppQueryVO.ROOT_ALIAS, whereObj, iseQuery, joinsObj, true);

    StringBuilder hql = new StringBuilder(512);
    
    /*define if the projection will be a 'entity projection or 'count(entity)'*/
    if (projections == null || projections.isEmpty()) {
      if (iseQuery.isCount()) {
        String distinct = iseQuery.isDistinct() ? " distinct " : "";
        projections = Collections.<String> singletonList("count(" + distinct + AppQueryVO.ROOT_ALIAS + ")");
      } else {
        projections = Collections.<String> singletonList(AppQueryVO.ROOT_ALIAS);      
      }      
    }

    /* handle projections of query, since that are note a projection from count or from the entity */
    if(!iseQuery.isCount() && !projections.contains(AppQueryVO.ROOT_ALIAS)) {
      List<String> newProjections = new ArrayList<String>();
      for (String projection : projections) {
        if (!projection.startsWith(AppQueryVO.ROOT_ALIAS + "."))
          projection = AppQueryVO.ROOT_ALIAS + "." + projection;
        String[] projectionPropFirstLevel = projection.split("\\.");
        if (projectionPropFirstLevel.length > 2  && !iseQuery.isNewED())  {/* projetar somente propriedades de 1� n�vel (limita��o do Transformer) */
          if (logger.isDebugEnabled()) logger.warn("N�o � poss�vel projetar propriedades para al�m do primeiro n�vel (Limita��o do Transformer).");
          continue;
        }
        if (!iseQuery.isNewED())
          projection = projectionPropFirstLevel[0] + "." + projectionPropFirstLevel[1] + " as " + projectionPropFirstLevel[1];
        else {
          String keyJoin = projection.substring(0, projection.lastIndexOf("."));
          if (keyJoin.contains(".") && joinsObj.containsKey(keyJoin)) 
            projection = joinsObj.get(keyJoin) + projection.substring(projection.lastIndexOf("."), projection.length());
        }
        newProjections.add(projection);
      }
      if (newProjections.isEmpty()) {
        projections = Collections.<String> singletonList(AppQueryVO.ROOT_ALIAS);
      } else {
        projections = newProjections;        
        if (iseQuery.isNewED()) { /*ao usar new deve ser implementado um construtor compativel com as proje��es no ed informado*/
          projections = Collections.<String> singletonList(" new " + ed.getClass().getName() + "(" + (projections.toString().replace("[", "").replace("]", "")) + ") ");
        } else {
          resultTransform = true; /* ao usar o Transform n�o pode fazer fetch nos joins */
        }
      }
    }
    String distinct = iseQuery.isDistinct() && !iseQuery.isCount()/*distinct no count � feito acima*/ ? " distinct " : "";
    hql.append("select " + distinct + projections.toString().replace("[", "").replace("]", "")); 
    hql.append(" from " + ed.getClass().getName() + " " + AppQueryVO.ROOT_ALIAS + " ");

    /* Join das propriedades, do tipo IseED, n�o nulas do objeto ED */  
    if (!joinsObj.isEmpty()) {
      /* trata os joins e seus alias. Sempre usa o alias atribuido � um join para outro join que contenha aquele caminho e assim sucessivamente
       *  Ex.: Se o join ed.turmaED tem o alias 'alias_0', 
       *        ent�o o join ed.turmaED.estabelecimentoED ficar� alias_0.estabelecimentoED alias_1
       *        onde alias_0 representa o join ed.turmaED.
       * */
      List<String> listJoinFetch = new ArrayList<String>();
      String joinType = iseQuery.getJoinType() == null ? AppQueryVO.JoinType.LEFT.name() : iseQuery.getJoinType().name();
      /* Usa fetch no join se foi solicitado e se n�o usa Transform para proje��es espec�ficas de primeiro n�vel*/
      String fetch = !iseQuery.isCount() && !iseQuery.isNewED() && iseQuery.isFetchJoinInNotNullProperty() && !resultTransform ? " fetch " : " ";
      if (logger.isDebugEnabled()) {
        if (!(!iseQuery.isCount() && !iseQuery.isNewED() && iseQuery.isFetchJoinInNotNullProperty() && !resultTransform)) 
          logger.debug("Joins das propriedades ED's n�o nulas N�O ser�o com fetch, pois a proje��o da query � um 'count', 'new' ou usa 'transformer'!");
      }
      for (String joinFetch : joinsObj.keySet()) {
        String key = joinFetch.substring(0, joinFetch.lastIndexOf("."));
        if (key.contains(".") && joinsObj.containsKey(key))
          listJoinFetch.add(joinType + " join " + fetch  + joinsObj.get(key) + joinFetch.substring(joinFetch.lastIndexOf("."), joinFetch.length()) + " " + joinsObj.get(joinFetch));
        else
          listJoinFetch.add(joinType + " join " + fetch + joinFetch + " " + joinsObj.get(joinFetch));
      }
      
      /* ordena os joins pela ordem de inser��o. Importante para n�o usar um alias antes dele ser declarado na query */
      Collections.sort(listJoinFetch, new Comparator<String>() {
        @Override
        public int compare(String j1, String j2) {
          /* Recupera o alias que fica ao final de cada join e, ent�o, os compara. 
           * O alias segue o padr�o: 'alias_' + n� da ordem de inser��o. Ex.: alias_0, alias_1, alias_2 ... */
          String[] a1 = j1.split(" ");
          String[] a2 = j2.split(" ");
          String alias1 = a1[a1.length - 1];
          String alias2 = a2[a1.length - 1];
          return alias1.compareTo(alias2);
        }
        
      });
      for (String joinFetch : listJoinFetch)
        hql.append(" " + joinFetch);
    }
    
    /* Joins de IseQuery */
    if (joins != null) {
      for (String join : joins) {
        if (iseQuery.isCount() || resultTransform || iseQuery.isNewED()) {
          if (logger.isDebugEnabled()) logger.debug("Removendo o 'fetch' dos joins declarados, pois a proje��o da query � um 'new', 'count' ou usa 'transformer'!");
          join = join.replaceAll(" fetch ", " ");
        }
        boolean addJoin = true;
        /*substitui caminho inicial do join por alias de join j� declarado*/
        for (String joinAlias : join.split(" ")) {
          if (joinAlias.startsWith(AppQueryVO.ROOT_ALIAS + ".")) {
            String keyJoin = joinAlias.substring(0, joinAlias.lastIndexOf("."));          
            if (keyJoin.contains(".") && joinsObj.containsKey(keyJoin)) {
              String newJoinAlias = joinsObj.get(keyJoin) + joinAlias.substring(joinAlias.lastIndexOf("."), joinAlias.length());
              if (!joinsObj.containsKey(newJoinAlias))
                join = join.replace(joinAlias, newJoinAlias);
              else
                addJoin = false;
            }
          }
        }
        if (addJoin)
          hql.append(" " + join + " ");
      }
    } 
    
    /* Come�o da cl�usula where */
    hql.append(" where (1=1) ");
    
    /* Restri��es Where retornada de IseQuery */
    if (wheres != null) { 
      for (String whereClause : wheres) {
        if (iseQuery.isBuildWhereClauseFromObject()) {
          if (whereClause.contains(".")) {
            String key = whereClause.substring(0, whereClause.lastIndexOf("."));
            if (!key.startsWith(AppQueryVO.ROOT_ALIAS + "."))
              key = AppQueryVO.ROOT_ALIAS + "." + key;            
            if (key.contains(".") && joinsObj.containsKey(key)) 
              whereClause = joinsObj.get(key) + whereClause.substring(whereClause.lastIndexOf("."), whereClause.length());
          }
        }
        hql.append(" and " + whereClause);
      }
    }
    
    /* Restri��es Where retornada dos atributos n�o nulos do objeto */
    if (iseQuery.isBuildWhereClauseFromObject()) {
      for (String whereClause : whereObj.keySet()) 
        hql.append(" and " + whereClause);      
    }
    
    /* Order By*/
    ListProperties propList = iseQuery.getPropList();
    List<Ordination> listOrdem = null;
    if (propList != null)
      listOrdem = iseQuery.getPropList().getOrdenacao();
    List<String> orderBy = null;
    if (listOrdem != null && !listOrdem.isEmpty()) {
      orderBy = new ArrayList<String>();
      for (Ordination ordem : listOrdem) {
        if (ordem.isAsc())
          orderBy.add(ordem.getPropriedade() + " asc ");
        else
          orderBy.add(ordem.getPropriedade() + " desc ");
      }
    }
    if (orderBy != null && !orderBy.isEmpty() && !iseQuery.isCount()) {
      if (orderBy != null && !iseQuery.isCount() && !joinsObj.isEmpty()) {
        /* trata a propriedade usada para ordena��o. Pode ser que ela tenha um alias atribuido nos joins */
        List<String> newOrderBy = new ArrayList<String>();
        for (String order : orderBy) {
          if (!order.startsWith(AppQueryVO.ROOT_ALIAS + ".")) {
            if (joins == null || joins.isEmpty()) {
              order = AppQueryVO.ROOT_ALIAS + "." + order;
            } else if (joins != null) {
              for (String j : joins) {
                if (j != null && !j.trim().endsWith(order.substring(0, order.lastIndexOf(".")))) {
                  order = AppQueryVO.ROOT_ALIAS + "." + order;
                  break;
                }
              }
            }
          }
          String keyJoin = order.substring(0, order.lastIndexOf("."));          
          if (keyJoin.contains(".") && joinsObj.containsKey(keyJoin))
            order = joinsObj.get(keyJoin) + order.substring(order.lastIndexOf("."), order.length());
          newOrderBy.add(order);
        }
        orderBy = newOrderBy;
      }
      hql.append(" order by " + orderBy.toString().replace("[", "").replace("]", ""));
      
    } else if (!iseQuery.isCount()) { 
      /* Se nenhum 'order by' for informado ent�o ordena pelo id do ed  */
      for (Field fId : ed.getClass().getDeclaredFields()) {
        if (fId.isAnnotationPresent(Id.class)) {
          hql.append(" order by " + AppQueryVO.ROOT_ALIAS + "." + fId.getName() + " asc ");
          break;
        }
      }
    }
    long fimMontaQuery = System.currentTimeMillis();
    if (logger.isDebugEnabled()) {
      long time = (fimMontaQuery - iniMontaQuery);
      if (time > 50)
        logger.debug("Constru��o da query excedeu 50ms!");
      if (time > 100)
        logger.debug("Constru��o da query excedeu 100ms!");
      logger.debug(">>> IseBD demorou " + time + "ms para montar a query. <<<");
      logger.debug("HQL> " + hql);
    }
    
    /* Criando a query */
    Query query = null;
    Session session = this.em.unwrap(Session.class);
    if (iseQuery.isDetachedState()) {
    	StatelessSession statelessSession = session.getSessionFactory().openStatelessSession();
    	query = statelessSession.createQuery(hql.toString());
    } else {
    	query = session.createQuery(hql.toString());    	
    }

    /* Configurando parametros das restri��es where de IseQuery*/
    if (params != null && !params.isEmpty()) {
      for (String keyParam : params.keySet()) {
        Object paramValue = params.get(keyParam);
        if (paramValue instanceof Collection<?>)
          query.setParameterList(keyParam, (Collection<?>) params.get(keyParam));     
        else if (paramValue instanceof Object[])
          query.setParameterList(keyParam, (Object[]) params.get(keyParam));     
        else
          query.setParameter(keyParam, paramValue);
      }
    } 

    /* Configurando parametros das restri��es where retornada dos atributos n�o nulos do objeto */
    if (iseQuery.isBuildWhereClauseFromObject()) {
      for (String whereClause : whereObj.keySet()) {
        Map<String, Object> keyValueParam = whereObj.get(whereClause);
        for (String keyParam : keyValueParam.keySet()) 
          query.setParameter(keyParam, keyValueParam.get(keyParam));      
      }      
    }
    
    /* Configurando pagina��o */
    if (iseQuery.getPropList() != null && iseQuery.getPropList().getQuantidade() > 0) {
      if (logger.isDebugEnabled()) {
        logger.debug("Query FirstResult: " + iseQuery.getPropList().getPrimeiro());
        logger.debug("Query MaxResults: " + iseQuery.getPropList().getQuantidade());
      }
      query.setFirstResult(iseQuery.getPropList().getPrimeiro());
      query.setMaxResults(iseQuery.getPropList().getQuantidade());      
    }
    
    /*Se tiver proje��es de 1� n�vel ent�o usar Transformer*/
    if(resultTransform)
      query.setResultTransformer(Transformers.aliasToBean(ed.getClass()));
    
    return query;
  }
  
  private void buildWhereClauseAndJoinsFromEd(ED ed, String joinED, Map<String,Map<String,Object>> where, AppQueryVO iseQuery, Map<String,String> joinsObj, boolean fillWhereClause) {
    if (joinED == null) 
      joinED = AppQueryVO.ROOT_ALIAS;
    Object idValueFromED = null;
    boolean error = false;
    try {
      idValueFromED = getIdFromED(ed);
    } catch (LazyInitializationException lie) {
      error = true;
      if (logger.isDebugEnabled()) logger.debug("Objeto nao inicializado. Lazy!");
    } catch (Exception e) {
      error = true;
      if (e.getCause() != null && e.getCause().getClass() == LazyInitializationException.class) {
        if (logger.isDebugEnabled()) logger.debug("Objeto nao inicializado: " + ed.getClass() + ". LazyInitializationException!");
      } else {
        logger.warn(e.getCause(), e);   
      }
    }
    if (!error) {
      boolean whereByIDFilled = false;
      if (fillWhereClause)
        whereByIDFilled = buildIDWhereCondition(ed, idValueFromED, joinED, where, iseQuery, joinsObj);
      if (AppQueryVO.ROOT_ALIAS.equals(joinED) && whereByIDFilled)
        fillWhereClause = false; /*se o id do ed ra�z estiver preenchido ent�o n�o precisa outras clausulas where*/
      for (Field field : ed.getClass().getDeclaredFields()) {
        if (isTransientOrStatic(field))
          continue;
        Object valueParam = null;
        try {
          valueParam = invokeGetMethod(field, ed);
        } catch (Exception e) {
          logger.warn(e);
          continue;
        }
        if (valueParam == null)
          continue;
        /*se o id do ed, do join em quest�o, estiver preenchido, ent�o n�o precisa outras clausulas where pra esse ed */
        /*se o id do ed raiz estiver preenchido ent�o n�o precisa outras clausulas where */
        if (!whereByIDFilled && isSimpleAttribute(field) && fillWhereClause){
          buildWhereCondition(field, valueParam, joinED, where, iseQuery, joinsObj);
        } else if (isIseEDAttribute(field)) {
          String newJoin = joinED + "." + field.getName();
          if (!joinsObj.containsKey(newJoin))
            joinsObj.put(newJoin, "alias_" + StringUtils.leftPad(joinsObj.size()+"", 3, "0"));
          @SuppressWarnings("unchecked")
          ED edSubLevel = (ED) valueParam;
          /* chamada recursiva */
          buildWhereClauseAndJoinsFromEd(edSubLevel, newJoin, where, iseQuery, joinsObj, fillWhereClause);
        }        
      }
    }
  }
 
  private void buildWhereCondition(Field field, Object valueParam, String joinED, Map<String,Map<String,Object>> where, AppQueryVO iseQuery, Map<String,String> joinsObj) {
    String condition = null;
    String keyParam = null;
    if (joinsObj.containsKey(joinED)) {
      condition = joinsObj.get(joinED) + "." + field.getName();
      keyParam = joinsObj.get(joinED) + field.getName();
    } else {
      condition = joinED + "." + field.getName();
      keyParam = joinED.replace(".", "") + field.getName();
    }
    if (valueParam instanceof String) {
      if (((String)valueParam).contains("%"))
        valueParam = ((String)valueParam).replaceAll("%", "");
      if (((String)valueParam).isEmpty())
        return;
      condition = " lower(" + condition + ") like :" + keyParam;
      if (((String) valueParam).length() == 1)
        valueParam = MatchMode.EXACT.toMatchString(((String)valueParam).toLowerCase());            
      else if (iseQuery.getMatchMode4Str() == null)
        valueParam = MatchMode.START.toMatchString(((String)valueParam).toLowerCase());
      else
        valueParam = iseQuery.getMatchMode4Str().toMatchString(((String)valueParam).toLowerCase());
    } else {
      condition += " = :" + keyParam;
    }
    where.put(condition, Collections.<String,Object> singletonMap(keyParam, valueParam));
  }
  
  private boolean buildIDWhereCondition(ED ed, Object idValueFromED, String joinED, Map<String,Map<String,Object>> where, AppQueryVO iseQuery, Map<String,String> joinsObj) {
    if (idValueFromED != null) {
      for (Field fieldID : ed.getClass().getDeclaredFields()) {
        if (fieldID.isAnnotationPresent(Id.class)) {
          buildWhereCondition(fieldID, idValueFromED, joinED, where, iseQuery, joinsObj);
          return true;
        }
      }
    }
    return false;
  }
  
  private static <ED extends BaseEntity<? extends Serializable>> Object invokeGetMethod(Field field, ED ed) 
  throws NoSuchMethodException, SecurityException, IllegalAccessException, IllegalArgumentException, InvocationTargetException {
    String methodName = "get" + field.getName().toUpperCase().charAt(0) + field.getName().substring(1, field.getName().length());
    Method methodGetField1N  = ed.getClass().getDeclaredMethod(methodName);
    return methodGetField1N.invoke(ed);
  }
  
  private static <ED extends BaseEntity<? extends Serializable>> Object getIdFromED(ED ed)
  throws NoSuchMethodException, SecurityException, IllegalAccessException, IllegalArgumentException, InvocationTargetException {
    Method methodGetId  = ed.getClass().getDeclaredMethod("getIdEntity");
    return methodGetId.invoke(ed);
  }
 
  private static boolean isTransientOrStatic(Field field) {
    if (field.isAnnotationPresent(Transient.class) || Modifier.isStatic(field.getModifiers()))
      return true;
    else
      return false;
  }
  
  private static boolean isSimpleAttribute(Field field) {
    List<?> interfacesTypeField = Arrays.asList(field.getType().getInterfaces());
    if (field.isAnnotationPresent(Column.class) 
        && (interfacesTypeField == null || !interfacesTypeField.contains(Collection.class) 
          && (field.getType().getSuperclass() == null || !field.getType().getSuperclass().equals(BaseEntity.class)) ) )
      return true;
    else 
      return false;
  }
  
  private static boolean isIseEDAttribute(Field field) {
    if (field.getType().getSuperclass() != null && field.getType().getSuperclass().equals(BaseEntity.class))
      return true;
    else 
      return false;
  }
  
  @SuppressWarnings("unused")
  private static boolean isOneToManyList(Field field) {
    if (field.isAnnotationPresent(OneToMany.class) && field.getType().equals(List.class)) {
      return true;
    }
    return false;
  }
  
}