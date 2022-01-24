#!/bin/bash

OUTPUT_DIR=$1
DEPS_DIR="resources/jar"

EFLECT_JAR="eflect-experiments.jar:../eflect.jar"
DACAPO_JAR="${DEPS_DIR}/dacapo.jar"

BENCHMARK=sunflow
java -Deflect.output=$OUTPUT_DIR -cp $EFLECT_JAR:$DACAPO_JAR Harness $BENCHMARK -c eflect.experiments.EflectCallback --no-validation --scratch-directory=$OUTPUT_DIR/scratch
rm -rf $OUTPUT_DIR/scratch
