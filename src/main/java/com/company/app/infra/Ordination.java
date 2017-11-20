package com.company.app.infra;

import java.io.Serializable;

/**
 * Contém as propriedades necessárias para montagem dinâmica 
 * dos critérios de ordenação de uma lista (ORDER BY)
 * @author adriano-fonseca
 *
 */
public class Ordination implements Serializable {
	
	private static final long serialVersionUID = 1L;
	
	private String propriedade;	
	private boolean asc = true;
	
	public Ordination() {
	}
	
	public Ordination(String propriedade) {
		this.propriedade = propriedade;
	}
	
	public Ordination(String propriedade, boolean asc) {
		this.propriedade = propriedade;
		this.asc = asc;
	}
	
	public String getPropriedade() {
		return propriedade;
	}
	
	public void setPropriedade(String propriedade) {
	  this.propriedade = propriedade;
	}
	
	public boolean isAsc() {
		return asc;
	}
	
	public void setAsc(boolean asc) {
	  this.asc = asc;
	}
	
}
