package com.company.resources;

import static org.junit.Assert.assertEquals;

import java.util.HashMap;

import javax.inject.Inject;
import javax.ws.rs.core.Response;

import org.apache.http.HttpEntity;
import org.hibernate.transform.ResultTransformer;
import org.hibernate.type.Type;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.arquillian.persistence.ApplyScriptAfter;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import com.company.app.model.Data;
import com.company.app.model.Diff;
import com.company.app.model.Side;
import com.company.app.resources.DiffResource;

/**
 * To generate data fot the tests I have used the toll on
 * https://www.base64encode.org/
 * 
 * @author adriano
 *
 */
@RunWith(Arquillian.class)
public class DiffResourceIT {

	@Inject
	DiffResource diffResource;

	@Deployment
	public static WebArchive createDeployment() {
		return ShrinkWrap.create(WebArchive.class).addClass(Type.class).addClass(HttpEntity.class)
				.addClass(ResultTransformer.class).addPackages(true, "com.company.app")
				.addAsResource("h2-test-persistence.xml", "META-INF/persistence.xml");
	}

	@Test
	@ApplyScriptAfter("deleteAllData.sql")
	public void testeAddLeft() {
		Data data = new Data("d2Flcw==", 31L, Side.L);
		Response res = diffResource.addLeft(data, 31L);
		assertEquals(200, res.getStatus());
	}

	@Test
	@ApplyScriptAfter("deleteAllData.sql")
	public void testeAddRight() {
		Data data = new Data("d2Flcw==", 32L, Side.R);
		Response res = diffResource.addLeft(data, 32L);
		assertEquals(200, res.getStatus());
	}

	@Test
	@ApplyScriptAfter("deleteAllData.sql")
	public void shouldFailIfTheyAreNotEqual() {
		Diff diff = new Diff(1L);
		Data left = new Data("d2Flcw==", 1L, Side.L);
		Data right = new Data("d2Flcw==", 1L, Side.R);
		diff.getListData().add(left);
		diff.getListData().add(right);
		diffResource.addDiff(diff, 1L);
		Response res = diffResource.compare(1L);
		HashMap<String, String> result = (HashMap<String, String>) res.getEntity();
		assertEquals("EQUALS", result.get("STATUS"));
	}

	@Test
	@ApplyScriptAfter("deleteAllData.sql")
	public void shouldFailIfTheyAreNotSameLenght() {
		Diff diff = new Diff(1L);
		// waes
		Data left = new Data("d2Flcw==", 1L, Side.L);
		// waEs
		Data right = new Data("d2FFcw==", 1L, Side.R);
		diff.getListData().add(left);
		diff.getListData().add(right);
		diffResource.addDiff(diff, 1L);
		Response res = diffResource.compare(1L);
		HashMap<String, String> result = (HashMap<String, String>) res.getEntity();		System.out.println(result);
		assertEquals("SAME_LENGHTS", result.get("STATUS"));
	}

	@Test
	@ApplyScriptAfter("deleteAllData.sql")
	public void shouldFailIfDifferencesNotMatch() {
		Diff diff = new Diff(1L);
		// waes
		Data left = new Data("d2Flcw==", 1L, Side.L);
		// waEs
		Data right = new Data("d2FFcw==", 1L, Side.R);
		diff.getListData().add(left);
		diff.getListData().add(right);
		diffResource.addDiff(diff, 1L);
		Response res = diffResource.compare(1L);
		HashMap<String, String> result = (HashMap<String, String>) res.getEntity();		System.out.println(result);
		assertEquals("[2]", result.get("DIFFERENCES"));
	}
	
	@Test
	@ApplyScriptAfter("deleteAllData.sql")
	public void shouldFailIfDifferencesNotMatch2() {
		Diff diff = new Diff(1L);
		// waes
		Data right = new Data("d2Flcw==", 1L, Side.R);
		// waEs
		Data left = new Data("d2FFcw==", 1L, Side.L);
		diff.getListData().add(left);
		diff.getListData().add(right);
		diffResource.addDiff(diff, 1L);
		Response res = diffResource.compare(1L);
		HashMap<String, String> result = (HashMap<String, String>) res.getEntity();		System.out.println(result);
		assertEquals("[2]", result.get("DIFFERENCES"));
	}

	@Test
	@ApplyScriptAfter("deleteAllData.sql")
	public void shouldFailIfDifferencesNotMatch3() {
		Diff diff = new Diff(1L);
		// AdrIanO
		Data left = new Data("QWRySWFuTw==", 1L, Side.L);
		// adriano
		Data right = new Data("YWRyaWFubw==", 1L, Side.R);
		diff.getListData().add(left);
		diff.getListData().add(right);
		diffResource.addDiff(diff, 1L);
		Response res = diffResource.compare(1L);
		HashMap<String, String> result = (HashMap<String, String>) res.getEntity();
		assertEquals("[0, 3, 6]", result.get("DIFFERENCES"));
	}
	
	@Test
	@ApplyScriptAfter("deleteAllData.sql")
	public void shouldFailIfDoesNotDetectDifferentLenght() {
		Diff diff = new Diff(1L);
		// diego maradona
		Data left = new Data("ZGllZ28gbWFyYWRvbmE=", 1L, Side.L);
		// diego maradon
		Data right = new Data("ZGllZ28gbWFyYWRvbg==", 1L, Side.R);
		diff.getListData().add(left);
		diff.getListData().add(right);
		diffResource.addDiff(diff, 1L);
		Response res = diffResource.compare(1L);
		HashMap<String, String> result = (HashMap<String, String>) res.getEntity();
		assertEquals("DIFFERENT_LENGHTS", result.get("STATUS"));
	}
	
	@Test
	@ApplyScriptAfter("deleteAllData.sql")
	public void shouldFailIfDoesNotDetectDifferentLenght2() {
		Diff diff = new Diff(1L);
		// diego maradona
		Data right = new Data("ZGllZ28gbWFyYWRvbmE=", 1L, Side.R);
		// diego maradon
		Data left = new Data("ZGllZ28gbWFyYWRvbg==", 1L, Side.L);
		diff.getListData().add(left);
		diff.getListData().add(right);
		diffResource.addDiff(diff, 1L);
		Response res = diffResource.compare(1L);
		HashMap<String, String> result = (HashMap<String, String>) res.getEntity();
		assertEquals("DIFFERENT_LENGHTS", result.get("STATUS"));
	}

	@Test
	@ApplyScriptAfter("deleteAllData.sql")
	public void shouldFailIfTheyAreNotDifferentLenght() {
		Diff diff = new Diff(1L);
		Data left = new Data("d2Flcw==", 1L, Side.L);
		Data right = new Data("d2FF", 1L, Side.R);
		diff.getListData().add(left);
		diff.getListData().add(right);
		diffResource.addDiff(diff, 1L);
		Response res = diffResource.compare(1L);
		HashMap<String, String> result = (HashMap<String, String>) res.getEntity();
		assertEquals("DIFFERENT_LENGHTS", result.get("STATUS"));
	}
	
	

}