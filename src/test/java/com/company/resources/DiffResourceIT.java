package com.company.resources;

import static org.junit.Assert.assertEquals;

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
import com.company.app.model.Result;
import com.company.app.model.ResultWithDifferences;
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

	@Before
	public void cleanStudentData() {
		System.out.println("always before a test method");
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
		Result result = (Result) res.getEntity();
		assertEquals("EQUALS", result.getStatus());
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
		Result result = (Result) res.getEntity();
		System.out.println(result);
		assertEquals(result.getStatus(), "SAME_LENGHTS");
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
		ResultWithDifferences result = (ResultWithDifferences) res.getEntity();
		System.out.println(result);
		assertEquals(result.getDifferences(), "[2]");
	}

	@Test
	@ApplyScriptAfter("deleteAllData.sql")
	public void shouldFailIfDifferencesNotMatch2() {
		Diff diff = new Diff(1L);
		// AdrIanO
		Data left = new Data("QWRySWFuTw==", 1L, Side.L);
		// adriano
		Data right = new Data("YWRyaWFubw==", 1L, Side.R);
		diff.getListData().add(left);
		diff.getListData().add(right);
		diffResource.addDiff(diff, 1L);
		Response res = diffResource.compare(1L);
		ResultWithDifferences result = (ResultWithDifferences) res.getEntity();
		System.out.println(result);
		assertEquals(result.getDifferences(), "[0, 3, 6]");
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
		Result result = (Result) res.getEntity();
		System.out.println(result);
		assertEquals(result.getStatus(), "DIFFERENT_LENGHTS");
	}

}