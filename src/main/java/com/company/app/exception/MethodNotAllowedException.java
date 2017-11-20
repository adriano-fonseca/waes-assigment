package com.company.app.exception;

import java.util.HashMap;


/**
 * 
 * Exception luached when a HTTP method called is not correct for the operation requested
 *  * 
 * @author adriano-fonseca
 *
 */
public class MethodNotAllowedException extends DAOException {

  private static final long serialVersionUID = 1L;

  public MethodNotAllowedException(HashMap<String, String> msg) {
    super(msg.get("message"));
  }
}