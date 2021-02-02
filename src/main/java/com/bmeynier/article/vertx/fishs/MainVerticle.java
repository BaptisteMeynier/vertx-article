package com.bmeynier.article.vertx.fishs;

import com.bmeynier.article.vertx.fishs.database.FishDatabaseVerticle;
import com.bmeynier.article.vertx.fishs.http.HttpServerVerticle;
import io.vertx.core.*;
import io.vertx.micrometer.MicrometerMetricsOptions;
import io.vertx.micrometer.VertxPrometheusOptions;

public class MainVerticle extends AbstractVerticle {

  @Override
  public void start(Promise<Void> startPromise) throws Exception {

    vertx.deployVerticle(FishDatabaseVerticle.class.getName(), new DeploymentOptions().setInstances(2))
      .onSuccess(ar ->{
        vertx.deployVerticle(HttpServerVerticle.class.getName());
      }).onFailure(ar->{
      System.out.println(ar.getCause().toString());
    });


   /* ShellService service = ShellService.create(vertx,
      new ShellServiceOptions().setTelnetOptions(
        new TelnetTermOptions().
          setHost("localhost").
          setPort(4000)
      )
    );
    service.start();*/
  }

  public static void main(String... args) {

    MicrometerMetricsOptions metricsOptions = new MicrometerMetricsOptions()
      .setPrometheusOptions(new VertxPrometheusOptions().setEnabled(true))
      .setEnabled(true);

    Vertx.vertx(new VertxOptions().setMetricsOptions(metricsOptions)).deployVerticle(MainVerticle.class.getName());
  }

}
