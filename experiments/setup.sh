# !/bin/bash

EFLECT_JAR="../eflect.jar"
if [ ! -f $EFLECT_JAR ]; then
  make -C ..
fi

TARGET_DIR="third_party"
mkdir -p $TARGET_DIR

DACAPO_JAR_URL="https://sourceforge.net/projects/dacapobench/files/evaluation/dacapo-evaluation-git%2B309e1fa.jar/download"
DACAPO_JAR=$TARGET_DIR/"dacapo.jar"
if [ ! -f $DACAPO_JAR ]; then
  wget -O $DACAPO_JAR $DACAPO_JAR_URL
fi

TENSORFLOW_JAR_URL="https://storage.googleapis.com/tensorflow/libtensorflow/libtensorflow-1.14.0.jar"
TENSORFLOW_JNI_URL="https://storage.googleapis.com/tensorflow/libtensorflow/libtensorflow_jni-cpu-linux-x86_64-2.4.0.tar.gz"
TENSORFLOW_JAR=$TARGET_DIR/"libtensorflow.jar"
TENSORFLOW_JNI_TAR_NAME="libtensorflow_jni-cpu-linux-x86_64-2.4.0.tar.gz"
TENSORFLOW_JNI_TAR=$TARGET_DIR/$TENSORFLOW_JNI_TAR_NAME
if [ ! -f $TENSORFLOW_JAR ]; then
  wget -O $TENSORFLOW_JAR $TENSORFLOW_JAR_URL
  wget -O $TENSORFLOW_JNI_TAR $TENSORFLOW_JNI_URL
  cd $TARGET_DIR && tar -xvf $TENSORFLOW_JNI_TAR_NAME
  rm $TENSORFLOW_JNI_TAR
fi

RESOURCES=resources
mkdir -p $RESOURCES

GRAPH_TAR_URL=https://storage.googleapis.com/download.tensorflow.org/models/inception_v3_2016_08_28_frozen.pb.tar.gz
GRAPH_TAR=inception_v3_2016_08_28_frozen.pb.tar.gz

IMAGE_URL=https://farm1.static.flickr.com/134/391533489_8a3b17aa93.jpg
IMAGE=$RESOURCES/test_image.jpeg

if [ ! -f $IMAGE ]; then
  wget -O $RESOURCES/$GRAPH_TAR $GRAPH_TAR_URL
  wget -O $IMAGE $IMAGE_URL
  cd $RESOURCES && tar -xvf $GRAPH_TAR
  rm $GRAPH_TAR
fi
