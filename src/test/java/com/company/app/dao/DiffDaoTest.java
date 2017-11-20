package com.company.app.dao;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.doThrow;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Spy;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import com.company.app.exception.InsufficientDataException;
import com.company.app.model.Data;
import com.company.app.util.UtilBase64;

@RunWith(PowerMockRunner.class)
//Static classes need to be prepar before to use 
@PrepareForTest(UtilBase64.class)
public class DiffDaoTest {

  @Spy
  DiffDAO    diffDAO = new DiffDAO();
  
  UtilBase64 utilBase64;
  
  List<Data> dataValid = new ArrayList<Data>();
  List<Data> dataValid2 = new ArrayList<Data>();
  List<Data> dataValid3 = new ArrayList<Data>();
  

  @Before
  public void setUp() throws Exception {
	  Data d1 = new Data();
	  Data d2 = new Data();
	  Data d3 = new Data();
	  Data d4 = new Data();

	  d1.setContent("QWRyaWFubw==");
	  d2.setContent("YWRyaUFubwx=");
	  d3.setContent("YQ==");
	  d4.setContent("YWJj");
	  
	  this.dataValid.add(d1);
	  
	  this.dataValid2.add(d1);
	  this.dataValid2.add(d2);
	  
	  this.dataValid3.add(d3);
	  this.dataValid3.add(d4);
//    turmaED.setCursoED(new CursoED());
//    turmaED.setCalendarioEstabED(new CalendarioEstabED());
//    turmaED.getCalendarioEstabED().setNroIntDuracCal(DuracaoCalendario.ANUAL.getId());
    
   // doNothing().when(diffResource).addDiff(any(Diff.class), any(Long.class));
    
    //Mockando Classe estatica com powermock
    //PowerMockito.mockStatic(UtilUsuario.class);
    //PowerMockito.when(UtilUsuario.verificaPermissao(any(TipoOperacao.class), any(Integer.class))).thenReturn(true);
    
    //Mockando find chamado indiretamente por alguns metodos da RN
//    doAnswer(new Answer<Diff>() {
//      @Override
//      public Diff answer(InvocationOnMock invocation) throws Throwable {
//      	Diff t = new Diff();
//        return t;
//      }
//    }).when(diffDAO).find(any(Diff.class));
    
  }
  
  @Test
  public void shouldFailIfValidContent(){
	  List<Data> data = new ArrayList<Data>();
	  assertFalse(diffDAO.isValidBase64Content(data));
  }
  
  //TODO: Tests failing I don know why
  @Test
  public void shouldFailIfDataWasValid(){
	  boolean asnwer = diffDAO.isValidBase64Content(dataValid);
	  assertTrue(asnwer);
  }
  
  @Test
  public void shouldFailIfDataWasInvalid2(){
	  boolean asnwer = diffDAO.isValidBase64Content(dataValid2);
	  assertTrue(asnwer);
  }
  
  @Test
  public void shouldFailIfDataWasInvalid3(){
	  boolean asnwer = diffDAO.isValidBase64Content(dataValid3);
	  assertTrue(asnwer);
  }
  
  @Test
  public void shouldFailIfValidContent2(){
	  assertFalse(diffDAO.isValidBase64Content(null));
  }
  
  @Test
  public void getNumberOfElementsThatMatchToPattern(){
	  //Mock to static classes using PoweMock
	  PowerMockito.mockStatic(UtilBase64.class);
	  PowerMockito.when(UtilBase64.isValidBase64Encode(any(String.class))).thenReturn(true);
	  assertEquals(new Long(2), diffDAO.getNumberOfElementsThatMatchToPattern(dataValid2));
  }
  
  @Test(expected = InsufficientDataException.class)
  public void shouldLauchInsufficientDataException(){
	  doThrow(new InsufficientDataException(new HashMap<String, String>())).when(diffDAO).findByIdFetchData(any(Long.class));
	  diffDAO.compare(1L);
  }
  
//  @Test	
//  public void deveFalharSeTurmaMatriculaPorDisciplina() {
//    //  create mock
////    TurmaRN turmaRN = Mockito.mock(TurmaRN.class);
//  	 //Mockando find chamado indiretamente por alguns metodos da RN
//    doAnswer(new Answer<Student>() {
//      @Override
//      public Student answer(InvocationOnMock invocation) throws Throwable {
//      	Student t = new Student();
//      	t.setName("Adriano");
//        return t;
//      }
//    }).when(studentDAO).find(any(Student.class));
//
//    // use mock in test....
//    Student student = studentDAO.find(new Student());
//    assertEquals(student.getName(), "Adriano");
//  }
  
//  @Test
//  public void deveFalharSeCursoDiferenteDeNulo() {
//    turmaED.setCursoED(null);
//    try{
//    TurmaQuadroDTO turmaQuadro = new TurmaQuadroDTO();
//    assertEquals(false,turmaRN.isValidaHomologacao(turmaED, turmaQuadro));
//    fail();
//    }catch(RNException rne){
//      assertEquals(rne.getMessage(), "Turma ou Curso da Turma n�o definido.");
//    }
//  }
  
}