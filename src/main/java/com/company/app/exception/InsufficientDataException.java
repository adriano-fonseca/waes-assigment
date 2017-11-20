package com.company.app.exception;

import java.util.HashMap;


/**
 * 
 * Exception luached when there is not enough data to perform the operation
 * 
 * @author adriano-fonseca
 *
 */
public class InsufficientDataException extends DAOException {

  private static final long serialVersionUID = 1L;

  public InsufficientDataException(HashMap<String, String> msg) {
    super(msg.get("message"));
  }
}