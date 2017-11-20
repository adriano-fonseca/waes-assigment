package com.company.app.dao;

import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import javax.annotation.PostConstruct;
import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;

import com.company.app.exception.DiffExceptionException;
import com.company.app.exception.InsufficientDataException;
import com.company.app.exception.InvalidBase64Encode;
import com.company.app.exception.RecordNotFoundException;
import com.company.app.infra.DAOImpl;
import com.company.app.model.Data;
import com.company.app.model.Diff;
import com.company.app.util.UtilBase64;
import com.company.app.util.UtilMessages;
import com.company.app.util.UtilMessages.Messages;

@Stateless
public class DiffDAO extends DAOImpl<Diff> {

	@PersistenceContext
	private EntityManager entityManager;
	
	@Override
	public Diff add(Diff t) {
		if(!isValidBase64Content(t.getListData())){
			throw new InvalidBase64Encode(UtilMessages.getMessage("message", Messages.INVALID_BASE64_ENCODE.getMsg()));
		}
		
		return super.add(t);
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

	/**
	 * 
	 * @param list
	 * @return number of elements that attends to pattern (REGEX) that validades if the data is a valid Base64 encode
	 */
	Long getNumberOfElementsThatMatchToPattern(List<Data> list) {
		return list.stream().map(data -> UtilBase64.isValidBase64Encode(data.getContent())).count();
	}

	@PostConstruct
	public void init() {
		super.init(entityManager);
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
	 * 		   SAME_LENGHTS and DIFFERENCES_AT, this seconde an array with the positions 
	 * 		   where the differences were found.
	 */
	HashMap<String, String> compare(String content1, String content2) {
		if(content1.length() != content2.length()) {
			return UtilMessages.getMessage("result", "DIFFERENT_LENGHTS");
		} else {
			return getResultForContentsWithSameSize(content1, content2);
		}
	}

	private HashMap<String, String> getResultForContentsWithSameSize(String content1, String content2) {
		HashMap<String,String> result = null;
		List<Integer> differences = getDifferences(content1, content2);
		
		if(differences.isEmpty()){
			result = UtilMessages.getMessage("result", "EQUALS");
		}else {
			result = UtilMessages.getMessage("result", "SAME_LENGHTS");
			result.put("DIFFERENCES_AT", differences.toString());
		}
		return result;
	}

	private List<Integer> getDifferences(String content1, String content2) {
		List<Integer> differences = null;
		try {
			byte[] dataDecoded1 = UtilBase64.decodeBase64(new String(content1.getBytes(), "UTF-8"));
			byte[] dataDecoded2 = UtilBase64.decodeBase64(new String(content2.getBytes(), "UTF-8"));
			differences = IntStream.range(0,dataDecoded1.length).filter(i -> dataDecoded1[i] !=  dataDecoded2[i]).map(x -> x).boxed().collect(Collectors.toList());
		} catch (UnsupportedEncodingException e) {
			throw new DiffExceptionException(UtilMessages.getMessage("message",Messages.DIFF_ERROR.getMsg()));
		}
		return differences;
	}

	@SuppressWarnings("unchecked")
	public List<Diff> listDiffWithDataLazzy(Diff diff) {
		List<Diff> list = this.entityManager.createQuery("SELECT NEW com.company.app.model.Diff(d.idDiff) FROM Diff d").getResultList();
		return list;
	}
}