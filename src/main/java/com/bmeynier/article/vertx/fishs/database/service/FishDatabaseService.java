/*
 *  Copyright (c) 2017 Red Hat, Inc. and/or its affiliates.
 *  Copyright (c) 2017 INSA Lyon, CITI Laboratory.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.bmeynier.article.vertx.fishs.database.service;


import io.vertx.codegen.annotations.Fluent;
import io.vertx.codegen.annotations.GenIgnore;
import io.vertx.codegen.annotations.ProxyGen;
import io.vertx.codegen.annotations.VertxGen;
import io.vertx.core.AsyncResult;
import io.vertx.core.Handler;

import io.vertx.core.Vertx;
import io.vertx.core.eventbus.DeliveryOptions;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.jdbcclient.JDBCPool;

import java.util.HashMap;

@VertxGen
@ProxyGen
public interface FishDatabaseService {


  @Fluent
  FishDatabaseService fetchAllFishs(Handler<AsyncResult<JsonArray>> resultHandler);

  @Fluent
  FishDatabaseService createFish(String name, Handler<AsyncResult<JsonObject>> resultHandler);

  @Fluent
  FishDatabaseService modifyFish(int id, String name, Handler<AsyncResult<JsonArray>> resultHandler);

  @Fluent
  FishDatabaseService deleteFish(String name, Handler<AsyncResult<JsonObject>> resultHandler);

  @Fluent
  FishDatabaseService deleteAllFishs(Handler<AsyncResult<JsonObject>> resultHandler);

  @Fluent
  FishDatabaseService existFishById(long id, Handler<AsyncResult<JsonObject>> resultHandler);

  @Fluent
  FishDatabaseService existFishByName(String fishName, Handler<AsyncResult<JsonObject>> resultHandler);

  @Fluent
  FishDatabaseService isAvailable(Handler<AsyncResult<JsonObject>> resultHandler);

  @GenIgnore
  static FishDatabaseService create(JDBCPool jdbcPool, HashMap<SqlQuery, String> sqlQueries, Handler<AsyncResult<FishDatabaseService>> readyHandler) {
    return new FishDatabaseServiceImpl(jdbcPool, sqlQueries, readyHandler);
  }

  @GenIgnore
  static FishDatabaseService createProxy(Vertx vertx, String address, DeliveryOptions options) {
    return new FishDatabaseServiceVertxEBProxy(vertx, address, options);
  }

  @GenIgnore
  static FishDatabaseService createProxy(Vertx vertx, String address) {
    return new FishDatabaseServiceVertxEBProxy(vertx, address);
  }

}
