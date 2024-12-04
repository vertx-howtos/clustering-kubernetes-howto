package io.vertx.howtos.cluster;

import io.vertx.core.*;
import io.vertx.core.http.HttpServer;
import io.vertx.ext.cluster.infinispan.ClusterHealthCheck;
import io.vertx.ext.healthchecks.HealthChecks;
import io.vertx.ext.healthchecks.Status;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.healthchecks.HealthCheckHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class BackendVerticle extends VerticleBase {

  private static final Logger log = LoggerFactory.getLogger(BackendVerticle.class);

  // tag::config[]
  private static final int HTTP_PORT = Integer.parseInt(System.getenv().getOrDefault("HTTP_PORT", "0"));
  private static final String POD_NAME = System.getenv().getOrDefault("POD_NAME", "unknown");
  // end::config[]

  // tag::start[]
  @Override
  public Future<?> start() {
    Future<Void> registration = registerConsumer();

    Router router = setupRouter();

    Future<HttpServer> httpServer = vertx.createHttpServer()
      .requestHandler(router)
      .listen(HTTP_PORT)
      .onSuccess(server -> log.info("Server started and listening on port {}", server.actualPort()));

    return Future.join(registration, httpServer);
  }
  // end::start[]

  // tag::consumer[]
  private Future<Void> registerConsumer() {
    return vertx.eventBus().<String>consumer("greetings", msg -> {
      msg.reply(String.format("Hello %s from %s", msg.body(), POD_NAME));
    }).completion();
  }
  // end::consumer[]

  // tag::router[]
  private Router setupRouter() {
    Router router = Router.router(vertx);

    router.get("/health").handler(rc -> rc.response().end("OK"));

    Handler<Promise<Status>> procedure = ClusterHealthCheck.createProcedure(vertx, false);
    HealthChecks checks = HealthChecks.create(vertx).register("cluster-health", procedure);
    router.get("/readiness").handler(HealthCheckHandler.createWithHealthChecks(checks));
    return router;
  }
  // end::router[]

  // tag::main[]
  public static void main(String[] args) {
    Vertx.clusteredVertx(new VertxOptions())
      .compose(vertx -> vertx.deployVerticle(new BackendVerticle()))
      .await();
  }
  // end::main[]
}
