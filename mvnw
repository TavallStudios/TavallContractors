#!/bin/sh
set -e
BASEDIR=$(cd "$(dirname "$0")" && pwd)
MAVEN_PROJECTBASEDIR="$BASEDIR"
exec java -Dmaven.multiModuleProjectDirectory="$MAVEN_PROJECTBASEDIR" -classpath "$BASEDIR/.mvn/wrapper/maven-wrapper.jar" org.apache.maven.wrapper.MavenWrapperMain "$@"
