package com.company.resources;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Spy;
import org.powermock.modules.junit4.PowerMockRunner;

import com.company.app.resources.DiffResource;

@RunWith(PowerMockRunner.class)
public class DiffResourceTest {

  @Spy
  DiffResource  diffResource = new DiffResource();

  
  @Test
  public void shouldFailIfDataValid(){
	  
  }
 
 }