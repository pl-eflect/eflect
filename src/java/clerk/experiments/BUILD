load("@rules_java//java:defs.bzl", "java_binary")

java_binary(
  name = "nop",
  visibility = ["//visibility:public"],
  main_class = "clerk.experiments.NOP",
  srcs = ["NOP.java"],
  deps = [
    "@chappie//src/java/chappie/attribution:profile",
    "@chappie//src/java/chappie/profiling:core",
    "@chappie//src/java/chappie/sampling/energy",
    "//src/java/clerk",
    "@jRAPL//src/jrapl:cpu-scaler-lib",
  ],
  resources = ["@jRAPL//src/jrapl:libCPUScaler.so"],
)

java_binary(
  name = "naive_sunflow",
  visibility = ["//visibility:public"],
  main_class = "clerk.experiments.NaiveSunflow",
  srcs = ["NaiveSunflow.java"],
  deps = [
    "@aeneas",
    "@sunflow",
    "@chappie//src/java/chappie/profiling:core",
    "@chappie//src/java/chappie/sampling/energy",
    "//src/java/clerk",
    "//src/java/clerk:reward",
  ],
  resources = ["@jRAPL//src/jrapl:libCPUScaler.so"],
  jvm_flags = ["-Djava.library.path=/home/timur/projects/async-profiler/build"],
)

java_binary(
  name = "sunflow",
  visibility = ["//visibility:public"],
  main_class = "clerk.experiments.Sunflow",
  srcs = ["Sunflow.java"],
  deps = [
    "@aeneas",
    "@sunflow",
    "@chappie//src/java/chappie/profiling:core",
    "@chappie//src/java/chappie/sampling/energy",
    "//src/java/clerk",
    "//src/java/clerk:reward",
  ],
  resources = ["@jRAPL//src/jrapl:libCPUScaler.so"],
  jvm_flags = ["-Djava.library.path=/home/timur/projects/async-profiler/build"],
)

java_binary(
  name = "dacapo",
  visibility = ["//visibility:public"],
  main_class = "Harness",
  srcs = ["DaCapo.java"],
  deps = [
    "@dacapo",
    "@chappie//src/java/chappie/attribution:profile",
    "@chappie//src/java/chappie/profiling:core",
    "@chappie//src/java/chappie/sampling/energy",
    "//src/java/clerk",
    "@jRAPL//src/jrapl:cpu-scaler-lib",
  ],
  resources = ["@jRAPL//src/jrapl:libCPUScaler.so"],
  # jvm_flags = ["-Djava.library.path=/home/timur/projects/async-profiler/build"],
  args = [
    "--callback clerk.experiments.DaCapo",
    "--no-validation",
  ]
)


java_binary(
  name = "dacapo_nop",
  visibility = ["//visibility:public"],
  main_class = "Harness",
  srcs = ["DaCapoNOP.java"],
  deps = [
    "@dacapo",
    "@chappie//src/java/chappie/attribution:profile",
    "@chappie//src/java/chappie/profiling:core",
    "@chappie//src/java/chappie/sampling/energy",
    "//src/java/clerk",
    "@jRAPL//src/jrapl:cpu-scaler-lib",
  ],
  resources = ["@jRAPL//src/jrapl:libCPUScaler.so"],
  # jvm_flags = ["-Djava.library.path=/home/timur/projects/async-profiler/build"],
  args = [
    "--callback clerk.experiments.DaCapo",
    "--no-validation",
  ]
)
