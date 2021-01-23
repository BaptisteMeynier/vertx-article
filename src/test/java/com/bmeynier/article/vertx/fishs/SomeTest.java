package com.bmeynier.article.vertx.fishs;

import com.bmeynier.article.vertx.fishs.http.HttpServerVerticle;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.Promise;
import io.vertx.core.Vertx;
import io.vertx.core.VertxOptions;
import io.vertx.ext.web.client.WebClient;
import io.vertx.ext.web.codec.BodyCodec;
import io.vertx.junit5.Checkpoint;
import io.vertx.junit5.VertxExtension;
import io.vertx.junit5.VertxTestContext;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;
@ExtendWith(VertxExtension.class)
class SomeTest {

  // Deploy the verticle and execute the test methods when the verticle is successfully deployed
 /* @BeforeEach
  void deploy_verticle(Vertx vertx, VertxTestContext testContext) {
    vertx.createHttpServer()
      .requestHandler(req -> {
        req.response()
          .putHeader("Content-Type", "plain/text")
          .end("Yo!");
        logger.info("Handled a request on path {} from {}", req.path(), req.remoteAddress().host());
      })
      .listen(11981, ar -> {
        if (ar.succeeded()) {
          startPromise.complete();
        } else {
          startPromise.fail(ar.cause());
        }
      });
  }
*/
  // Repeat this test 3 times
  //@RepeatedTest(2)
  @Test
  void http_server_check_response(Vertx vertx, VertxTestContext testContext) {
    WebClient client = WebClient.create(vertx);
    client.get(8080, "localhost", "/")
      .as(BodyCodec.string())
      .send(testContext.succeeding(response -> testContext.verify(() -> {
        assertThat(response.body()).isEqualTo("Ok");
        testContext.completeNow();
      })));
  }

  @Test
  void http_server_check_response2(Vertx vertx, VertxTestContext testContext) {
    vertx.createHttpServer()
      .requestHandler(req -> {
        req.response().end("Ok");
      })
      .listen(8080, ar -> {
        if (ar.failed()) {
          testContext.failNow(ar.cause());
        } else {
          testContext.succeeding(id -> testContext.completeNow());
        }
      });


        WebClient client = WebClient.create(vertx);

    client.get(8080, "localhost", "/")
      .as(BodyCodec.string())
      .send(testContext.succeeding(response -> testContext.verify(() -> {
        assertThat(response.body()).isEqualTo("Ok");
        testContext.completeNow();
      })));
  }


  public class SampleVerticle extends AbstractVerticle {

    private final Logger logger = LoggerFactory.getLogger(SampleVerticle.class);

    @Override
    public void start(Promise<Void> startPromise) {
      vertx.createHttpServer()
        .requestHandler(req -> {
          req.response()
            .putHeader("Content-Type", "plain/text")
            .end("A FISH !");
          logger.info("Handled a request on path {} from {}", req.path(), req.remoteAddress().host());
        })
        .listen(11981, ar -> {
          if (ar.succeeded()) {
            startPromise.complete();
          } else {
            startPromise.fail(ar.cause());
          }
        });
    }
  }


  @Test
  void http_server_check_response3(Vertx vertx, VertxTestContext testContext) {
    WebClient webClient = WebClient.create(vertx);
   // Checkpoint deploymentCheckpoint = testContext.checkpoint();
  //  Checkpoint requestCheckpoint = testContext.checkpoint(10);

    vertx.deployVerticle(new SampleVerticle(), testContext.succeeding(id -> {
    //  deploymentCheckpoint.flag();

      for (int i = 0; i < 10; i++) {
        webClient.get(11981, "localhost", "/")
          .as(BodyCodec.string())
          .send(testContext.succeeding(resp -> {
            testContext.verify(() -> {
              assertThat(resp.statusCode()).isEqualTo(200);
              assertThat(resp.body()).contains("Yo!");
          //    requestCheckpoint.flag();
            });
          }));
      }
    }));
  }



  @DisplayName("➡️ A nested test with customized lifecycle")
  @Nested
  class CustomLifecycleTest {

    Vertx vertx;

    @BeforeEach
    void prepare() {
      vertx = Vertx.vertx(new VertxOptions()
        .setMaxEventLoopExecuteTime(1000)
        .setPreferNativeTransport(true));
    }

    @Test
    @DisplayName("⬆️ Deploy SampleVerticle")
    void deploySampleVerticle(VertxTestContext testContext) {
      vertx.deployVerticle(new SampleVerticle(), testContext.succeeding(id -> testContext.completeNow()));
    }

    @Test
    @DisplayName("🛂 Make a HTTP client request to SampleVerticle")
    void httpRequest(VertxTestContext testContext) {
      WebClient webClient = WebClient.create(vertx);
      vertx.deployVerticle(new SampleVerticle(), testContext.succeeding(id -> {
        webClient.get(11981, "localhost", "/yo")
          .as(BodyCodec.string())
          .send(testContext.succeeding(resp -> {
            testContext.verify(() -> {
              assertThat(resp.statusCode()).isEqualTo(200);
              assertThat(resp.body()).contains("Yo!");
              testContext.completeNow();
            });
          }));
      }));
    }

    @AfterEach
    void cleanup() {
      vertx.close();
    }
  }

}
