package com.company.app.resources;

import java.util.HashMap;
import java.util.List;

import javax.inject.Inject;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import com.company.app.exception.RecordNotFoundException;
import com.company.app.model.Data;
import com.company.app.model.Diff;
import com.company.app.model.Result;
import com.company.app.model.Side;
import com.company.app.services.DiffService;
import com.company.app.util.UtilMessages;
import com.company.app.util.UtilMessages.Messages;	

@Path("diff/")
@Produces(MediaType.APPLICATION_JSON + ";charset=utf-8")
@Consumes(MediaType.APPLICATION_JSON + ";charset=utf-8")
public class DiffResource {

	@Inject
	DiffService diffService;

	@GET
	@Path("{idDiff}/show")
	public Response show(@PathParam("idDiff") Long idDiff) {
		Response response = null;
		try {
			Diff diff = diffService.find(idDiff);
			response = Response.ok().entity(diff).build();
		} catch (Exception e) {
			response =  Response.status(Response.Status.NOT_FOUND).entity(UtilMessages.getMessage("message", Messages.RECORD_NOT_FOUND.getMsg())).build();
		}
		return response;
	}
	
	@GET
	@Path("{idDiff}")
	public Response compare(@PathParam("idDiff") Long idDiff) {
		Result res = diffService.compare(idDiff);
		return Response.ok().entity(res).build();
	}

	@GET
	public Response list() {
		List<Diff> diffs = diffService.listDiffWithDataLazzy();
		return Response.ok().entity(diffs).build();
	}

	@DELETE
	@Path("{idDiff}")
	public Response remove(@PathParam("idDiff") Long idDiff) {
		Response response = null;
		try{
			diffService.remove(idDiff);
			response = Response.ok().entity(UtilMessages.getMessage("message", Messages.RECORD_REMOVED.getMsg())).build();
		} catch (RecordNotFoundException e) {
			response =  Response.status(Response.Status.NOT_FOUND).entity(UtilMessages.getMessage("message", Messages.RECORD_NOT_FOUND.getMsg())).build();
		}
		return response;
	}

	@POST
	@Path("{idDiff}")
	public Response addDiff(Diff diff, @PathParam("idDiff") Long idDiff) {
		diff.setIdDiff(idDiff);
		diff = diffService.add(diff);
		return Response.ok().entity(diff).build();
	}

	@POST
	@Path("{idDiff}/right")
	public Response addRight(Data data, @PathParam("idDiff") Long idDiff) {
		return diffService.addDataForDiff(data, idDiff, Side.R);
	}

	@POST
	@Path("{idDiff}/left")
	public Response addLeft(Data data, @PathParam("idDiff") Long idDiff) {
		return diffService.addDataForDiff(data, idDiff, Side.L);
	}
	
	@PUT
	@Path("{idDiff}/right")
	public Response changeRight(Data data, @PathParam("idDiff") Long idDiff) {
		return diffService.changeDataForDiff(data, idDiff, Side.R);
	}

	@PUT
	@Path("{idDiff}/left")
	public Response changeLeft(Data data, @PathParam("idDiff") Long idDiff) {
		return diffService.changeDataForDiff(data, idDiff, Side.L);
	}
}