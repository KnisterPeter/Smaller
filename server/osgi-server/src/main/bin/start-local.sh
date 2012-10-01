#!/bin/sh

CD=$(dirname $0)
cd "$CD/.."
java -jar lib/osgi-kernel-${version}.jar -repository=file:${user.home}/.m2/repository/
