package com.company.app.services;

import java.util.HashMap;
import java.util.List;

import javax.enterprise.inject.Model;
import javax.inject.Inject;
import javax.ws.rs.core.Response;

import com.company.app.dao.DiffDAO;
import com.company.app.exception.MethodNotAllowedException;
import com.company.app.exception.RecordNotFoundException;
import com.company.app.model.Data;
import com.company.app.model.Diff;
import com.company.app.model.Side;
import com.company.app.util.UtilMessages;
import com.company.app.util.UtilMessages.Messages;

@Model
public class DiffService {

	@Inject
	DiffDAO diffDAO;

	public Diff find(Long idDiff) {
		return diffDAO.findByIdFetchData(idDiff);
	}

	public Response remove(Long idDiff) {
		Response response = null;
		try {
			Diff diff = new Diff();
			diff.setIdDiff(idDiff);
			diffDAO.remove(diff);
			response = Response.ok().entity(UtilMessages.getMessage("message", Messages.RECORD_REMOVED.getMsg())).build();
		} catch (RecordNotFoundException e) {
			response = Response.status(Response.Status.BAD_REQUEST).entity(UtilMessages.getMessage("message", Messages.RECORD_NOT_FOUND.getMsg())).build();

		}
		return response;
	}

	public Diff add(Diff diff) {
		diff = diffDAO.add(diff);
		return diff;
	}

	public Diff change(Diff diff) {
		return diffDAO.change(diff);
	}

	public List<Diff> list() {
		return diffDAO.list(new Diff());
	}

	public List<Diff> listDiffWithDataLazzy() {
		return diffDAO.listDiffWithDataLazzy(new Diff());
	}
	
	public HashMap<String, String> compare(Long idDiff) {
		return diffDAO.compare(idDiff);
	}

	/**
	 * Try to get a Diff from database, if there is not a Diff persisted, 
	 * it will persist the diff and the left data.
	 * 
	 * Case the Diff is already persisted, we need to check if there is already
	 * a left data persisted, it there is we need to return HTTP Error 405 (Method Not Allowed), 
	 * otherwise we will persist the left data.
	 * 
	 * PS: Second the HATEOS (Level 2) guidelines the Post Verb should be use to create
	 * and PUT should be used to Update Information
	 * 
	 * @param data
	 * @param idDiff
	 * @param side
	 * @return
	 */
	public Response addDataForDiff(Data data, Long idDiff, Side side) {
		Response response = null;
		data.setSide(side);
		
		try{
			//Can launch RecordNotFoundException
			Diff diff = diffDAO.findByIdFetchData(idDiff);
			//Can lauch MethodNotAllowedException
			diff = changeDiffData(diff, data, side);
			//If no Exceptions HTTP Status 200
			response = Response.ok().entity(diff).build();
		} catch (RecordNotFoundException e){
			Diff diff = addDiffAndData(idDiff, data);
			response = Response.ok().entity(diff).build();
		} catch (MethodNotAllowedException e){
			response = Response.status(Response.Status.METHOD_NOT_ALLOWED).entity(UtilMessages.getMessage("message", Messages.PUT_TO_UPDATE.getMsg())).build();
		}
		
		return response;
	}
	
	/**
	 * Basically checks if this side of Diff has already been persisted. If was launch
	 * an MethodNotAllowedException, because is an update (Should use PUT), or else change
	 * diff adding this side data.
	 * 
	 * @param diff
	 * @param data 
	 * @param side
	 * @return
	 */
	private Diff changeDiffData(Diff diff, Data data, Side side) {
		boolean isDataUpdate = isTheSidePassedOnTheList(side, diff.getListData());
		
		if(isDataUpdate){
			throw new MethodNotAllowedException(UtilMessages.getMessage("message", Messages.PUT_TO_UPDATE.getMsg()));
		}else {
			diff.getListData().add(data);
			diff = diffDAO.change(diff);
		}
		return diff;
	}

	private Diff addDiffAndData(Long idDiff, Data data) {
		Diff diff = new Diff(idDiff);
		diff.getListData().add(data);
		diff = diffDAO.add(diff);
		return diff;
	}

	/**
	 * 
	 * @param side
	 * @param listData
	 * @return return tru if there is any occurence of the side passed on the list
	 * 
	 * PS: Using scope package (accessed just for class in the same package) to execute Unit Tests.
	 */
	boolean isTheSidePassedOnTheList(Side side, List<Data> listData) {
		return listData.stream().anyMatch(data -> data.getSide().equals(side));
	}

	public Response changeDataForDiff(Data data, Long idDiff, Side r) {
		// TODO fazer metodo put para alterar data sides
		return null;
	}
}
