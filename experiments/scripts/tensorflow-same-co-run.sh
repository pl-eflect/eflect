#!/bin/bash

OUTPUT_DIR=$1

DEPS_DIR="resources/jar"

EFLECT_JAR="eflect-experiments.jar:../eflect.jar"
TENSORFLOW_JAR=$DEPS_DIR/libtensorflow.jar
MODEL=$PWD/"resources/inception_v3_2016_08_28_frozen.pb"
IMAGE=$PWD/"resources/test_image.jpeg"

TENSORFLOW_SO=$PWD/$DEPS_DIR/libtensorflow_jni.so

pids=""
for i in `seq 1 1 $2`; do
  java -Dtf.lib=$TENSORFLOW_SO -Deflect.output=$OUTPUT_DIR/$i -cp $EFLECT_JAR:$TENSORFLOW_JAR eflect.experiments.InceptionDriver $MODEL $IMAGE &
  pids+=$!" "
done

for pid in $pids; do
  tail --pid=$pid -f /dev/null
done
