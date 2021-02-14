package com.bmeynier.article.vertx.fishs;

import com.bmeynier.article.vertx.fishs.database.FishDatabaseVerticle;
import com.bmeynier.article.vertx.fishs.http.HttpServerVerticle;
import io.vertx.config.ConfigRetriever;
import io.vertx.config.ConfigRetrieverOptions;
import io.vertx.config.ConfigStoreOptions;
import io.vertx.core.*;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.shell.ShellService;
import io.vertx.ext.shell.ShellServiceOptions;
import io.vertx.ext.shell.term.TelnetTermOptions;


public class MainVerticle extends AbstractVerticle {

  @Override
  public void start(Promise<Void> startPromise) throws Exception {

    JsonObject config = vertx.getOrCreateContext().config();

    if (config.isEmpty()) {
      initializeCustomizedConfigLoader();
    } else {
      initalizeVerticle(config);
    }

    initializeShellService();
  }

  private void initializeCustomizedConfigLoader() {
    ConfigStoreOptions conf = new ConfigStoreOptions()
      .setType("file")
      .setOptional(true)
      .setConfig(new JsonObject().put("path", "application.json"));

    ConfigRetriever retriever = ConfigRetriever.create(vertx, new ConfigRetrieverOptions().addStore(conf).setScanPeriod(2000));

    retriever.getConfig(json -> initalizeVerticle(json.result()));
  }

  private void initializeShellService() {
    ShellService service = ShellService.create(vertx,
      new ShellServiceOptions().setTelnetOptions(
        new TelnetTermOptions().
          setHost("localhost").
          setPort(4000)
      )
    );
    service.start();
  }


  private void initalizeVerticle(JsonObject jsonConf) {
    vertx.deployVerticle(FishDatabaseVerticle.class.getName(), new DeploymentOptions().setInstances(2).setConfig(jsonConf))
      .onSuccess(ar -> {
        vertx.deployVerticle(HttpServerVerticle.class.getName(), new DeploymentOptions().setConfig(jsonConf));
      }).onFailure(ar -> {
      System.out.println(ar.getCause().toString());
    });
  }

  public static void main(String... args) {
    Vertx.vertx().deployVerticle(MainVerticle.class.getName());
  }

}

