#!/bin/bash

OUTPUT_DIR=$1

# same benchmark experiments
ITERS=$2

SIZE=default
BENCHMARKS=(biojava jython xalan)

for CORUNS in `seq 2 1 5`; do
  for BENCHMARK in ${BENCHMARKS[@]}; do
    ./scripts/dacapo-same-co-run.sh "$BENCHMARK -s $SIZE -n $ITERS" $CORUNS $OUTPUT_DIR/same/$CORUNS/$BENCHMARK
  done
done

SIZE=large
BENCHMARKS=(avrora batik eclipse h2 pmd sunflow)

for CORUNS in `seq 2 1 5`; do
  for BENCHMARK in ${BENCHMARKS[@]}; do
    ./scripts/dacapo-same-co-run.sh "$BENCHMARK -s $SIZE -n $ITERS" $CORUNS $OUTPUT_DIR/same/$CORUNS/$BENCHMARK
  done
done

for CORUNS in `seq 2 1 5`; do
  ./scripts/tensorflow-same-co-run.sh $OUTPUT_DIR/same/$CORUNS/tensorflow $CORUNS
done

###############################################
# mixed benchmark experiments
SCRATCH_DIR=scratch
DEPS_DIR="resources/jar"

EFLECT_JAR="eflect-experiments.jar:../eflect.jar"
DACAPO_JAR="${DEPS_DIR}/dacapo.jar"

mkdir $SCRATCH_DIR

pids=""

ITERS=$2

SIZE=large

BENCHMARK=sunflow
java -Deflect.output=$OUTPUT_DIR/mixed/sunflow-h2/$BENCHMARK -cp $EFLECT_JAR:$DACAPO_JAR Harness $BENCHMARK -s $SIZE -n $ITERS \
  -c eflect.experiments.EflectCallback --no-validation --scratch-directory=$SCRATCH_DIR/$BENCHMARK &
pids+=$!" "

BENCHMARK=h2
java -Deflect.output=$OUTPUT_DIR/mixed/sunflow-h2/$BENCHMARK -cp $EFLECT_JAR:$DACAPO_JAR Harness $BENCHMARK -s $SIZE -n $ITERS \
  -c eflect.experiments.EflectCallback --no-validation --scratch-directory=$SCRATCH_DIR/$BENCHMARK &
pids+=$!" "

for pid in $pids; do
  tail --pid=$pid -f /dev/null
done

rm $SCRATCH_DIR -r

###############################################
mkdir $SCRATCH_DIR

pids=""

ITERS=$2

SIZE=large

BENCHMARK=pmd
java -Deflect.output=$OUTPUT_DIR/mixed/h2-pmd/$BENCHMARK -cp $EFLECT_JAR:$DACAPO_JAR Harness $BENCHMARK -s $SIZE -n $ITERS \
  -c eflect.experiments.EflectCallback --no-validation --scratch-directory=$SCRATCH_DIR/$BENCHMARK &
pids+=$!" "

BENCHMARK=h2
java -Deflect.output=$OUTPUT_DIR/mixed/h2-pmd/$BENCHMARK -cp $EFLECT_JAR:$DACAPO_JAR Harness $BENCHMARK -s $SIZE -n $ITERS \
  -c eflect.experiments.EflectCallback --no-validation --scratch-directory=$SCRATCH_DIR/$BENCHMARK &
pids+=$!" "

for pid in $pids; do
  tail --pid=$pid -f /dev/null
done

rm $SCRATCH_DIR -r

################################################
mkdir $SCRATCH_DIR

pids=""

ITERS=$2

BENCHMARK=xalan
SIZE=small
java -Deflect.output=$OUTPUT_DIR/mixed/sunflow-h2-xalan-pmd/$BENCHMARK -cp $EFLECT_JAR:$DACAPO_JAR Harness $BENCHMARK -s $SIZE -n $ITERS \
  -c eflect.experiments.EflectCallback --no-validation --scratch-directory=$SCRATCH_DIR/$BENCHMARK &
pids+=$!" "

SIZE=large

BENCHMARK=sunflow
java -Deflect.output=$OUTPUT_DIR/mixed/sunflow-h2-xalan-pmd/$BENCHMARK -cp $EFLECT_JAR:$DACAPO_JAR Harness $BENCHMARK -s $SIZE -n $ITERS \
  -c eflect.experiments.EflectCallback --no-validation --scratch-directory=$SCRATCH_DIR/$BENCHMARK &
pids+=$!" "

BENCHMARK=h2
java -Deflect.output=$OUTPUT_DIR/mixed/sunflow-h2-xalan-pmd/$BENCHMARK -cp $EFLECT_JAR:$DACAPO_JAR Harness $BENCHMARK -s $SIZE -n $ITERS \
  -c eflect.experiments.EflectCallback --no-validation --scratch-directory=$SCRATCH_DIR/$BENCHMARK &
pids+=$!" "

BENCHMARK=pmd
java -Deflect.output=$OUTPUT_DIR/mixed/sunflow-h2-xalan-pmd/$BENCHMARK -cp $EFLECT_JAR:$DACAPO_JAR Harness $BENCHMARK -s $SIZE -n $ITERS \
  -c eflect.experiments.EflectCallback --no-validation --scratch-directory=$SCRATCH_DIR/$BENCHMARK &
pids+=$!" "

for pid in $pids; do
  tail --pid=$pid -f /dev/null
done

rm $SCRATCH_DIR -r
