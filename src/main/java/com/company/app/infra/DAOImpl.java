package com.company.app.infra;

import java.io.Serializable;
import java.util.HashMap;
import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.EntityNotFoundException;
import javax.persistence.NonUniqueResultException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.Hibernate;
import org.hibernate.proxy.HibernateProxy;
import org.hibernate.proxy.LazyInitializer;

import com.company.app.exception.DAOException;
import com.company.app.exception.RecordNotFoundException;
import com.company.app.model.BaseEntity;
import com.company.app.util.UtilMessages;

/**
 * Implements Bussined Logic(EJB) all DAO should extend this class and call the
 * method {@link #init(EntityManager)} in the
 * 
 * @link PostConstruct}
 * 
 * @author adriano-fonseca
 *
 * @param <Entity>
 */
public abstract class DAOImpl<Entity extends BaseEntity<? extends Serializable>> implements DAO<Entity> {

	private final Log log = LogFactory.getLog(getClass());

	private EntityManager em;
	protected AppQuery<Entity> BD;

	/**
	 * This method needs to be call in method noted with PostConstruct in the
	 * DAO
	 * 
	 * @param em
	 */
	protected void init(EntityManager em) {
		this.em = em;
		this.BD = new AppQuery<Entity>(em);
	}

	@Override
	public Entity find(Entity ed) {
		Entity entityReturned = find(new AppQueryVO.Builder(ed).build());
		HashMap<String, String> msg = null;
		if (entityReturned == null) {
			msg = new HashMap<String, String>();
			msg.put("message", "Record not found!");
			if (msg != null) {
				throw new RecordNotFoundException(msg);
			}
		}
		return entityReturned;
	}

	@Override
	public Entity find(AppQueryVO iseQuery) {
		long i = System.currentTimeMillis();
		if (iseQuery.isCount())
			iseQuery = iseQuery.getBuilder().count(false).build();
		List<Entity> list = BD.list(iseQuery);
		long f = System.currentTimeMillis();
		if (log.isDebugEnabled())
			log.debug("*** [Entity find(Entity ed, IseQueryVO<Entity> iseQuery)] " + (f - i) + " ms.");
		if (list == null || list.isEmpty())
			return null;
		if (list.size() > 1)
			throw new NonUniqueResultException(iseQuery.getEntity().toString());
		list.set(0, hibernateProxy2Impl(list.get(0)));
		return list.get(0);
	}

	@Override
	public Entity find(AppQueryVO.Builder iseQueryBuilder) {
		return find(iseQueryBuilder.build());
	}

	@Override
	public List<Entity> list(Entity ed) {
		return list(new AppQueryVO.Builder(ed).build());
	}

	@Override
	public List<Entity> listDetached(Entity ed) {
		return list(new AppQueryVO.Builder(ed).detachedState(true).build());
	}

	@Override
	public List<Entity> list(Entity ed, ListProperties pl) {
		return list(new AppQueryVO.Builder(ed).propList(pl).build());
	}

	@Override
	public List<Entity> list(AppQueryVO iseQuery) {
		long i = System.currentTimeMillis();
		if (iseQuery.isCount())
			iseQuery = iseQuery.getBuilder().count(false).build();
		List<Entity> list = BD.list(iseQuery);
		int j = 0;
		for (Entity edItem : list) {
			list.set(j++, hibernateProxy2Impl(edItem));
		}
		long f = System.currentTimeMillis();
		if (log.isDebugEnabled())
			log.debug(
					"*** [List<Entity> list(Entity ed, IseQueryVO<Entity> iseQuery)] executou em: " + (f - i) + " ms.");
		return list;
	}

	@Override
	public List<Entity> list(AppQueryVO.Builder iseQueryBuilder) {
		return list(iseQueryBuilder.build());
	}

	@Override
	public long count(Entity ed) {
		return count(new AppQueryVO.Builder(ed).count(true).fetchJoinInNotNullProperty(false).build());
	}

	@Override
	public long count(AppQueryVO iseQuery) {
		long i = System.currentTimeMillis();
		if (!iseQuery.isCount())
			iseQuery = iseQuery.getBuilder().count(true).build();
		Number count = BD.count(iseQuery);
		long f = System.currentTimeMillis();
		if (log.isDebugEnabled())
			log.debug("*** [Number count(Entity ed, IseQueryVO<Entity> iseQuery)] executou em: " + (f - i) + " ms.");
		return count == null ? 0L : count.longValue();
	}

	@Override
	public long count(AppQueryVO.Builder iseQueryBuilder) {
		return count(iseQueryBuilder.build());
	}

	@Override
	public <T extends BaseEntity<? extends Serializable>> T findById(Class<T> clazz, Serializable id) {
		if (clazz == null || id == null)
			throw new IllegalArgumentException();
		T t = em.find(clazz, id);
		return t;
	}

	@SuppressWarnings("unchecked")
	protected <T extends BaseEntity<? extends Serializable>> T hibernateProxy2Impl(T ed) {
		if (ed != null && HibernateProxy.class.isAssignableFrom(ed.getClass())) {
			try {
				return (T) ((HibernateProxy) ed).getHibernateLazyInitializer().getImplementation();
			} catch (Exception e) {
				return ed;
			}
		}
		return ed;
	}

	protected <T extends BaseEntity<? extends Serializable>> boolean isNullOrHProxy(T ed) {
		if (ed == null || HibernateProxy.class.isAssignableFrom(ed.getClass()))
			return true;
		return false;
	}

	public static Number recuperarIdNumberDeEntidadeLazy(Object edProxy) {
		return (Number) recuperarIdDeEntidadeLazy(edProxy);
	}

	public static Serializable recuperarIdDeEntidadeLazy(Object edProxy) {
		if (edProxy == null)
			return null;
		if (!Hibernate.isInitialized(edProxy)) {
			HibernateProxy hProxy = (HibernateProxy) edProxy;
			LazyInitializer lazyInitializer = hProxy.getHibernateLazyInitializer();
			return lazyInitializer.getIdentifier();
		}
		return ((BaseEntity<?>) edProxy).getIdEntity();
	}

	public Entity add(Entity t) {
		if (em.contains(t)) {
			throw new RuntimeException("Trying add an object already menaged.");
		}
		em.persist(t);
		em.flush();
		return t;
	}

	public Entity change(Entity t) {
		HashMap<String, String> msg = null;
		try {
			Entity queryBean = find(t);
			if (queryBean == null) {
				msg = new HashMap<String, String>();
				msg.put("message", "Record not found!");
				throw new RecordNotFoundException(msg);
			}
		} catch (DAOException e) {
			msg.put("message", "Record not found!");
			throw new RecordNotFoundException(msg);
		}

		Entity managed = em.merge(t);
		if (UtilDAO.isOpenJpa(em)) {
			UtilDAO.forcaMergeNull(t, managed);
		}
		em.flush();
		return managed;
	}

	@SuppressWarnings("unchecked")
	public Entity remove(Entity t) {
		Object conteudoPk = t.getIdEntity();

		Entity entityFind = (Entity) em.find(t.getClass(), conteudoPk);
		if (entityFind == null) {
			throw new RecordNotFoundException(UtilMessages.getMessage("message", UtilMessages.Messages.RECORD_NOT_FOUND.getMsg()));
		} else {
			em.remove(entityFind);
			em.flush();
		}
		return entityFind;
	}

}