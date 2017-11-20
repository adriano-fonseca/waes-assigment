package com.company.app.util;

import static org.junit.Assert.assertEquals;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Spy;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

@RunWith(PowerMockRunner.class)
//Static classes need to be prepar before to use 
@PrepareForTest(UtilMessages.class)
public class UtilMassageTest {

  @Spy
  UtilMessages  utilBase64 = new UtilMessages();

  
  @Test
  public void shouldFailIfMapWithouKeyMessageIsReturned(){
	  assertEquals("TEST", UtilMessages.getMessage("message","TEST").get("message"));
  }
  
}	