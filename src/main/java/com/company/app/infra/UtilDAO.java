package com.company.app.infra;

import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import javax.persistence.CascadeType;
import javax.persistence.Embedded;
import javax.persistence.EntityManager;
import javax.persistence.Id;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.Transient;

import org.apache.log4j.Logger;



public class UtilDAO {
  
  private static final Logger LOGGER = Logger.getLogger(UtilDAO.class);
  
  public static boolean isOpenJpa(EntityManager em) {
    if (em == null) {
      throw new RuntimeException(
          "Entity manager is not available.");
    }
    // nao usar getClass porque o conteudo é proxy
    return em.toString().startsWith("org.apache");
  }
  
  /**
   * Atribui valor null a cada propriedade do objeto managed quando: - a
   * propriedade no objeto detached for null - e a propriedadde no objeto
   * manage não for null
   * 
   * @param detached
   * @param managed
   */
  public static void forcaMergeNull(Object detached, Object managed) {
    Set<Object> setJaAvaliados = new HashSet<Object>();
    forcaMergeNull(detached, managed, setJaAvaliados);
    setJaAvaliados = null;
  }
  public static void forcaMergeNull(Object detached, Object managed, Set<Object> jaAvaliados) {
    try {
      if (jaAvaliados.contains(detached)) {
        // teste realizado para evitar reavaliar relacionamentos recursivos
        return;
      }
      jaAvaliados.add(detached);
      
      PropertyDescriptor pds[];
      pds = Introspector.getBeanInfo(detached.getClass()).getPropertyDescriptors();

      Method mr;
      Method mw;
      for (int p = 0; p < pds.length; p++) {

        mr = pds[p].getReadMethod();
        mw = pds[p].getWriteMethod();
        if (mr == null || mw == null) {
          continue;
        }
        Field f;
        try {
          f = detached.getClass().getDeclaredField(pds[p].getName());
        } catch (NoSuchFieldException e) {
          continue;
        }
        if (f.getAnnotation(Transient.class) != null) {
          continue;
        }
        if (f.getAnnotation(Id.class) != null) {
          continue;
        }
        Object conteudoManaged = mr.invoke(managed, (Object[]) null);
        if (conteudoManaged == null) {
          continue;
        }

        Object conteudoDetached = mr.invoke(detached, (Object[]) null);
        if (conteudoDetached != null) {
          if (!isRelacionamentoComMerge(f)) {
            continue;
          }
          if (conteudoDetached instanceof Collection) {
            Collection<?> colDetached = (Collection<?>) conteudoDetached;
            Collection<?> colManaged = (Collection<?>) conteudoManaged;
            for (Object itemDetached : colDetached) {
              for (Object itemManaged : colManaged) {
                if (itemDetached.equals(itemManaged)) {
                  forcaMergeNull(itemDetached, itemManaged, jaAvaliados);
                  break;
                }
              }
            }
          } else {
            forcaMergeNull(conteudoDetached, conteudoManaged, jaAvaliados);
          }
          continue;
        }
        if (isRelacionamentoParaMuitos(f)) {
          // não força null em propriedades de relacionamento para muitos
          continue;
        }
        Object nada = null;
        LOGGER.debug("Atribuindo null: " + f.getName() + " de " + managed.getClass().getName() + " Objetos avalidos até o momento:" + jaAvaliados.size() );        
        mw.invoke(managed, nada);
      }
    } catch (RuntimeException e) {
      throw e;
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }
  
  private static boolean isRelacionamento(Field f) {
    if (f.getAnnotation(ManyToOne.class) != null) {
      return true;
    }
    if (f.getAnnotation(OneToMany.class) != null) {
      return true;
    }
    if (f.getAnnotation(OneToOne.class) != null) {
      return true;
    }
    if (f.getAnnotation(ManyToMany.class) != null) {
      return true;
    }
    if (f.getAnnotation(Embedded.class) != null) {
      return true;
    }
    return false;
  }
  
  private static boolean isRelacionamentoParaMuitos(Field f) {
    if (f.getAnnotation(OneToMany.class) != null) {
      return true;
    }
    if (f.getAnnotation(ManyToMany.class) != null) {
      return true;
    }
    return false;
  }
  private static boolean isRelacionamentoComMerge(Field f) {
    if (f.getAnnotation(ManyToOne.class) != null) {
      ManyToOne annotation = f.getAnnotation(ManyToOne.class);
      if (annotation == null) {
        return false;
      }
      return isCascadeTypeMerge(annotation.cascade());
    }
    if (f.getAnnotation(OneToMany.class) != null) {
      OneToMany annotation = f.getAnnotation(OneToMany.class);
      if (annotation == null) {
        return false;
      }
      return isCascadeTypeMerge(annotation.cascade());
    }
    if (f.getAnnotation(OneToOne.class) != null) {
      OneToOne annotation = f.getAnnotation(OneToOne.class);
      if (annotation == null) {
        return false;
      }
      return isCascadeTypeMerge(annotation.cascade());
    }
    if (f.getAnnotation(ManyToMany.class) != null) {
      ManyToMany annotation = f.getAnnotation(ManyToMany.class);
      if (annotation == null) {
        return false;
      }
      return isCascadeTypeMerge(annotation.cascade());
    }
    if (f.getAnnotation(Embedded.class) != null) {
      Embedded annotation = f.getAnnotation(Embedded.class);
      if (annotation == null) {
        return false;
      }
      return true;
    }
    return false;
  }

  private static boolean isCascadeTypeMerge(CascadeType[] c) {
    if (c == null) {
      return false;
    }
    for (int i = 0; i < c.length; i++) {
      if (c[i] == CascadeType.ALL) {
        return true;
      }
      if (c[i] == CascadeType.MERGE) {
        return true;
      }
    }
    return false;
  }

}
