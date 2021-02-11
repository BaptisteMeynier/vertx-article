#!/usr/bin/env bash

export LAUNCHER="com.bmeynier.article.vertx.fishs.FishLauncher"
export VERTICLE="com.bmeynier.article.vertx.fishs.MainVerticle"
export CMD="mvn compile"
export VERTX_CMD="run"

mvn compile dependency:copy-dependencies
java \
  -cp  $(echo target/dependency/*.jar | tr ' ' ':'):"target/classes" \
  $LAUNCHER $VERTX_CMD $VERTICLE \
  --redeploy="src/main/**/*" --on-redeploy=$CMD \
  --launcher-class=$LAUNCHER \
  --java-opts="-Dhsqldb.reconfig_logging=false" \
  $@
