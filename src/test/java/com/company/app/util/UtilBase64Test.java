package com.company.app.util;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.UnsupportedEncodingException;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

@RunWith(PowerMockRunner.class)
//Static classes need to be prepar before to use 
@PrepareForTest(UtilBase64.class)
public class UtilBase64Test {

  @Before
  public void setUp() throws Exception {
	  
  }
  
  @Test
  public void shouldFailIfDecodeGiveSomethingDifferentThanExpected(){
	  byte[] decoded = UtilBase64.decodeBase64("QWRyaWFubw==","UTF-8");
	  try {
		assertEquals("Adriano", new String(decoded, "UTF-8"));
	} catch (UnsupportedEncodingException e) {
		e.printStackTrace();
	}
  }
  
  @Test
  public void shouldFailIfEncodenGiveSomethingDifferentThanExpected(){
	  try {
		  String param = new String("Adriano".getBytes(), "UTF-8");
		  byte[] decoded = UtilBase64.encodeBase64(param);
		  assertEquals("QWRyaWFubw==", new String(decoded, "UTF-8"));
	} catch (UnsupportedEncodingException e) {
		e.printStackTrace();
	}
  }
  
  @Test
  public void shouldFailIfDecodedGiveSomethingDifferentThanExpectedDefaultCharset(){
	  byte[] decoded = UtilBase64.decodeBase64("QWRyaWFubw==");
	  assertEquals("Adriano", new String(decoded));
  }
  
  @Test
  public void shouldFailIfEncodedGiveSomethingDifferentThanExpectedDefaultCharset(){
		byte[] decoded = UtilBase64.encodeBase64(new String("Adriano".getBytes()));
		assertEquals("QWRyaWFubw==", new String(decoded));
  }
  
  @Test
  public void shouldFailIfisValidReturnTrueToInvalidPattern(){
	  assertFalse(UtilBase64.isValidBase64Encode("A"));
  }
  
  @Test
  public void shouldFailIfisValidReturnTrue2(){
	  assertFalse(UtilBase64.isValidBase64Encode("ABC"));
  }
  
  @Test
  public void shouldFailIfisValidReturnFalseToValidPattern(){
	  assertTrue(UtilBase64.isValidBase64Encode("ABDC"));
  }
  
  @Test
  public void shouldFailIfisValidReturnFalseToValidPattern2(){
	  assertTrue(UtilBase64.isValidBase64Encode("AB=="));
  }
  
  @Test
  public void shouldFailIfisValidReturnFalseToValidPattern3(){
	  assertTrue(UtilBase64.isValidBase64Encode("ABC="));
  }
  
  @Test
  public void shouldFailIfisValidReturnFalseToValidPattern4(){
	  assertTrue(UtilBase64.isValidBase64Encode("AB=="));
  }
  
  
 
}