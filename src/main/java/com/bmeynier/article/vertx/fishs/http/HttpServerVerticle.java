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
import io.vertx.core.*;

import io.vertx.core.eventbus.DeliveryOptions;
import io.vertx.core.json.JsonArray;

import io.vertx.core.tracing.TracingPolicy;
import io.vertx.ext.healthchecks.HealthCheckHandler;
import io.vertx.ext.healthchecks.Status;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.handler.StaticHandler;
import io.vertx.ext.web.openapi.RouterBuilder;
import io.vertx.ext.web.validation.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Objects;


public class HttpServerVerticle extends AbstractVerticle {

  private static final Logger LOGGER = LoggerFactory.getLogger(HttpServerVerticle.class);

  public static final String CONFIG_HTTP_SERVER_PORT = "http.server.port";
  public static final String CONFIG_FISHDB_QUEUE = "bus.db";
  public static final String HEALTH_CONTEXT = "/health*";
  public static final String SWAGGER_CONTEXT = "/swagger-ui*";
  public static final String API_VERSION = "/v1";

  private FishDatabaseService dbService;

  @Override
  public void start(Promise<Void> promise) {
    LOGGER.info("FISH SERVER VERTICLE");

    String fishDbQueue = config().getString(CONFIG_FISHDB_QUEUE, "fishdb.queue");
    int portNumber = config().getInteger(CONFIG_HTTP_SERVER_PORT, 8080);

    DeliveryOptions deliveryOptions = new DeliveryOptions().setTracingPolicy(TracingPolicy.ALWAYS);

    dbService = FishDatabaseService.createProxy(vertx, fishDbQueue, deliveryOptions);

    RouterBuilder.create(vertx, "src/main/resources/webroot/fishStore.yaml").onComplete(ar -> {
      if (ar.succeeded()) {
        Router global = this.getOpenApiRouter(ar.result());

        vertx.createHttpServer()
          .requestHandler(global)
          .listen(portNumber)
          .onSuccess(res -> {
            LOGGER.info("HTTP server running on port " + portNumber);
            promise.complete();
          }).onFailure(err -> {
            LOGGER.error("Could not start a HTTP server", err.getCause());
            promise.fail(err.getCause());
          }
        );

      } else {
        // Something went wrong during router builder initialization
        Throwable exception = ar.cause();
      }
    });
  }

  public void faillureHandler(RoutingContext routingContext) {
    if (routingContext.failure() instanceof BadRequestException) {
      if (routingContext.failure() instanceof ParameterProcessorException) {
        // Something went wrong while parsing/validating a parameter
      } else if (routingContext.failure() instanceof BodyProcessorException) {
        // Something went wrong while parsing/validating the body
      } else if (routingContext.failure() instanceof RequestPredicateException) {
        // A request predicate is unsatisfied
      }
    }
    LOGGER.error("An error occur during request validating");
  }

  private Router getOpenApiRouter(RouterBuilder routerBuilder) {
    routerBuilder
      .operation("listFishs")
      .handler(this::allFishsHandler)
      .failureHandler(this::faillureHandler);

    routerBuilder.operation("createFish")
      .handler(this::fishCreateHandler)
      .failureHandler(this::faillureHandler);

    routerBuilder.operation("modifyFish")
      .handler(this::fishModificationHandler)
      .failureHandler(this::faillureHandler);

    routerBuilder.operation("deleteFishs")
      .handler(this::fishDeleteHandler)
      .failureHandler(this::faillureHandler);

    Router global = Router.router(vertx);

    Router generated = routerBuilder.createRouter();
    global.mountSubRouter(API_VERSION, generated);

    HealthCheckHandler healthCheckHandler = healthHandler();
    global.get(HEALTH_CONTEXT).handler(healthCheckHandler);
    global.get(SWAGGER_CONTEXT).handler(StaticHandler.create());

    return global;
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
        context.response().end(name + " fish was created.");
      } else {
        context.fail(reply.cause());
      }
    });

  }

  private void fishDeleteHandler(RoutingContext context) {
    String name = context.request().getParam("name");

    if (Objects.isNull(name)) {
      dbService.deleteAllFishs(reply -> {
        if (reply.succeeded()) {
          context.end("Delete all fishs ");
        } else {
          context.fail(reply.cause());
        }
      });
    } else {
      dbService.deleteFish(name, reply -> {
        if (reply.succeeded()) {
          context.end("Delete " + name);
        } else {
          context.fail(reply.cause());
        }
      });
    }
  }


}


