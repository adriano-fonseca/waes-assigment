package com.company.app.exception;

import java.util.HashMap;


/**
 * 
 * Exception luached when something unexpected happens in the diff
 * 
 * @author adriano-fonseca
 *
 */
public class DiffExceptionException extends DAOException {

  private static final long serialVersionUID = 1L;

  public DiffExceptionException(HashMap<String, String> msg) {
    super(msg.get("message"));
  }
}