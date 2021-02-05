package com.bmeynier.article.vertx.fishs.domain;

import io.vertx.codegen.annotations.DataObject;
import io.vertx.sqlclient.templates.annotations.RowMapped;

import java.util.Objects;

@RowMapped
@DataObject
public class Fish {
  private long id;
  private String name;

  public Fish() {
  }

  public Fish(String name) {
    this.name = name;
  }

  public Fish(long id, String name) {
    this.id = id;
    this.name = name;
  }

  public long getId() {
    return id;
  }

  public void setId(long id) {
    this.id = id;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }


  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    Fish fish = (Fish) o;
    return id == fish.id && Objects.equals(name, fish.name);
  }

  @Override
  public int hashCode() {
    return Objects.hash(id, name);
  }

  @Override
  public String toString() {
    return "Fish{" +
      "id=" + id +
      ", name='" + name + '\'' +
      '}';
  }
}
