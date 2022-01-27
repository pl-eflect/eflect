# `eflect` #

`eflect` is an energy profiler for multi-threaded Java applications in shared environments. Below is our paper abstract about this work:

> Developing energy-aware applications is a well known approach to software-based energy optimization. This promising approach is however faced with a significant hurdle when deployed to the environments shared among multiple applications, where the energy consumption effected by one application may erroneously be observed by another application. We introduce `eflect`, a novel software framework for disentangling the energy consumption of co-running applications. Our key idea, called energy virtualization, enables each energy-aware application to be only aware of the energy consumption effected by its execution. `eflect` is unique in its lightweight design: it is a purely application-level solution that requires no modification to the underlying hardware or system software. Experiments show `eflect` incurs low overhead with high precision. Furthermore, it can seamlessly port existing application-level energy frameworks --- one for energy-adaptive approximation and the other for energy profiling --- to shared environments while retaining their intended effectiveness.

# Experiment reproduction: Docker-based execution #

Our publication data can be reproduced using a docker image. You can either run the image from docker hub:

```bash
docker run --privileged --cap-add=ALL -it -v /dev:/dev -v /lib/modules:/lib/modules pleflect/eflect-icse22:1.0
```

or build and run the provided `Dockerfile`:

```bash
docker build -t eflect-icse20 .
docker run --privileged --cap-add=ALL -it -v /dev:/dev -v /lib/modules:/lib/modules pleflect/eflect-icse22:1.0
```

This repository's code will already be fully built and the experiments can be run using `experiments/run-experiments.sh`, which will output to `experiments/data`.

**NOTE**: The data reported in the paper was produced through an evaluation with the system described below. As energy consumption varies from system to system, e.g., the number of cores, the OS schedulers, the JVM runtime behavior, etc., a reproduction on a different system may not produce identical results as we reported in the paper. Specifically, `eflect` requires the use of [RAPL](https://en.wikipedia.org/wiki/Perf_(Linux)#RAPL), which only works on Intel cpus:

  > - Dual socket Intel E5-2630 v4 2.20 GHz (20 cores)
  > - Hyper threading enabled
  > - 64 GB DDR4 RAM
  > - Debian 4.9 (linux kernel 4.9)
  > - Debian default `powersave` governor
  > - Java 11 Hotspot VM build 11.0.2+9-LTS

# Experiment reproduction: Building from source #

If you prefer to build `eflect` from source, please follow the instructions below:

## Building ##

`eflect` requires the following to build and run:

```bash
apt-get install -y git openjdk-11-jdk libjna-jni git make wget kmod python3 python3-pip
pip3 install numpy pandas matplotlib
```

A deployable `eflect` jar can be built from the top-level with `make eflect`. To run the experiments, you will need to first run `experiments/setup.sh` which will download the experiment dependencies and build the experiment driving code.

## Execution ##

Once `eflect` and its driver are built, the entire experiment can be run using `experiments/run-experiments.sh` in the same way as the docker image.
