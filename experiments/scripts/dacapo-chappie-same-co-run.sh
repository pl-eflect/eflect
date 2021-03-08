#!/bin/bash

OUTPUT_DIR=$3

SCRATCH_DIR=scratch
DEPS_DIR="third_party"

EFLECT_JAR="eflect-experiments.jar:../eflect.jar"
DACAPO_JAR="${DEPS_DIR}/dacapo.jar:${DEPS_DIR}/async-profiler/build/async-profiler.jar"

mkdir $SCRATCH_DIR

pids=""
for i in `seq 1 1 $2`; do
  java -Djava.library.path=$PWD/$DEPS_DIR/async-profiler/build -Deflect.output=$OUTPUT_DIR/$i -cp $EFLECT_JAR:$DACAPO_JAR Harness $1 -c eflect.experiments.ChappieEflectCallback --no-validation --scratch-directory=$SCRATCH_DIR/$i &
  pids+=$!" "
done

for pid in $pids; do
  tail --pid=$pid -f /dev/null
done

rm $SCRATCH_DIR -r