package com.company.app.exception;

import java.util.HashMap;
import java.util.Map;

import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

@Provider
public class DAOExceptionMapper implements ExceptionMapper<DAOException> {
	
	@Override
	public Response toResponse(DAOException ex) {
		Map<String, String> erros = new HashMap<String, String>();
		erros.put("message", ex.getMessage());
		return Response.status(Status.BAD_REQUEST).entity(erros).build();
	}

}
