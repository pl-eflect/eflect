# Use the official image as a parent image.
FROM debian:stretch-slim

# Setup the environment
ENV JAVA_HOME /usr/lib/jvm/java-1.11.0-openjdk-amd64
WORKDIR /home

# Install debian packages
RUN echo 'deb http://ftp.debian.org/debian stretch-backports main' | tee /etc/apt/sources.list.d/stretch-backports.list
RUN dpkg --configure -a
RUN apt-get -f install
RUN apt-get upgrade
RUN apt-get update
RUN mkdir -p /usr/share/man/man1
RUN apt-get install -y openjdk-11-jdk
RUN apt-get install -y openjdk-11-dbg
RUN apt-get install -y libjna-jni
RUN apt-get install -y git
RUN apt-get install -y make
RUN apt-get install -y wget
RUN apt-get install -y kmod
RUN apt-get install -y python3 python3-pip

# Setup python
RUN pip3 install numpy pandas matplotlib

# Setup eflect
RUN pwd
RUN git clone https://github.com/pl-eflect/eflect.git
RUN cd eflect && make eflect
RUN cd eflect/experiments && bash setup.sh

ENTRYPOINT modprobe msr && cd eflect/experiments && ./smoke-test.sh && /bin/bash
