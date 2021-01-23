package com.bmeynier.article.vertx.fishs.database;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.Promise;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.jdbc.JDBCClient;
import io.vertx.serviceproxy.ServiceBinder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Properties;

public class FishDatabaseVerticle extends AbstractVerticle {

  private static final Logger LOGGER = LoggerFactory.getLogger(FishDatabaseVerticle.class);
  public static final String CONFIG_FISHDB_JDBC_URL = "fishdb.jdbc.url";
  public static final String CONFIG_FISHDB_JDBC_DRIVER_CLASS = "fishdb.jdbc.driver_class";
  public static final String CONFIG_FISHDB_JDBC_MAX_POOL_SIZE = "fishdb.jdbc.max_pool_size";
  public static final String CONFIG_FISHDB_SQL_QUERIES_RESOURCE_FILE = "fishdb.sqlqueries.resource.file";
  public static final String CONFIG_FISHDB_QUEUE = "fishdb.queue";

  @Override
  public void start(Promise<Void> promise) throws Exception {
    LOGGER.info("FISH DATABSE VERTICLE");
    HashMap<SqlQuery, String> sqlQueries = loadSqlQueries();

    JDBCClient dbClient = JDBCClient.createShared(vertx, new JsonObject()
      .put("url", config().getString(CONFIG_FISHDB_JDBC_URL, "jdbc:hsqldb:file:db/fish"))
      .put("driver_class", config().getString(CONFIG_FISHDB_JDBC_DRIVER_CLASS, "org.hsqldb.jdbcDriver"))
      .put("max_pool_size", config().getInteger(CONFIG_FISHDB_JDBC_MAX_POOL_SIZE, 30)));

    FishDatabaseService.create(dbClient, sqlQueries, ready -> {
      if (ready.succeeded()) {
        ServiceBinder binder = new ServiceBinder(vertx);
        binder
          .setAddress(CONFIG_FISHDB_QUEUE)
          .register(FishDatabaseService.class, ready.result());
        promise.complete();
      } else {
        promise.fail(ready.cause());
      }
    });


  }

  /*
   * Note: this uses blocking APIs, but data is small...
   */
  private HashMap<SqlQuery, String> loadSqlQueries() throws IOException {

    String queriesFile = config().getString(CONFIG_FISHDB_SQL_QUERIES_RESOURCE_FILE);
    InputStream queriesInputStream;
    if (queriesFile != null) {
      queriesInputStream = new FileInputStream(queriesFile);
    } else {
      queriesInputStream = getClass().getResourceAsStream("/db-queries.properties");
    }

    Properties queriesProps = new Properties();
    queriesProps.load(queriesInputStream);
    queriesInputStream.close();

    HashMap<SqlQuery, String> sqlQueries = new HashMap<>();
    sqlQueries.put(SqlQuery.CREATE_FISHS_TABLE, queriesProps.getProperty("create-fishs-table"));
   sqlQueries.put(SqlQuery.ALL_FISHS, queriesProps.getProperty("all-fishs"));
    sqlQueries.put(SqlQuery.CREATE_FISH, queriesProps.getProperty("create-fish"));
    sqlQueries.put(SqlQuery.SAVE_FISH, queriesProps.getProperty("save-fish"));
    sqlQueries.put(SqlQuery.DELETE_FISH, queriesProps.getProperty("delete-fish"));
    return sqlQueries;
  }
}
