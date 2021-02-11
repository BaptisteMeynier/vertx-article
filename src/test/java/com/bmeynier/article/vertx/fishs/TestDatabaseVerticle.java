package com.bmeynier.article.vertx.fishs;

import com.bmeynier.article.vertx.fishs.database.FishDatabaseVerticle;
import com.bmeynier.article.vertx.fishs.database.service.FishDatabaseService;
import io.vertx.core.Future;
import io.vertx.core.Promise;
import io.vertx.core.Vertx;
import io.vertx.junit5.VertxExtension;
import io.vertx.junit5.VertxTestContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;


import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("Test database interactions")
@ExtendWith(VertxExtension.class)
public class TestDatabaseVerticle {

  FishDatabaseService dbService;

  @BeforeEach
  void deploy_verticle(Vertx vertx, VertxTestContext testContext) {
    vertx.deployVerticle(new FishDatabaseVerticle(), res -> {
      dbService = FishDatabaseService.createProxy(vertx, "fishdb.queue");
      dbService.deleteAllFishs(testContext.succeeding(id -> testContext.completeNow()));
    });
  }


  @Test
  void it_should_insert_fish(VertxTestContext testContext) {
    String fish = "Discus";
    dbService.createFish(fish, res -> {
      assertThat(res.succeeded()).isTrue();
      testContext.completeNow();
    });
  }

  @Test
  void it_should_insert_delete_fish(VertxTestContext testContext) {
    String fish = "Discus";
    createFish(fish).result();

    dbService.deleteFish(fish, res -> {
      assertThat(res.succeeded()).isTrue();
      testContext.completeNow();
    });
  }

  @Test
  void it_should_detect_existing_fish(VertxTestContext testContext) {
    String fish = "Discus";
    createFish(fish).result();

    dbService.existFishByName(fish, res -> {
      if (res.succeeded()) {
        assertThat(res.result().getBoolean("exist")).isTrue();
      }
      testContext.completeNow();
    });
  }


  @Test
  void it_should_not_existing_fish(VertxTestContext testContext) {
    String fish = "Unknown";

    dbService.existFishByName(fish, res -> {
      if (res.succeeded()) {
        assertThat(res.result().getBoolean("exist")).isFalse();
      }
      testContext.completeNow();
    });
  }


  @Test
  void it_should_get_existing_fish(VertxTestContext testContext) {
    String fish = "Discus";

    createFish(fish).onSuccess(nothing ->
      dbService.fetchAllFishs(res -> {
      if (res.succeeded()) {
        assertThat(res.result().encode()).contains("Discus");
      }
      testContext.completeNow();
    }));
  }


  private Future createFish(String name) {
    Promise promise = Promise.promise();
    dbService.createFish(name, res -> {
      if (res.succeeded()) {
        promise.complete();
      }
    });
    return promise.future();
  }

  private Future deleteAllFish() {
    Promise promise = Promise.promise();
    dbService.deleteAllFishs(res -> {
      if (res.succeeded()) {
        promise.complete();
      }
    });
    return promise.future();
  }

}
