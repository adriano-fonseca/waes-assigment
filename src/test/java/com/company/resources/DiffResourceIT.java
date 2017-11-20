package com.company.resources;

import static org.junit.Assert.assertEquals;

import java.util.HashMap;

import javax.inject.Inject;
import javax.ws.rs.core.Response;

import org.codehaus.jackson.map.ObjectMapper;
import org.hibernate.transform.ResultTransformer;
import org.hibernate.type.Type;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import com.company.app.model.Data;
import com.company.app.model.Diff;
import com.company.app.model.Side;
import com.company.app.resources.DiffResource;

@RunWith(Arquillian.class)
public class DiffResourceIT {
	

  @Inject
  DiffResource   diffResource;
  
  @Deployment
  public static WebArchive createDeployment() {
    return ShrinkWrap.create(WebArchive.class)
    		.addClass(Type.class)
    		.addClass(ResultTransformer.class)
    		.addPackages(true, "com.company.app")
    .addAsResource("h2-test-persistence.xml", "META-INF/persistence.xml");
  }


  @Before
  public void cleanStudentData() {
    System.out.println("always before a test method");
  }
  
  //TODO: Criar testes para os entryPois exigidos pelo menos
  @Test
  public void testeAddLeft(){
	  Data data = new Data("d2Flcw==", 1L, Side.L);
	  Response res = diffResource.addLeft(data, 1L);
	  assertEquals(res.getStatus(),200);
  }
  
  @Test
  public void testeAddRight(){
	  Data data = new Data("d2Flcw==", 1L, Side.R);
	  Response res = diffResource.addLeft(data, 1L);
	  assertEquals(res.getStatus(),200);
  }
  
  @Test
  public void testeAddCompare(){
	 Diff diff = new Diff(1L);
	 Data left = new Data("d2Flcw==", 1L, Side.L);
	 Data right = new Data("d2Flcw==", 1L, Side.R);
	 diff.getListData().add(left);
	 diff.getListData().add(right);
	 diffResource.addDiff(diff, 1L);
	 Response res = diffResource.compare(1L);
	 System.out.println(res.getEntity());
	// ObjectMapper objectMapper = new ObjectMapper();
	 //HashMap result = objectMapper.readValue(res.getEntity().getContent(), HashMap.class);
	 assertEquals(((Response) res.getEntity()).getStatus(), 200);
  }

}