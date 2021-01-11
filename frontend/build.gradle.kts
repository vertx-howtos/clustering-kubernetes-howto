plugins {
  java
  application
  id("com.google.cloud.tools.jib") version "2.4.0"
}

repositories {
  mavenCentral()
}

val vertxVersion = "4.0.0"
val verticle = "io.vertx.howtos.cluster.FrontendVerticle"

dependencies {
  implementation("io.vertx:vertx-web:${vertxVersion}")
  implementation("io.vertx:vertx-infinispan:${vertxVersion}")
  implementation("io.vertx:vertx-health-check:${vertxVersion}")
  implementation("ch.qos.logback:logback-classic:1.2.3")
}

application {
  mainClassName = verticle
}

jib {
  to {
    image = "clustering-kubernetes/frontend"
  }
  container {
    mainClass = "io.vertx.core.Launcher"
    args = listOf("run", verticle, "-cluster")
    ports = listOf("8080", "7800")
  }
}

tasks.wrapper {
  gradleVersion = "5.2"
}
