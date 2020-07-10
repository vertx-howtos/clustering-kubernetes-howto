package io.vertx.howtos.cluster;

import io.vertx.core.*;
import io.vertx.core.eventbus.Message;
import io.vertx.ext.cluster.infinispan.ClusterHealthCheck;
import io.vertx.ext.healthchecks.HealthCheckHandler;
import io.vertx.ext.healthchecks.HealthChecks;
import io.vertx.ext.healthchecks.Status;
import io.vertx.ext.web.Router;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class FrontendVerticle extends AbstractVerticle {

  private static final Logger log = LoggerFactory.getLogger(FrontendVerticle.class);

  private static final int HTTP_PORT = Integer.parseInt(System.getenv().getOrDefault("HTTP_PORT", "8080"));

  public static void main(String[] args) {
    Vertx.clusteredVertx(new VertxOptions())
      .compose(vertx -> vertx.deployVerticle(new FrontendVerticle()))
      .onFailure(t -> t.printStackTrace());
  }

  @Override
  public void start() {
    Router router = Router.router(vertx);

    router.get("/hello").handler(rc -> {
      vertx.eventBus().<String>request("greetings", rc.queryParams().get("name"))
        .map(Message::body)
        .onSuccess(reply -> rc.response().end(reply))
        .onFailure(rc::fail);
    });

    router.get("/health").handler(rc -> rc.response().end("OK"));

    Handler<Promise<Status>> procedure = ClusterHealthCheck.createProcedure(vertx, false);
    HealthChecks checks = HealthChecks.create(vertx).register("cluster-health", procedure);
    router.get("/readiness").handler(HealthCheckHandler.createWithHealthChecks(checks));

    vertx.createHttpServer()
      .requestHandler(router)
      .listen(HTTP_PORT)
      .onSuccess(server -> log.info("Server started and listening on port {}", server.actualPort()));
  }
}
