#!/bin/bash

OUTPUT_DIR=$1
ITERS=$2

SIZE=default
BENCHMARKS=(biojava jython xalan)

for CORUNS in `seq 2 1 5`; do
  for BENCHMARK in ${BENCHMARKS[@]}; do
    ./scripts/dacapo-same-co-run.sh "$BENCHMARK -s $SIZE -n $ITERS" $CORUNS $OUTPUT_DIR/same/$CORUNS/$BENCHMARK
  done
done

SIZE=large
BENCHMARKS=(avrora batik eclipse graphchi h2 pmd sunflow)

for CORUNS in `seq 2 1 5`; do
  for BENCHMARK in ${BENCHMARKS[@]}; do
    ./scripts/dacapo-same-co-run.sh "$BENCHMARK -s $SIZE -n $ITERS" $CORUNS $OUTPUT_DIR/same/$CORUNS/$BENCHMARK
  done
done

for CORUNS in `seq 2 1 5`; do
  ./scripts/tensorflow-same-co-run.sh $CORUNS $OUTPUT_DIR/same/$CORUNS/tensorflow
done
