package com.bmeynier.article.vertx.fishs.database;

import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.jdbc.JDBCClient;
import io.vertx.ext.sql.SQLConnection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.stream.Collectors;

public class FishDatabaseServiceImpl implements FishDatabaseService {

  private static final Logger LOGGER = LoggerFactory.getLogger(FishDatabaseServiceImpl.class);

  private final HashMap<SqlQuery, String> sqlQueries;
  private final JDBCClient dbClient;

  FishDatabaseServiceImpl(JDBCClient dbClient, HashMap<SqlQuery, String> sqlQueries, Handler<AsyncResult<FishDatabaseService>> readyHandler) {
    this.dbClient = dbClient;
    this.sqlQueries = sqlQueries;

    dbClient.getConnection(ar -> {
      if (ar.failed()) {
        LOGGER.error("Could not open a database connection", ar.cause());
        readyHandler.handle(Future.failedFuture(ar.cause()));
      } else {
        LOGGER.info("SUCCESS to init connection", ar.cause());
        SQLConnection connection = ar.result();
        connection.execute(sqlQueries.get(SqlQuery.CREATE_FISHS_TABLE), create -> {
          connection.close();
          if (create.failed()) {
            LOGGER.error("Database preparation error", create.cause());
            readyHandler.handle(Future.failedFuture(create.cause()));
          } else {
            LOGGER.info("Final SUCCESS to init connection", ar.cause());
            readyHandler.handle(Future.succeededFuture(this));
          }
        });
      }
    });
  }

  @Override
  public FishDatabaseService fetchAllFishs(Handler<AsyncResult<JsonArray>> resultHandler) {
    LOGGER.error("FETCH ALL");
    dbClient.query(sqlQueries.get(SqlQuery.ALL_FISHS), res -> {
      if (res.succeeded()) {
        JsonArray fishs = new JsonArray(res.result()
          .getResults()
          .stream()
          .sorted((objects, t1) -> t1.getInteger(0))
          .collect(Collectors.toList()));
        resultHandler.handle(Future.succeededFuture(fishs));
      } else {
        LOGGER.error("Database query error", res.cause());
        resultHandler.handle(Future.failedFuture(res.cause()));
      }
    });
    return this;
  }

  @Override
  public FishDatabaseService createFish(String name, Handler<AsyncResult<JsonArray>> resultHandler) {
    JsonArray data = new JsonArray().add(name);

    dbClient.updateWithParams(sqlQueries.get(SqlQuery.CREATE_FISH), data, res -> {
      if (res.succeeded()) {
        resultHandler.handle(Future.succeededFuture());
      } else {
        LOGGER.error("Database query error", res.cause());
        resultHandler.handle(Future.failedFuture(res.cause()));
      }
    });

    return this;
  }

  @Override
  public FishDatabaseService modifyFish(int id, String name, Handler<AsyncResult<JsonArray>> resultHandler) {
    JsonArray data = new JsonArray().add(name).add(id);

    dbClient.updateWithParams(sqlQueries.get(SqlQuery.SAVE_FISH), data, res -> {
      if (res.succeeded()) {
        resultHandler.handle(Future.succeededFuture());
      } else {
        LOGGER.error("Database query error", res.cause());
        resultHandler.handle(Future.failedFuture(res.cause()));
      }
    });

    return this;
  }

  @Override
  public FishDatabaseService deleteFish(String name, Handler<AsyncResult<JsonArray>> resultHandler) {
    JsonArray data = new JsonArray().add(name);
    dbClient.updateWithParams(sqlQueries.get(SqlQuery.DELETE_FISH), data, res -> {
      if (res.succeeded()) {
        resultHandler.handle(Future.succeededFuture());
      } else {
        LOGGER.error("Database query error", res.cause());
        resultHandler.handle(Future.failedFuture(res.cause()));
      }
    });
    return this;
  }

  @Override
  public FishDatabaseService isAvailable(Handler<AsyncResult<JsonObject>> resultHandler) {
    dbClient.getConnection(res -> {
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
