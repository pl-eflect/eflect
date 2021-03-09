#!/bin/bash

OUTPUT_DIR=$2

SCRATCH_DIR=scratch
DEPS_DIR="resources/jar"

EFLECT_JAR="eflect-experiments.jar:../eflect.jar"
SUNFLOW_JAR="${DEPS_DIR}/sunflow.jar:${DEPS_DIR}/stokelib.jar:${DEPS_DIR}/guava-20.0.jar"

pids=""
for i in `seq 1 1 $1`; do
  java -Deflect.output=$OUTPUT_DIR/$i -cp $EFLECT_JAR:$SUNFLOW_JAR eflect.experiments.EflectSunflow # &
  exit
  pids+=$!" "
done

for pid in $pids; do
  tail --pid=$pid -f /dev/null
done
