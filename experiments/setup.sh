# !/bin/bash

# helper functions
function fetch_file {
  url=$1
  file=$2
  if [ ! -f $file ]; then
    wget -O $file $url
  fi
}

function fetch_tar {
  url=$1
  tar=$2
  dir=$3
  if [ ! -f $tar ]; then
    wget -O $tar $url
    tar -xvf $tar -C $dir
  fi
}

function fetch_zip {
  url=$1
  zip=$2
  dir=$3
  if [ ! -f $zip ]; then
    wget -O $zip $url
    unzip $zip -d $dir
  fi
}

# build eflect
EFLECT_JAR=../eflect.jar
if [ ! -f $EFLECT_JAR ]; then
  make -C ..
fi

# build resource dirs
RESOURCE_DIR=resources
mkdir -p $RESOURCE_DIR

BIN_DIR=$RESOURCE_DIR/bin
mkdir -p $BIN_DIR

DATA_DIR=$RESOURCE_DIR/data
mkdir -p $DATA_DIR

JAR_DIR=$RESOURCE_DIR/jar
mkdir -p $JAR_DIR

# grab from web
DACAPO_JAR_URL=https://sourceforge.net/projects/dacapobench/files/evaluation/dacapo-evaluation-git%2B309e1fa.jar/download
DACAPO_JAR=dacapo.jar
fetch_file $DACAPO_JAR_URL $JAR_DIR/$DACAPO_JAR

TENSORFLOW_JAR_URL=https://storage.googleapis.com/tensorflow/libtensorflow/libtensorflow-1.14.0.jar
TENSORFLOW_JAR=libtensorflow.jar
fetch_file $TENSORFLOW_JAR_URL $JAR_DIR/$TENSORFLOW_JAR

SUNFLOW_URL=https://clerk-deps.s3.amazonaws.com/sunflow.zip
SUNFLOW_ZIP=sunflow.zip
fetch_zip $SUNFLOW_URL $JAR_DIR/$SUNFLOW_ZIP $JAR_DIR

TENSORFLOW_JNI_URL=https://storage.googleapis.com/tensorflow/libtensorflow/libtensorflow_jni-cpu-linux-x86_64-2.4.0.tar.gz
TENSORFLOW_JNI_TAR=libtensorflow_jni-cpu-linux-x86_64-2.4.0.tar.gz
fetch_tar $TENSORFLOW_JNI_URL $BIN_DIR/$TENSORFLOW_JNI_TAR $BIN_DIR

IMAGE_URL=https://farm1.static.flickr.com/134/391533489_8a3b17aa93.jpg
IMAGE=test_image.jpeg
fetch_file $IMAGE_URL $DATA_DIR/$IMAGE

GRAPH_TAR_URL=https://storage.googleapis.com/download.tensorflow.org/models/inception_v3_2016_08_28_frozen.pb.tar.gz
GRAPH_TAR=inception_v3_2016_08_28_frozen.pb.tar.gz
fetch_tar $GRAPH_TAR_URL $DATA_DIR/$GRAPH_TAR $DATA_DIR

# build local deps
if [ ! -f $JAR_DIR/async-profiler.jar ]; then
  make -C third_party/async-profiler
  cp third_party/async-profiler/build/async-profiler.jar resources/jar/.
  cp third_party/async-profiler/build/libasyncProfiler.so resources/bin/.
fi

if [ ! -f $JAR_DIR/stokelib.jar ]; then
  make -C third_party/aeneas
  cp third_party/aeneas/stokelib.jar resources/jar/.
fi

# build experiments
if [ ! -f eflect-experiments.jar ]; then
  make
fi
