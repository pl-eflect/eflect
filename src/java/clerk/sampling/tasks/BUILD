load("@rules_java//java:defs.bzl", "java_library")

java_library(
  name = "tasks",
  visibility = ["//visibility:public"],
  srcs = glob(["*.java"]),
  deps = [
    "@chappie//src/java/chappie/profiling:core",
    "@chappie//src/java/chappie/profiling:samples",
    "@chappie//src/java/chappie/util",
    "@jlibc//src/main/java/jlibc/proc",
    "@jRAPL",
  ],
)
