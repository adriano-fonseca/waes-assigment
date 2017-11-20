package com.company.app.model;

import java.io.Serializable;

import javax.persistence.MappedSuperclass;

import org.codehaus.jackson.annotate.JsonIgnore;


@MappedSuperclass
public abstract class BaseEntity<PK extends Serializable> implements Serializable, Cloneable {

  private static final long serialVersionUID = 1L;
  
  @JsonIgnore
  public abstract PK getIdEntity();

  public BaseEntity() {
    super();
  }

  public BaseEntity(PK id) {
    super();
  }
  
  @Override
  public Object clone() throws CloneNotSupportedException {
    return super.clone();
  }
  
  
}