#!/bin/sh

CD=$(dirname $0)
cd "$CD/.."
java \
  -Dorg.apache.felix.host=127.0.0.1 \
  -Dorg.osgi.service.http.port=1148 \
  -jar lib/osgi-kernel-${version}.jar \
  -repository=file:${user.home}/.m2/repository/ \
  install:lib/osgi-file-${version}.jar
