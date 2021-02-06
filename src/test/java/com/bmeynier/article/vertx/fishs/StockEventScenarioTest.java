package com.bmeynier.article.vertx.fishs;

import com.bmeynier.article.vertx.fishs.database.FishDatabaseVerticle;
import com.bmeynier.article.vertx.fishs.domain.Fish;
import com.bmeynier.article.vertx.fishs.http.HttpServerVerticle;
import io.gatling.app.cli.StatusCode;
import io.vertx.core.Vertx;
import io.vertx.ext.web.client.WebClient;
import io.vertx.ext.web.codec.BodyCodec;
import io.vertx.junit5.VertxExtension;
import io.vertx.junit5.VertxTestContext;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("Scenario with multiple stock events")
@ExtendWith(VertxExtension.class)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class StockEventScenarioTest {

  public static final int PORT = 8080;
  public static final String HOST = "localhost";
  public static final String APPLICATION_CONTEXT = "/fish";

  @BeforeEach
  void deploy_verticle(Vertx vertx, VertxTestContext testContext) {
    vertx.deployVerticle(new FishDatabaseVerticle())
      .onSuccess(res -> vertx.deployVerticle(new HttpServerVerticle(), testContext.succeeding(id -> testContext.completeNow())))
      .onFailure(res -> System.out.println(res.getCause()));
  }

  @Order(1)
  @Test
  void it_should_remove_all_data(Vertx vertx, VertxTestContext testContext) {

    WebClient.create(vertx).delete(PORT, HOST, APPLICATION_CONTEXT)
      .as(BodyCodec.string())
      .send(testContext.succeeding(response -> testContext.verify(() -> {
        assertThat(response.statusCode()).isEqualTo(200);
        testContext.completeNow();
      })));
  }

  @Order(2)
  @RepeatedTest(3)
  void it_should_not_get_fish_when_data_base_is_empty(Vertx vertx, VertxTestContext testContext) {
    WebClient client = WebClient.create(vertx);
    client.get(PORT, HOST, APPLICATION_CONTEXT)
      .as(BodyCodec.string())
      .send(testContext.succeeding(response -> testContext.verify(() -> {
        assertThat(response.body()).isEqualTo("[]");
        testContext.completeNow();
      })));
  }


  @Order(3)
  @Test
  void it_should_insert_fish(Vertx vertx, VertxTestContext testContext) {
    String fishName = "Scalare";
    WebClient client = WebClient.create(vertx);
    client.post(PORT, HOST, APPLICATION_CONTEXT + "?name=" + fishName)
      .as(BodyCodec.string())
      .send(testContext.succeeding(response -> testContext.verify(() -> {
        assertThat(response.body()).isNotBlank();
        testContext.completeNow();
      })));
  }

  @RepeatedTest(2)
  @Order(4)
  void it_should_not_insert_fish_with_same_name(Vertx vertx, VertxTestContext testContext) {
    String fishName = "Discus";
    WebClient client = WebClient.create(vertx);
    client.post(PORT, HOST, APPLICATION_CONTEXT  + "?name=" + fishName)
      .as(BodyCodec.string())
      .send(testContext.succeeding(response -> testContext.verify(() -> {
        assertThat(response.statusCode()).isNotEqualTo(200);
        testContext.completeNow();
      })));
  }

  @Order(5)
  @Test
  void it_should_find_fishs(Vertx vertx, VertxTestContext testContext) {
    //GIVEN
    WebClient client = WebClient.create(vertx);
    client.get(PORT, HOST, APPLICATION_CONTEXT)
      .as(BodyCodec.string())
      .send(testContext.succeeding(response -> testContext.verify(() -> {
        assertThat(response.body()).isNotBlank();
        testContext.completeNow();
      })));
  }

  @Order(6)
  @Test
  void it_should_delete_fish_by_name(Vertx vertx, VertxTestContext testContext) {
    WebClient client = WebClient.create(vertx);
    client.delete(PORT, HOST, "/fish?name=Scalare")
      .as(BodyCodec.string())
      .send(testContext.succeeding(response -> testContext.verify(() -> {
        assertThat(response.body()).isNotBlank();
        testContext.completeNow();
      })));
  }

}
