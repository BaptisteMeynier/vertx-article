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

package com.bmeynier.article.vertx.fishs.http;


import com.bmeynier.article.vertx.fishs.database.service.FishDatabaseService;
import io.micrometer.prometheus.PrometheusMeterRegistry;
import io.vertx.config.ConfigStoreOptions;
import io.vertx.core.*;

import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.healthchecks.HealthCheckHandler;
import io.vertx.ext.healthchecks.Status;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.handler.BodyHandler;
import io.vertx.micrometer.MicrometerMetricsOptions;
import io.vertx.micrometer.VertxPrometheusOptions;
import io.vertx.micrometer.backends.BackendRegistries;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class HttpServerVerticle extends AbstractVerticle {

  private static final Logger LOGGER = LoggerFactory.getLogger(HttpServerVerticle.class);

  public static final String CONFIG_HTTP_SERVER_PORT = "http.server.port";
  public static final String CONFIG_FISHDB_QUEUE = "bus.db";
  public static final String HEALTH_CONTEXT = "/health*";
  public static final String METRICS_CONTEXT = "/metrics*";
  private static final String FISH_CONTEXT = "/fish";

  private FishDatabaseService dbService;

  @Override
  public void start(Promise<Void> promise) {
    LOGGER.info("FISH SERVER VERTICLE");

    String fishDbQueue = config().getString(CONFIG_FISHDB_QUEUE, "fishdb.queue");
    int portNumber = config().getInteger(CONFIG_HTTP_SERVER_PORT, 8084);

    dbService = FishDatabaseService.createProxy(vertx, fishDbQueue);

    vertx.createHttpServer()
      .requestHandler(getRouter())
      .listen(portNumber)
      .onSuccess(res -> {
        LOGGER.info("HTTP server running on port " + portNumber);
        promise.complete();
      }).onFailure(ar -> {
        LOGGER.error("Could not start a HTTP server", ar.getCause());
        promise.fail(ar.getCause());
      }
    );
  }

  private Router getRouter() {

    HealthCheckHandler healthCheckHandler = healthHandler();

    Router router = Router.router(vertx);
    router.get(FISH_CONTEXT).handler(this::allFishsHandler);
    router.post(FISH_CONTEXT).handler(BodyHandler.create());
    router.post(FISH_CONTEXT).handler(this::fishCreateHandler);
    router.put(FISH_CONTEXT).handler(this::fishModificationHandler);
    router.delete(FISH_CONTEXT).handler(this::fishDeleteHandler);
    router.get(HEALTH_CONTEXT).handler(healthCheckHandler);

    return router;
  }

  private HealthCheckHandler healthHandler() {
    HealthCheckHandler healthCheckHandler = HealthCheckHandler.create(vertx);
    healthCheckHandler.register("check-database", promise -> {
      dbService.isAvailable(res -> {
        promise.complete(res.succeeded() ? Status.OK() : Status.KO());
      });
    });
    return healthCheckHandler;
  }


  private void fishModificationHandler(RoutingContext routingContext) {
    int id = Integer.parseInt(routingContext.request().getParam("id"));
    String name = routingContext.request().getParam("name");

    dbService.modifyFish(id, name, reply -> {
      if (reply.succeeded()) {
        routingContext.end("The name of the Fish number " + id + " was renamed by " + name);
      } else {
        routingContext.fail(reply.cause());
      }
    });
  }


  private void allFishsHandler(RoutingContext context) {
    dbService.fetchAllFishs(reply -> {
      if (reply.succeeded()) {
        JsonArray body = new JsonArray(reply.result().getList());
        context.response().end(body.toBuffer());

      } else {
        context.fail(reply.cause());
      }
    });
  }

  private void fishCreateHandler(RoutingContext context) {
    String name = context.request().getParam("name");

    dbService.createFish(name, reply -> {
      if (reply.succeeded()) {
        context.response().end(  name + " fish was created.");
      } else {
        context.fail(reply.cause());
      }
    });

  }

  private void fishDeleteHandler(RoutingContext context) {
    String name = context.request().getParam("name");

    dbService.deleteFish(name, reply -> {
      if (reply.succeeded()) {
        context.end("Delete " + name);
      } else {
        context.fail(reply.cause());
      }
    });

  }


}


