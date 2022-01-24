#!/bin/bash

OUTPUT_DIR=$1
ITERS=$2

./scripts/aeneas-co-run.sh "35 35" $OUTPUT_DIR/same $ITERS
./scripts/aeneas-co-run.sh "35 70" $OUTPUT_DIR/mixed $ITERS

./scripts/aeneas-foreign.sh 35 $ITERS $OUTPUT_DIR/foreign "h2 -n 5 -s large"
