#!/bin/bash

OUTPUT_DIR=$1
ITERS=$2

SCRATCH_DIR=scratch
DEPS_DIR="resources/jar"

EFLECT_JAR="eflect-experiments.jar:../eflect.jar"
SUNFLOW_JAR="${DEPS_DIR}/sunflow.jar:${DEPS_DIR}/stokelib.jar:${DEPS_DIR}/guava-20.0.jar"
DACAPO_JAR="${DEPS_DIR}/dacapo.jar"

pids=""
java -Deflect.output=$OUTPUT_DIR -cp $EFLECT_JAR:$SUNFLOW_JAR eflect.experiments.EflectSunflow $ITERS &
pids+=$!" "

java -cp $DACAPO_JAR Harness $1 --no-validation --scratch-directory=$SCRATCH_DIR &
pids+=$!" "

for pid in $pids; do
  tail --pid=$pid -f /dev/null
done

rm $SCRATCH_DIR -r
rm stoke.log
