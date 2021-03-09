#!/bin/bash

OUTPUT_DIR=$3

SCRATCH_DIR=scratch
DEPS_DIR="resources/jar"

EFLECT_JAR="eflect-experiments.jar:../eflect.jar"
DACAPO_JAR="${DEPS_DIR}/dacapo.jar"

mkdir $SCRATCH_DIR

pids=""
for i in `seq 1 1 $2`; do
  java -Deflect.output=$OUTPUT_DIR/$i -cp $EFLECT_JAR:$DACAPO_JAR Harness $1 -c eflect.experiments.EflectCallback --no-validation --scratch-directory=$SCRATCH_DIR/$i &
  pids+=$!" "
done

for pid in $pids; do
  tail --pid=$pid -f /dev/null
done

rm $SCRATCH_DIR -r
