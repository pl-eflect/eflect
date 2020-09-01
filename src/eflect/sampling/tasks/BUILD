load("@rules_java//java:defs.bzl", "java_library")

java_library(
  name = "tasks",
  visibility = ["//visibility:public"],
  srcs = glob(["*.java"]),
  deps = [
    "@chappie//src/chappie/sampling",
    "@chappie//src/chappie/utils",
    "@jlibc//src/jlibc/proc",
    "@jRAPL",
  ],
)
