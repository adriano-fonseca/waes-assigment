package com.company.app.infra;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.persistence.Entity;

import org.hibernate.criterion.MatchMode;

/**
 * Immutable class that represents the features from a query hql. It is used at {@link AppQuery}
 * 
 * @see AppQuery
 * @author adriano-fonseca
 */
public final class AppQueryVO implements Cloneable {
	
  public static final String ROOT_ALIAS = "ed"; 
  
  public enum JoinType {
    INNER, LEFT, RIGHT;
  }
  
  private final Object entity;
  private final Class<?> clazzEntity;
  private final List<String> joins;
  private final List<String> wheres;
  private final Map<String, Object> parameters;
  private final List<String> projections; 
  private final boolean detachedState; 
  private final boolean newED; /* when was used the constructor should be compatible with the entity's projections provided*/
  private final boolean distinct;
  private final boolean count;
  private final boolean fetchJoinInNotNullProperty;
  private final boolean buildWhereClauseFromObject;
  private final MatchMode matchMode4Str;
  private final ListProperties propList;
  private final JoinType joinType;
  
  private final Builder builder;
  
  public static class Builder {
  	
  	private Object entity;
  	private Class<?> clazzEntity;
  	private List<String> projections; 
    private List<String> joins;
    private List<String> wheres;
    private Map<String, Object> parameters;
    private boolean detachedState;
    private boolean newED;
    private boolean distinct;
    private boolean count;
    private boolean fetchJoinInNotNullProperty = true;
    private boolean buildWhereClauseFromObject = true;
    private MatchMode matchMode4Str = MatchMode.START;
    private ListProperties propList;
    private JoinType joinType = JoinType.LEFT;
   
    public Builder() {
    }

		public <VO> Builder(VO entity) {
    	setEntity(entity);
    }
		
		private <VO> void checkEntiy(VO entity) {
			if (entity == null)
    		throw new IllegalArgumentException("entity is null");
    	if (!entity.getClass().isAnnotationPresent(Entity.class))
    		throw new IllegalArgumentException("@javax.persistence.Entity is not present in entity parameter! (Class is not a JPA Entity)");
		}
    
    public Builder setEntity(Object entity) {
    	checkEntiy(entity);
    	this.entity = entity;
    	this.clazzEntity = entity.getClass();
			return this;
		}
    
    public Builder addJoin(String join) {
    	if (this.joins == null)
    		this.joins = new ArrayList<String>();
    	this.joins.add(join);
    	return this;
    }
    
    public Builder addWhere(String where) {
    	if (this.wheres == null)
    		this.wheres = new ArrayList<String>();
    	this.wheres.add(where);
    	return this;
    }
    
    public Builder addParameter(String key, Object value) {
    	if (this.parameters == null)
    		this.parameters = new HashMap<String, Object>();
    	this.parameters.put(key, value);
    	return this;
    }
    
    public Builder addProjection(String projection) {
    	if (this.projections == null)
    		this.projections = new ArrayList<String>();
    	this.projections.add(projection);
    	return this;
    }
    
    public Builder joins(List<String> joins) {
    	this.joins = joins;
    	return this;
    }
    
    public Builder wheres(List<String> wheres) {
    	this.wheres = wheres;
    	return this;
    }
    
    public Builder parameters(Map<String, Object> parameters) {
    	this.parameters = parameters;
    	return this;
    }
    
    public Builder projections(List<String> projections) {
    	this.projections = projections;
    	return this;
    }
    
    public Builder detachedState(boolean bool) {
    	this.detachedState = bool;
    	return this;
    }
    
    public Builder newED(boolean bool) {
    	this.newED = bool;
    	return this;
    }
    
    public Builder distinct(boolean bool) {
    	this.distinct = bool;
    	return this;
    }
    
    public Builder count(boolean bool) {
    	this.count = bool;
    	return this;
    }
    
    public Builder fetchJoinInNotNullProperty(boolean bool) {
    	this.fetchJoinInNotNullProperty = bool;
    	return this;
    }
    
    public Builder buildWhereClauseFromObject(boolean bool) {
    	this.buildWhereClauseFromObject = bool;
    	return this;
    }

    public Builder matchMode4Str(MatchMode matchMode) {
    	this.matchMode4Str = matchMode;
    	return this;
    }
    
    public Builder propList(ListProperties propList) {
    	this.propList = propList;
    	return this;
    }
    
    public Builder joinType(JoinType joinType) {
    	this.joinType = joinType;
    	return this;
    }
    
    public Builder addOrderBy(String orderBy, boolean isAsc) {
      if (orderBy == null || orderBy.isEmpty()) 
        return this;
      if (this.propList == null)
      	this.propList = new ListProperties();
      if (this.propList.getOrdenacao() == null)
      	this.propList.setOrdenacao(new ArrayList<Ordination>());
      this.propList.getOrdenacao().add(new Ordination(orderBy, isAsc));
      return this;
    }
    
