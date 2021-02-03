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

    ConfigStoreOptions conf = new ConfigStoreOptions()
      .setType("file")
      .setConfig(new JsonObject().put("path", "application.json"));

    ConfigRetriever retriever = ConfigRetriever.create(vertx, new ConfigRetrieverOptions().addStore(conf));

    retriever.getConfig(json -> {
      JsonObject jsonConf = json.result();
      vertx.deployVerticle(FishDatabaseVerticle.class.getName(), new DeploymentOptions().setInstances(2).setConfig(jsonConf))
        .onSuccess(ar -> {
          vertx.deployVerticle(HttpServerVerticle.class.getName(), new DeploymentOptions().setConfig(jsonConf));
        }).onFailure(ar -> {
        System.out.println(ar.getCause().toString());
      });
    });

    ShellService service = ShellService.create(vertx,
      new ShellServiceOptions().setTelnetOptions(
        new TelnetTermOptions().
          setHost("localhost").
          setPort(4000)
      )
    );
    service.start();
  }

  public static void main(String... args) {
    Vertx.vertx().deployVerticle(MainVerticle.class.getName());
  }


}
