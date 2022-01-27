#!/bin/bash

mkdir data
mkdir data/footprints

./evaluation-experiments.sh data/footprints/evaluation 20
./chappie-experiments.sh data/footprints/chappie 20
./aeneas-experiments.sh data/footprints/aeneas 500
