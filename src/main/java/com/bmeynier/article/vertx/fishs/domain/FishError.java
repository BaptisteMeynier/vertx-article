package com.bmeynier.article.vertx.fishs.domain;

import io.vertx.codegen.annotations.DataObject;


@DataObject
public class FishError {
  private String code;
  private String message;

  public String getCode() {
    return code;
  }

  public void setCode(String code) {
    this.code = code;
  }

  public String getMessage() {
    return message;
  }

  public void setMessage(String message) {
    this.message = message;
  }
}
