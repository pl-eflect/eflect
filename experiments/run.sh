#!/bin/bash

OUTPUT_DIR="eflect-logs"

ITERS=20

SIZE=default
BENCHMARKS=(biojava jython xalan)

for CORUNS in `seq 2 1 5`; do
  for BENCHMARK in ${BENCHMARKS[@]}; do
    ./scripts/dacapo-same-co-run.sh "$BENCHMARK -s $SIZE -n $ITERS" $CORUNS $OUTPUT_DIR/footprints/same/$CORUNS/$BENCHMARK
  done
done

SIZE=large
BENCHMARKS=(avrora batik eclipse h2 pmd sunflow)

for CORUNS in `seq 2 1 5`; do
  for BENCHMARK in ${BENCHMARKS[@]}; do
    ./scripts/dacapo-same-co-run.sh "$BENCHMARK -s $SIZE -n $ITERS" $CORUNS $OUTPUT_DIR/footprints/same/$CORUNS/$BENCHMARK
  done
done

CORUNS=2

SIZE=default
BENCHMARKS=(biojava jython xalan)

for BENCHMARK in ${BENCHMARKS[@]}; do
  ./scripts/dacapo-chappie-same-co-run.sh "$BENCHMARK -s $SIZE -n $ITERS" $CORUNS $OUTPUT_DIR/footprints/same/$CORUNS/$BENCHMARK
done

SIZE=large
BENCHMARKS=(avrora batik eclipse h2 pmd sunflow)

for BENCHMARK in ${BENCHMARKS[@]}; do
  ./scripts/dacapo-chappie-same-co-run.sh "$BENCHMARK -s $SIZE -n $ITERS" $CORUNS $OUTPUT_DIR/footprints/same/$CORUNS/$BENCHMARK
done

for CORUNS in `seq 2 1 5`; do
  ./scripts/tensorflow-same-co-run.sh $CORUNS $OUTPUT_DIR/footprints/same/$CORUNS/tensorflow
done

./scripts/sunflow-same-co-run.sh "50 50" $OUTPUT_DIR/footprints/same/sunflow
./scripts/sunflow-same-co-run.sh "30 70" $OUTPUT_DIR/mixed/sunflow
