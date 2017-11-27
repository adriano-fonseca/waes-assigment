package com.company.app.dao;

import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import javax.persistence.TypedQuery;

import org.hibernate.proxy.HibernateProxy;

import com.company.app.exception.DAOException;
import com.company.app.exception.DiffExceptionException;
import com.company.app.exception.InsufficientDataException;
import com.company.app.exception.InvalidBase64Encode;
import com.company.app.exception.RecordNotFoundException;
import com.company.app.model.Data;
import com.company.app.model.Diff;
import com.company.app.util.UtilBase64;
import com.company.app.util.UtilMessages;
import com.company.app.util.UtilMessages.Messages;

@Stateless
public class DiffDAO {

	@PersistenceContext
	private EntityManager entityManager;
	
	public Diff add(Diff t) {
		if(!isValidBase64Content(t.getListData())){
			throw new InvalidBase64Encode(UtilMessages.getMessage("message", Messages.INVALID_BASE64_ENCODE.getMsg()));
		}
		
			if (entityManager.contains(t)) {
				throw new RuntimeException("Trying add an object already menaged.");
			}
			entityManager.persist(t);
			entityManager.flush();
			return t;
	}

	/**
	 * 
	 * @param list
	 * @return return true if all element at list have a valid Base64Encode
	 */
	boolean isValidBase64Content(List<Data> list) {
		if(list == null || list.isEmpty()){
			return false;
		}else {
			Long nrMatchs = getNumberOfElementsThatMatchToPattern(list);
			return nrMatchs == list.size();
		}
	}
	
	public Diff remove(Diff diff) {
		diff = this.find(diff);
		if (diff == null) {
			throw new RecordNotFoundException(UtilMessages.getMessage("message", UtilMessages.Messages.RECORD_NOT_FOUND.getMsg()));
		} else {
			entityManager.remove(diff);
			entityManager.flush();
		}
		return diff;
	}
	
	public Diff change(Diff t) {
		HashMap<String, String> msg = null;
		try {
			Diff queryBean = find(t);
			if (queryBean == null) {
				msg = new HashMap<String, String>();
				msg.put("message", "Record not found!");
				throw new RecordNotFoundException(msg);
			}
		} catch (DAOException e) {
			msg.put("message", "Record not found!");
			throw new RecordNotFoundException(msg);
		}

		Diff managed = entityManager.merge(t);
		entityManager.flush();
		return managed;
	}
	
	
	/**
	 * 
	 * @param diff
	 * @return find diff by id
	 */
	public Diff find(Diff diff) {
		Diff entityReturned = entityManager.find(Diff.class, diff.getIdDiff());
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
	/**
	 * 
	 * @param diff
	 * @return return all diffs
	 */
  final public List<Diff> list(Diff diff) {
  	TypedQuery<Diff> q =  entityManager.createQuery("select t from SimpleEntity t", Diff.class);
  	return q.getResultList();
  }


	/**
	 * 
	 * @param list
	 * @return number of elements that attends to pattern (REGEX) that validades if the data is a valid Base64 encode
	 */
	Long getNumberOfElementsThatMatchToPattern(List<Data> list) {
		return list.stream().map(data -> UtilBase64.isValidBase64Encode(data.getContent())).count();
	}

	public Diff findByIdFetchData(Long id){
		try{
			Query query = entityManager.createQuery("SELECT d FROM Diff d JOIN FETCH d.listData WHERE d.idDiff =:id");
			query.setParameter("id", id);
			return (Diff) query.getSingleResult();
		}catch (NoResultException e){
			throw new RecordNotFoundException(UtilMessages.getMessage("message", Messages.RECORD_NOT_FOUND.getMsg()));
		}
	}
	
	/**
	 * 
	 * @param idDiff
	 * @return return diff's result
	 */
	public HashMap<String, String> compare(Long idDiff) {
		Diff diff = getDataForDiff(idDiff);
		
		boolean readyToCompare = diff.getListData() != null && diff.getListData().size() == 2;
		if(!readyToCompare){
			throw new InsufficientDataException(UtilMessages.getMessage("message", Messages.INSUFICIENT_DATA.getMsg()));
		} 
		return compare(diff.getListData().get(0).getContent(),  diff.getListData().get(1).getContent());
	}
	
	/**
	 * 
	 * @param idDiff
	 * @return
	 * 
	 * Pache Scope for the Tests
	 */
	Diff getDataForDiff(Long idDiff) {
		try {
			return this.findByIdFetchData(idDiff);
		} catch (Exception e) {
			throw new InsufficientDataException(UtilMessages.getMessage("message",Messages.RECORD_NOT_FOUND.getMsg()));
		}
	}

	/**
	 * 
	 * @param content1
	 * @param content2
	 * @return The method returns DIFFERENT_LENGHTS to different content's size. 
	 * 		   EQUALS if they are exactly the same (size and content)
	 * 		   SAME_LENGHTS and DIFFERENCES_AT, this second an array with the positions 
	 * 		   where the differences were found.
	 */
	HashMap<String, String> compare(String content1, String content2) {
		try {
			byte[] dataDecoded1 = UtilBase64.decodeBase64(new String(content1.getBytes(), "UTF-8"));
			byte[] dataDecoded2 = UtilBase64.decodeBase64(new String(content2.getBytes(), "UTF-8"));
			return getResultBase64Comparantion(dataDecoded1, dataDecoded2);
		} catch (UnsupportedEncodingException e) {
			throw new DiffExceptionException(UtilMessages.getMessage("message",Messages.DIFF_ERROR.getMsg()));
		} catch (IllegalArgumentException e){
			throw new DiffExceptionException(UtilMessages.getMessage("message",Messages.DIFF_ERROR.getMsg()));
		}
	}

	private HashMap<String, String> getResultBase64Comparantion(byte[] dataDecoded1, byte[] dataDecoded2) {
		if(dataDecoded1.length != dataDecoded2.length) {
			HashMap<String, String> res = new HashMap<String, String>();
			res.put("STATUS", "DIFFERENT_LENGHTS");
			return res;
		} else {
			return getResultForContentsWithSameSize(dataDecoded1, dataDecoded2);
		}
	}

	private HashMap<String, String> getResultForContentsWithSameSize(byte[] dataDecoded1, byte[] dataDecoded2) {
		List<Integer> differences = getDifferences(dataDecoded1, dataDecoded2);
		HashMap<String, String> res = new HashMap<String, String>();
		
		if(differences.isEmpty()){
			res.put("STATUS", "EQUALS");
		}else {
			res.put("STATUS", "SAME_LENGHTS");
			res.put("DIFFERENCES", differences.toString());
		}
		return res;
	}

	private List<Integer> getDifferences(byte[] dataDecoded1, byte[] dataDecoded2) {
		List<Integer> differences = null;
		differences = IntStream.range(0,dataDecoded1.length).filter(i -> dataDecoded1[i] !=  dataDecoded2[i]).map(x -> x).boxed().collect(Collectors.toList());
		return differences;
	}

	@SuppressWarnings("unchecked")
	public List<Diff> listDiffWithDataLazzy(Diff diff) {
		List<Diff> list = this.entityManager.createQuery("SELECT NEW com.company.app.model.Diff(d.idDiff) FROM Diff d").getResultList();
		return list;
	}
}