#!/bin/bash
JAVA_OPTS="-Dfile.encoding=utf-8 -Djava.awt.headless=false -Djava.library.path=$(dirname "$0")"
start javaw $JAVA_OPTS -jar "$(dirname "$0")/*.jar"