plugins {
  java
  application
  id("com.google.cloud.tools.jib") version "2.4.0"
}

repositories {
  mavenCentral()
}

val vertxVersion = "5.0.0.CR2"
val verticle = "io.vertx.howtos.cluster.BackendVerticle"

dependencies {
  implementation("io.vertx:vertx-launcher-application:${vertxVersion}")
  implementation("io.vertx:vertx-web:${vertxVersion}")
  implementation("io.vertx:vertx-infinispan:${vertxVersion}")
  implementation("io.vertx:vertx-health-check:${vertxVersion}")
  implementation("ch.qos.logback:logback-classic:1.5.12")
}

application {
  applicationDefaultJvmArgs =
    listOf("-Djava.net.preferIPv4Stack=true", "-Dvertx.jgroups.config=default-configs/default-jgroups-udp.xml")
  mainClass = verticle
}

jib {
  to {
    image = "clustering-kubernetes/backend"
  }
  container {
    mainClass = "io.vertx.launcher.application.VertxApplication"
    args = listOf(verticle, "-cluster")
    ports = listOf("8080", "7800")
  }
}
