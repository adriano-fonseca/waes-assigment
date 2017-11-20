package com.company.app.services;

import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Spy;
import org.powermock.modules.junit4.PowerMockRunner;

import com.company.app.model.Data;
import com.company.app.model.Side;
import com.company.app.services.DiffService;

@RunWith(PowerMockRunner.class)

public class DiffServiceTest {
	List<Data> noData = new ArrayList<Data>();
	List<Data> dataLeftOnly = new ArrayList<Data>();
	List<Data> dataRightOly = new ArrayList<Data>();
	List<Data> dataBothSides = new ArrayList<Data>();
	
	@Spy
	DiffService diffService = new DiffService();

	@Before
	public void setUp() throws Exception {
		Data dataLeft = new Data("", 1L, Side.L);
		Data dataRight = new Data("", 1L, Side.R);

		this.dataLeftOnly.add(dataLeft);

		this.dataRightOly.add(dataRight);

		this.dataBothSides.add(dataLeft);
		this.dataBothSides.add(dataRight);
	}

	@Test
	public void shouldFailIfReturnsFalseWithLeftDataOnTheList() {
		assertTrue(this.diffService.isTheSidePassedOnTheList(Side.L, dataLeftOnly));
	}
	
	@Test
	public void shouldFailIfReturnsFalseWithLeftDataOnTheList2() {
		assertTrue(this.diffService.isTheSidePassedOnTheList(Side.L, dataBothSides));
	}
	
	@Test
	public void shouldFailIfReturnsTrueWithNoLeftDataOnTheList() {
		assertFalse(this.diffService.isTheSidePassedOnTheList(Side.L, dataRightOly));
	}
	
	@Test
	public void shouldFailIfReturnsTrueWithNoDataLeft() {
		assertFalse(this.diffService.isTheSidePassedOnTheList(Side.L, noData));
	}
	
	@Test
	public void shouldFailIfReturnsFalseWithRightDataOnTheList2() {
		assertTrue(this.diffService.isTheSidePassedOnTheList(Side.R, dataBothSides));
	}
	
	@Test
	public void shouldFailIfReturnsTrueWithNoRightDataOnTheList() {
		assertFalse(this.diffService.isTheSidePassedOnTheList(Side.R, dataLeftOnly));
	}
	
	@Test
	public void shouldFailIfReturnsTrueWithNoDataRight() {
		assertFalse(this.diffService.isTheSidePassedOnTheList(Side.R, noData));
	}

}