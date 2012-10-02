#!/bin/sh

CD=$(dirname $0)
cd "$CD/.."
java -jar lib/osgi-kernel-${version}.jar \
  -repository=http://repo1.maven.org/maven2/ \
  install:lib/osgi-file-${version}.jar
