#!/bin/bash

root_dir=./data
mkdir $root_dir

root_dir=$root_dir/scaling-2
mkdir $root_dir

root_dir=$root_dir/h2
mkdir $root_dir

iters=20
rate=41

size=large
benchmarks=(
h2
)

# work_dir=${root_dir}/2
# mkdir ${work_dir}
# for benchmark in ${benchmarks[@]}; do
#   data_dir=${work_dir}/${benchmark}
#   mkdir $data_dir
#
#   mkdir scratch-1
#   mkdir scratch-2
#
#   args="$benchmark -n $iters -s $size --callback clerk.experiments.DaCapo --scratch-directory=scratch-1"
#   java -Dchappie.out=$data_dir/log-1 -Dclerk.rate=$rate -jar bazel-bin/src/java/clerk/experiments/dacapo_deploy.jar $args &
#   pid1=$!
#
#   args="$benchmark -n $iters -s $size --callback clerk.experiments.DaCapo --scratch-directory=scratch-2"
#   java -Dchappie.out=$data_dir/log-2 -Dclerk.rate=$rate -jar bazel-bin/src/java/clerk/experiments/dacapo_deploy.jar $args &
#   pid2=$!
#
#   tail --pid=$pid1 -f /dev/null
#   tail --pid=$pid2 -f /dev/null
#   tail --pid=$pid3 -f /dev/null
#
#   rm -r scratch-1
#   rm -r scratch-2
#   rm -r scratch-3
# done

work_dir=${root_dir}/3
mkdir ${work_dir}
for benchmark in ${benchmarks[@]}; do
  data_dir=${work_dir}/${benchmark}
  mkdir $data_dir

  mkdir scratch-1
  mkdir scratch-2
  mkdir scratch-3

  args="$benchmark -n $iters -s $size --callback clerk.experiments.DaCapo --scratch-directory=scratch-1"
  java -Dchappie.out=$data_dir/log-1 -Dclerk.rate=$rate -jar bazel-bin/src/java/clerk/experiments/dacapo_deploy.jar $args &
  pid1=$!

  args="$benchmark -n $iters -s $size --callback clerk.experiments.DaCapo --scratch-directory=scratch-2"
  java -Dchappie.out=$data_dir/log-2 -Dclerk.rate=$rate -jar bazel-bin/src/java/clerk/experiments/dacapo_deploy.jar $args &
  pid2=$!

  args="$benchmark -n $iters -s $size --callback clerk.experiments.DaCapo --scratch-directory=scratch-3"
  java -Dchappie.out=$data_dir/log-3 -Dclerk.rate=$rate -jar bazel-bin/src/java/clerk/experiments/dacapo_deploy.jar $args &
  pid2=$!

  tail --pid=$pid1 -f /dev/null
  tail --pid=$pid2 -f /dev/null
  tail --pid=$pid3 -f /dev/null

  rm -r scratch-1
  rm -r scratch-2
  rm -r scratch-3
done

work_dir=${root_dir}/4
mkdir ${work_dir}
for benchmark in ${benchmarks[@]}; do
  data_dir=${work_dir}/${benchmark}
  mkdir $data_dir

  mkdir scratch-1
  mkdir scratch-2
  mkdir scratch-3
  mkdir scratch-4

  args="$benchmark -n $iters -s $size --callback clerk.experiments.DaCapo --scratch-directory=scratch-1"
  java -Dchappie.out=$data_dir/log-1 -Dclerk.rate=$rate -jar bazel-bin/src/java/clerk/experiments/dacapo_deploy.jar $args &
  pid1=$!

  args="$benchmark -n $iters -s $size --callback clerk.experiments.DaCapo --scratch-directory=scratch-2"
  java -Dchappie.out=$data_dir/log-2 -Dclerk.rate=$rate -jar bazel-bin/src/java/clerk/experiments/dacapo_deploy.jar $args &
  pid2=$!

  args="$benchmark -n $iters -s $size --callback clerk.experiments.DaCapo --scratch-directory=scratch-3"
  java -Dchappie.out=$data_dir/log-3 -Dclerk.rate=$rate -jar bazel-bin/src/java/clerk/experiments/dacapo_deploy.jar $args &
  pid2=$!

  args="$benchmark -n $iters -s $size --callback clerk.experiments.DaCapo --scratch-directory=scratch-4"
  java -Dchappie.out=$data_dir/log-4 -Dclerk.rate=$rate -jar bazel-bin/src/java/clerk/experiments/dacapo_deploy.jar $args &
  pid2=$!

  tail --pid=$pid1 -f /dev/null
  tail --pid=$pid2 -f /dev/null
  tail --pid=$pid3 -f /dev/null
  tail --pid=$pid4 -f /dev/null

  rm -r scratch-1
  rm -r scratch-2
  rm -r scratch-3
  rm -r scratch-4
done

work_dir=${root_dir}/5
mkdir ${work_dir}
for benchmark in ${benchmarks[@]}; do
  data_dir=${work_dir}/${benchmark}
  mkdir $data_dir

  mkdir scratch-1
  mkdir scratch-2
  mkdir scratch-3
  mkdir scratch-4
  mkdir scratch-5

  args="$benchmark -n $iters -s $size --callback clerk.experiments.DaCapo --scratch-directory=scratch-1"
  java -Dchappie.out=$data_dir/log-1 -Dclerk.rate=$rate -jar bazel-bin/src/java/clerk/experiments/dacapo_deploy.jar $args &
  pid1=$!

  args="$benchmark -n $iters -s $size --callback clerk.experiments.DaCapo --scratch-directory=scratch-2"
  java -Dchappie.out=$data_dir/log-2 -Dclerk.rate=$rate -jar bazel-bin/src/java/clerk/experiments/dacapo_deploy.jar $args &
  pid2=$!

  args="$benchmark -n $iters -s $size --callback clerk.experiments.DaCapo --scratch-directory=scratch-3"
  java -Dchappie.out=$data_dir/log-3 -Dclerk.rate=$rate -jar bazel-bin/src/java/clerk/experiments/dacapo_deploy.jar $args &
  pid2=$!

  args="$benchmark -n $iters -s $size --callback clerk.experiments.DaCapo --scratch-directory=scratch-4"
  java -Dchappie.out=$data_dir/log-4 -Dclerk.rate=$rate -jar bazel-bin/src/java/clerk/experiments/dacapo_deploy.jar $args &
  pid2=$!

  args="$benchmark -n $iters -s $size --callback clerk.experiments.DaCapo --scratch-directory=scratch-5"
  java -Dchappie.out=$data_dir/log-5 -Dclerk.rate=$rate -jar bazel-bin/src/java/clerk/experiments/dacapo_deploy.jar $args &
  pid2=$!

  tail --pid=$pid1 -f /dev/null
  tail --pid=$pid2 -f /dev/null
  tail --pid=$pid3 -f /dev/null
  tail --pid=$pid4 -f /dev/null
  tail --pid=$pid5 -f /dev/null

  rm -r scratch-1
  rm -r scratch-2
  rm -r scratch-3
  rm -r scratch-4
  rm -r scratch-5
done
