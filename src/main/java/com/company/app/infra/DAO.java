package com.company.app.infra;

import java.io.Serializable;
import java.util.List;

import com.company.app.model.BaseEntity;

/**
 * Interface of RN(Bussines Logic)
 * The DAO should extend RNImpl
 * 
 * @author adriano-fonseca
 *
 * @param <ENTITY>
 */
public abstract interface DAO<ENTITY extends BaseEntity<? extends Serializable>> {
  
	
	/**
   * 
   * @param clazz
   * @param id
   * @return
   */
  public abstract <T extends BaseEntity<? extends Serializable>> T findById(Class<T> clazz, Serializable id);
	
  /**
   * Monta uma consulta a partir dor atributos do ENTITY e do objeto iseQueryVO.
   * 
   * @param ed
   * @return objeto
   */
  public abstract ENTITY find(ENTITY ed);

  /**
   * Monta uma consulta a partir dor atributos do ENTITY e do objeto iseQueryVO.
   * 
   * @param ed
   * @param iseQuery
   * @return
   */
  public abstract ENTITY find(AppQueryVO iseQuery);
  
  /**
   * 
   * @param iseQueryBuilder
   * @return
   */
  public abstract ENTITY find(AppQueryVO.Builder iseQueryBuilder);
  
  /**
   * Monta uma consulta a partir dor atributos do ENTITY.
   * 
   * @param ed
   * @return lista de objetos
   */
  public abstract List<ENTITY> list(ENTITY ed);

  /**
   * 
   * @param ed
   * @return
   */
  public abstract List<ENTITY> listDetached(ENTITY ed);

  /**
   * 
   * @param ed
   * @param pl
   * @return
   */
  public abstract List<ENTITY> list(ENTITY ed, ListProperties pl);

  /**
   * Monta uma consulta a partir do ed informado e do objeto iseQueryVO
   * 
   * @param ed
   * @param iseQuery
   * @return lista de objetos
   */
  public abstract List<ENTITY> list(AppQueryVO iseQuery);
 
  /**
   * Monta uma consulta a partir do ed informado e do objeto iseQueryVO
   * 
   * @param ed
   * @param iseQuery
   * @return lista de objetos
   */
  public abstract List<ENTITY> list(AppQueryVO.Builder iseQueryBuilder);
  
  /**
   * Monta uma consulta, a partir do ENTITY informado, para obter uma contagem
   * 
   * @param ed
   * @return count
   */
  public abstract long count(ENTITY ed);

  /**
   * Monta uma consulta a partir do ed informado e do objeto iseQueryVO para obter uma contagem
   * 
   * @param ed
   * @param iseQuery
   * @return count
   */
  public abstract long count(AppQueryVO iseQuery);
  
  /**
   * 
   * @param iseQueryBuilder
   * @return
   */
  public abstract long count(AppQueryVO.Builder iseQueryBuilder);
  
}
