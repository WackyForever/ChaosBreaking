#!/bin/sh
set -e
GRADLE_VERSION=8.11
GRADLE_HOME="${HOME}/.gradle/wrapper/dists/gradle-${GRADLE_VERSION}"
DIST="${GRADLE_HOME}/gradle-${GRADLE_VERSION}/bin/gradle"
if [ ! -x "$DIST" ]; then
  mkdir -p "$GRADLE_HOME"
  ZIP="$GRADLE_HOME/gradle.zip"
  curl -fsSL "https://services.gradle.org/distributions/gradle-${GRADLE_VERSION}-bin.zip" -o "$ZIP"
  unzip -q -o "$ZIP" -d "$GRADLE_HOME"
  rm -f "$ZIP"
fi
exec "$DIST" "$@"
