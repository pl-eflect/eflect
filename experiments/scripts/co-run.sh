#!/bin/bash

OUTPUT_DIR=$3

SCRATCH_DIR=scratch
DEPS_DIR="third_party"

EFLECT_JAR="eflect-experiments.jar:../eflect.jar"
DACAPO_JAR="${DEPS_DIR}/dacapo.jar"

mkdir $SCRATCH_DIR

pids=""
for cmd in $@; do
  $cmd
  pids+=$!" "
done

for pid in $pids; do
  tail --pid=$pid -f /dev/null
done

rm $SCRATCH_DIR -r
