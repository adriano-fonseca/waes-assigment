package com.company.app.infra;

import java.util.List;

import javax.persistence.EntityManager;

import org.hibernate.Criteria;
import org.hibernate.Session;
import org.hibernate.criterion.DetachedCriteria;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Projections;


public class UtilHibernate {
  EntityManager em;
  Class<?> entityClass;

  public UtilHibernate(EntityManager em, Class<?> entityClass) {
    this.em = em;
    this.entityClass = entityClass;
  }

  public List<?> lista(DetachedCriteria dc, ListProperties qp) {
    adicionaOrdem(dc, qp);
    return findByCriteria(em, dc, qp.getPrimeiro(), qp.getQuantidade());
  }
  
  public List<?> findByCriteria(EntityManager em, DetachedCriteria dc,int first, int maxResult) {
    Criteria criteria = dc.getExecutableCriteria(getSession());
    criteria.setFirstResult(first);
    criteria.setMaxResults(maxResult);
    return criteria.list();
  }

  public void adicionaOrdem(DetachedCriteria dc, ListProperties qp) {
    final List<Ordination> ordenacao = qp.getOrdenacao();
    if(ordenacao==null){
      return;
    }
    for (Ordination ordem : ordenacao) {
      if (ordem.getPropriedade() != null) {
        if (ordem.isAsc()) {
          dc.addOrder(Order.asc(ordem.getPropriedade()));
        } else {
          dc.addOrder(Order.desc(ordem.getPropriedade()));
        }
      }
    }
  }

  public long conta(DetachedCriteria dc) {
    Criteria criteria = dc.getExecutableCriteria(getSession());
    criteria.setProjection(Projections.count(getSession()
        .getSessionFactory().getClassMetadata(entityClass)
        .getIdentifierPropertyName()));
    Long result = (Long) criteria.uniqueResult();
    return result.intValue();
  }

  public Session getSession() {
    Session session = em.unwrap(Session.class);
    return session;
  }

}
