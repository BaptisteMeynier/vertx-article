package com.bmeynier.article.vertx.fishs.database.service;

import com.bmeynier.article.vertx.fishs.domain.Fish;
import com.bmeynier.article.vertx.fishs.domain.FishRowMapper;
import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.jdbcclient.JDBCPool;
import io.vertx.sqlclient.templates.SqlTemplate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class FishDatabaseServiceImpl implements FishDatabaseService {

  private static final Logger LOGGER = LoggerFactory.getLogger(FishDatabaseServiceImpl.class);

  private final HashMap<SqlQuery, String> sqlQueries;
  private final JDBCPool jdbcPool;

  FishDatabaseServiceImpl(JDBCPool jdbcPool, HashMap<SqlQuery, String> sqlQueries, Handler<AsyncResult<FishDatabaseService>> readyHandler) {
    this.jdbcPool = jdbcPool;
    this.sqlQueries = sqlQueries;

    jdbcPool.query(sqlQueries.get(SqlQuery.CREATE_FISHS_TABLE))
      .execute()
      .onSuccess(res -> readyHandler.handle(Future.succeededFuture(this)))
      .onFailure(res -> {
        LOGGER.error("Database preparation error", res.getCause());
        readyHandler.handle(Future.failedFuture(res.getCause()));
      });
  }

  @Override
  public FishDatabaseService fetchAllFishs(Handler<AsyncResult<JsonArray>> resultHandler) {
    LOGGER.error("FETCH ALL");
    SqlTemplate.forQuery(jdbcPool, sqlQueries.get(SqlQuery.ALL_FISHS))
      .mapTo(FishRowMapper.INSTANCE)
      .execute(Collections.emptyMap())
      .onSuccess(rows -> {
        JsonArray fishs = new JsonArray();
        for (Fish aFish : rows) {
          fishs.add(JsonObject.mapFrom(aFish));
        }
        resultHandler.handle(Future.succeededFuture(fishs));
      }).onFailure(res -> {
      LOGGER.error("Database query error", res.getCause());
      resultHandler.handle(Future.failedFuture(res.getCause()));
    });
    return this;
  }

  @Override
  public FishDatabaseService createFish(String name, Handler<AsyncResult<JsonObject>> resultHandler) {
    Fish fish = new Fish(name);

    SqlTemplate.forUpdate(jdbcPool, sqlQueries.get(SqlQuery.CREATE_FISH))
      .mapFrom(Fish.class)
      .execute(fish)
      .onSuccess(
        row -> resultHandler.handle(Future.succeededFuture()))
      .onFailure(res -> {
        LOGGER.error("Database query error", res.getCause());
        resultHandler.handle(Future.failedFuture(res.getCause()));
      });
    return this;
  }

  @Override
  public FishDatabaseService modifyFish(int id, String name, Handler<AsyncResult<JsonArray>> resultHandler) {
    Fish fish = new Fish(id, name);

    SqlTemplate.forUpdate(jdbcPool, sqlQueries.get(SqlQuery.SAVE_FISH))
      .mapFrom(Fish.class)
      .execute(fish)
      .onSuccess(res -> resultHandler.handle(Future.succeededFuture()))
      .onFailure(res -> {
        LOGGER.error("Database query error", res.getCause());
        resultHandler.handle(Future.failedFuture(res.getCause()));
      });
    return this;
  }

  @Override
  public FishDatabaseService deleteFish(String name, Handler<AsyncResult<JsonObject>> resultHandler) {
    SqlTemplate.forUpdate(jdbcPool, sqlQueries.get(SqlQuery.DELETE_FISH))
      .execute(Map.of("name", name))
      .onSuccess(res -> {
        resultHandler.handle(Future.succeededFuture());
      }).onFailure(res -> {
      LOGGER.error("Database query error", res.getCause());
      resultHandler.handle(Future.failedFuture(res.getCause()));
    });
    return this;
  }

  @Override
  public FishDatabaseService isAvailable(Handler<AsyncResult<JsonObject>> resultHandler) {
    jdbcPool.getConnection(res -> {
      if (res.succeeded()) {
        resultHandler.handle(Future.succeededFuture());
      } else {
        LOGGER.error("Database query error", res.cause());
        resultHandler.handle(Future.failedFuture(res.cause()));
      }
    });
    return this;
  }
}
