#!/bin/bash

OUTPUT_DIR=$3
ITERS=$2
SLA=$1
DACAPO_WORKLOAD=$4

SCRATCH_DIR=scratch
DEPS_DIR="resources/jar"

EFLECT_JAR="eflect-experiments.jar:../eflect.jar"
SUNFLOW_JAR="${DEPS_DIR}/sunflow.jar:${DEPS_DIR}/stokelib.jar:${DEPS_DIR}/guava-20.0.jar"
DACAPO_JAR="${DEPS_DIR}/dacapo.jar"

java -cp $DACAPO_JAR Harness $DACAPO_WORKLOAD --no-validation --scratch-directory=$SCRATCH_DIR &
pids+=$!" "

sleep 1m

pids=""
java -Deflect.output=$OUTPUT_DIR/1 -cp $EFLECT_JAR:$SUNFLOW_JAR eflect.experiments.EflectSunflow $SLA $ITERS &
pids+=$!" "

for pid in $pids; do
  tail --pid=$pid -f /dev/null
done

rm $SCRATCH_DIR -r
rm stoke.log
