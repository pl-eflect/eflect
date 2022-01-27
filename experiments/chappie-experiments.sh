#!/bin/bash

OUTPUT_DIR=$1
ITERS=$2

CORUNS=2

SIZE=default
BENCHMARKS=(biojava jython xalan)

for BENCHMARK in ${BENCHMARKS[@]}; do
  ./scripts/dacapo-chappie-same-co-run.sh "$BENCHMARK -s $SIZE -n $ITERS" $CORUNS $OUTPUT_DIR/$BENCHMARK
done

SIZE=large
BENCHMARKS=(avrora batik eclipse graphchi h2 pmd sunflow)

for BENCHMARK in ${BENCHMARKS[@]}; do
  ./scripts/dacapo-chappie-same-co-run.sh "$BENCHMARK -s $SIZE -n $ITERS" $CORUNS $OUTPUT_DIR/$BENCHMARK
done
