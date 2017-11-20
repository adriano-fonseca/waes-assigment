package com.company.app.exception;

import java.util.HashMap;


/**
 * 
 * Exception luached when a record is not found 
 * 
 * @author adriano-fonseca
 *
 */
public class RecordNotFoundException extends DAOException {

  private static final long serialVersionUID = 1L;

  public RecordNotFoundException(HashMap<String, String> msg) {
    super(msg.get("message"));
  }
}