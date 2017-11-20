package com.company.app.exception;

import java.util.HashMap;


/**
 * 
 * Exception luached when a the content of left or right are not a valid Base64 encode 
 * 
 * @author adriano-fonseca
 *
 */
public class InvalidBase64Encode extends DAOException {

  private static final long serialVersionUID = 1L;

  public InvalidBase64Encode(HashMap<String, String> msg) {
    super(msg.get("message"));
  }
}