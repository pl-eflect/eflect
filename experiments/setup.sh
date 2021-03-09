# !/bin/bash

# helper functions
function fetch_file {
  url=$1
  file=$2
  if [ ! -f $file ]; then
    wget -O $file $url
    dirty=true
  fi
}

# have to remove the tarball...
function fetch_tar {
  url=$1
  tar=$2
  dir=$3
  if [ ! -f $tar ]; then
    wget -O $tar $url
    tar -xvf $tar -C $dir
    dirty=true
  fi
}

# build eflect
EFLECT_JAR=../eflect.jar
if [ ! -f $EFLECT_JAR ]; then
  make -C ..
  dirty=true
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

TENSORFLOW_JNI_URL=https://storage.googleapis.com/tensorflow/libtensorflow/libtensorflow_jni-cpu-linux-x86_64-2.4.0.tar.gz
TENSORFLOW_JNI_TAR=libtensorflow_jni-cpu-linux-x86_64-2.4.0.tar.gz
fetch_tar $TENSORFLOW_JNI_URL $BIN_DIR/$TENSORFLOW_JNI_TAR $BIN_DIR

GRAPH_TAR_URL=https://storage.googleapis.com/download.tensorflow.org/models/inception_v3_2016_08_28_frozen.pb.tar.gz
GRAPH_TAR=inception_v3_2016_08_28_frozen.pb.tar.gz
fetch_tar $GRAPH_TAR_URL $DATA_DIR/$GRAPH_TAR $DATA_DIR

IMAGE_URL=https://farm1.static.flickr.com/134/391533489_8a3b17aa93.jpg
IMAGE=test_image.jpeg
fetch_file $IMAGE_URL $DATA_DIR/$IMAGE

SUNFLOW_JAR_URL=https://sunflow-deps.s3.amazonaws.com/sunflow.jar
SUNFLOW_JAR=sunflow.jar
fetch_file $SUNFLOW_JAR_URL $JAR_DIR/$SUNFLOW_JAR

JANINO_JAR_URL=https://sunflow-deps.s3.amazonaws.com/janino.jar
JANINO_JAR=janino.jar
fetch_file $JANINO_JAR_URL $JAR_DIR/$JANINO_JAR

# build local deps
if [ ! -f $JAR_DIR/async-profiler.jar ]; then
  make -C third_party/async-profiler
  cp third_party/async-profiler/build/async-profiler.jar resources/jar/.
  cp third_party/async-profiler/build/libasyncProfiler.so resources/bin/.
  dirty=true
fi

if [ ! -f $JAR_DIR/stokelib.jar ]; then
  make -C third_party/aeneas
  cp third_party/aeneas/stokelib.jar resources/jar/.
  cp third_party/aeneas/libs/guava-20.0.jar resources/jar/.
  dirty=true
fi

# build experiments
if [ ! -f eflect-experiments.jar ] || [ ! -z $dirty ]; then
  make
fi
