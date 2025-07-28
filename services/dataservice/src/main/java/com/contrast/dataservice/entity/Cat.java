package com.contrast.dataservice.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.JoinColumn;

@Entity
public class Cat {

  @Id
  @GeneratedValue(strategy = GenerationType.AUTO)
  private long id;

  @ManyToOne
  @JoinColumn(name = "obj_owner")
  private User objOwner;

  private String name;
  private String type;
  private String image;

  public long getId() {
    return id;
  }

  public void setId(long id) {
    this.id = id;
  }

  public User getObjOwner() {
    return objOwner;
  }

  public void setObjOwner(User objOwner) {
    this.objOwner = objOwner;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public String getType() {
    return type;
  }

  public void setType(String type) {
    this.type = type;
  }

  public String getImage() {
    return image;
  }

  public void setImage(String image) {
    this.image = image;
  }
}
