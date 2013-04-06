#!/bin/sh

CD=$(dirname $0)
cd "$CD/.."
java \
  -Dorg.apache.felix.host=127.0.0.1 \
  -Dorg.osgi.service.http.port=1148 \
  -jar lib/osgi-kernel-${osgi-kernel-version}.jar \
  -repository=http://repo1.maven.org/maven2/ \
  install:lib/osgi-file-${osgi-file-version}.jar