    public AppQueryVO build() {
    	validateBuilderVO();
    	return new AppQueryVO(this);
    }
    
    private void validateBuilderVO() {
    	checkEntiy(this.entity);
    	if (this.newED && (this.projections == null || this.projections.isEmpty()))
    		throw new RuntimeException("Ao usar o operador 'new' uma lista de 'projections' deve ser informada, bem como um construtor compat�vel");
    	if (this.parameters != null && !this.parameters.isEmpty() && (this.wheres == null || this.wheres.isEmpty())) 
    		throw new RuntimeException("Ao informar 'parameters' uma lista 'wheres' deve ser informada tamb�m");
    }
    
  }
  
  private AppQueryVO(Builder builder) {
  	this.builder = builder;
  	this.entity = builder.entity;
  	this.clazzEntity = builder.clazzEntity;
  	this.joins = builder.joins == null ? Collections.<String> emptyList() : builder.joins;
  	this.wheres = builder.wheres == null ? Collections.<String> emptyList() : builder.wheres;
  	this.parameters = builder.parameters == null ? Collections.<String, Object> emptyMap() : builder.parameters;
  	this.projections = builder.projections == null ? Collections.<String> emptyList() : builder.projections;          
  	this.detachedState = builder.detachedState;             
  	this.newED = builder.newED;    
  	this.distinct = builder.distinct;           
  	this.count = builder.count;        
  	this.fetchJoinInNotNullProperty = builder.fetchJoinInNotNullProperty;
  	this.buildWhereClauseFromObject = builder.buildWhereClauseFromObject;
  	this.matchMode4Str = builder.matchMode4Str;
  	this.propList = builder.propList;
  	this.joinType = builder.joinType;
  }

  public Object getEntity() {
  	return this.entity;
  }
  
  public Class<?> getClazzEntity() {
		return clazzEntity;
	}
  
	public List<String> getJoins() {
		return this.joins.isEmpty() ? this.joins : new ArrayList<String>(this.joins);
	}

	public List<String> getWheres() {
		return this.wheres.isEmpty() ? this.wheres : new ArrayList<String>(this.wheres);
	}

	public Map<String, Object> getParameters() {
		return this.parameters.isEmpty() ? this.parameters : new HashMap<String, Object>(this.parameters);
	}

	public List<String> getProjections() {
		return this.projections.isEmpty() ? this.projections : new ArrayList<String>(this.projections);
	}

	public boolean isDetachedState() {
		return detachedState;
	}

	public boolean isNewED() {
		return newED;
	}

	public boolean isDistinct() {
		return distinct;
	}

	public boolean isCount() {
		return count;
	}

	public boolean isFetchJoinInNotNullProperty() {
		return fetchJoinInNotNullProperty;
	}

	public boolean isBuildWhereClauseFromObject() {
		return buildWhereClauseFromObject;
	}

	public MatchMode getMatchMode4Str() {
		return matchMode4Str;
	}

	public ListProperties getPropList() {
		return propList;
	}

	public JoinType getJoinType() {
		return joinType;
	}

	public Builder getBuilder() {
		return builder;
	}
	
	@Override
	protected Object clone() throws CloneNotSupportedException {
		throw new CloneNotSupportedException();
	}
	
	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder(256);
		sb.append("\n ENTITY = " + (entity == null ? "null" : entity.getClass() + " " + entity) + "\n");               
		sb.append(" JOINS = " + joins + "\n");               
		sb.append(" WHERES = " + wheres + "\n");              
		sb.append(" PARAMETERS = " + parameters + "\n");   
		sb.append(" PROJECTIONS = " + projections + "\n");         
		sb.append(" IS DETACHED = " + detachedState + "\n");            
		sb.append(" IS NEW = " + newED + "\n");                    
		sb.append(" IS DISTINCT = " + distinct + "\n");                 
		sb.append(" IS COUNT = " + count + "\n");  
		sb.append(" IS FETCH JOIN IN NOT NUL PROPERTIES = " + fetchJoinInNotNullProperty + "\n");
		sb.append(" IS BUILD WHERE CLAUSE FROM OBJECT = " + buildWhereClauseFromObject + "\n");
		sb.append(" MATCH MODE FOR STRINGS PROPERTIES = " + matchMode4Str + "\n");
		if (propList != null) {
			sb.append(" FIRST RESULT = " + propList.getPrimeiro() + "\n");
			sb.append(" MAX RESULT = " + propList.getQuantidade() + "\n");
			if (propList.getOrdenacao() != null) {
				sb.append(" ORDER BY ");
				for (Ordination o : propList.getOrdenacao())
					sb.append(" " + o.getPropriedade() + " " + (o.isAsc() ? "ASC" : "DESC") + "; ");
				sb.append("\n");
			}
		}
		sb.append(" JOIN TYPE = " + joinType + "\n");
		return sb.toString();
	}

}