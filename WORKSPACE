load("@bazel_tools//tools/build_defs/repo:git.bzl", "git_repository", "new_git_repository")
load("@bazel_tools//tools/build_defs/repo:http.bzl", "http_archive")
load("@bazel_tools//tools/build_defs/repo:maven_rules.bzl", "maven_jar")

# http_archive(
#     name = "aeneas",
#     urls = ["https://clerk-deps.s3.amazonaws.com/aeneas.zip"],
# )

http_archive(
    name = "dacapo",
    urls = ["https://clerk-deps.s3.amazonaws.com/dacapo.zip"],
)

http_archive(
    name = "build_bazel_rules_android",
    urls = ["https://github.com/bazelbuild/rules_android/archive/v0.1.1.zip"],
    sha256 = "cd06d15dd8bb59926e4d65f9003bfc20f9da4b2519985c27e190cddc8b7a7806",
    strip_prefix = "rules_android-0.1.1",
)

RULES_JVM_EXTERNAL_TAG = "2.8"
RULES_JVM_EXTERNAL_SHA = "79c9850690d7614ecdb72d68394f994fef7534b292c4867ce5e7dec0aa7bdfad"
http_archive(
    name = "rules_jvm_external",
    strip_prefix = "rules_jvm_external-%s" % RULES_JVM_EXTERNAL_TAG,
    sha256 = RULES_JVM_EXTERNAL_SHA,
    url = "https://github.com/bazelbuild/rules_jvm_external/archive/%s.zip" % RULES_JVM_EXTERNAL_TAG,
)

DAGGER_TAG = "2.28.1"
DAGGER_SHA = "9e69ab2f9a47e0f74e71fe49098bea908c528aa02fa0c5995334447b310d0cdd"
http_archive(
    name = "dagger",
    strip_prefix = "dagger-dagger-%s" % DAGGER_TAG,
    sha256 = DAGGER_SHA,
    urls = ["https://github.com/google/dagger/archive/dagger-%s.zip" % DAGGER_TAG],
)

load("@rules_jvm_external//:defs.bzl", "maven_install")
load("@dagger//:workspace_defs.bzl", "DAGGER_ARTIFACTS", "DAGGER_REPOSITORIES")

maven_install(
    artifacts = DAGGER_ARTIFACTS + ["net.java.dev.jna:jna:5.4.0",],
    repositories = DAGGER_REPOSITORIES,
)

# maven_jar(
#     name = "net_java_dev_jna",
#     artifact = "net.java.dev.jna:jna:5.4.0",
# )

maven_jar(
    name = "net_java_dev_jna_jna",
    artifact = "net.java.dev.jna:jna:5.4.0",
    repository = "https://repo1.maven.org/maven2",
)

git_repository(
    name = "chappie",
    commit = "dab4c07b9a366eeac0a004bf73db697e3fc620eb",
    remote = "https://github.com/anthonycanino1/chappie.git",
)

git_repository(
    name = "jlibc",
    commit = "5996fd31ca86bc07909d7a04c180c5087a60d1be",
    remote = "https://github.com/timurbey/jlibc.git",
)

git_repository(
    name = "jRAPL",
    branch = "master",
    remote = "https://github.com/timurbey/jRAPL.git",
)

new_git_repository(
    name = "sunflow",
    branch = "master",
    remote = "https://github.com/fpsunflower/sunflow.git",
    build_file_content = """
load("@rules_java//java:defs.bzl", "java_library")

java_library(
  name = "sunflow",
  visibility = ["//visibility:public"],
  srcs = glob(["src/**/*.java"]),
  deps = ["janino.jar"],
)
    """,
)

# local_repository(
#   name = "jlibc",
#   path = "/home/timur/projects/jlibc"
# )

new_local_repository(
  name = "async_profiler",
  path = "/home/timur/projects/async-profiler",
  build_file_content = """
load("@rules_java//java:defs.bzl", "java_library")

cc_library(
    name = "async-profiler-lib",
    srcs = glob(["src/*.c"]),
    hdrs = glob(["src/*.h"]),
    deps = ["//tools:copy_jni_hdr_lib"],
    alwayslink = True
)

java_library(
    name = "async_profiler",
    visibility = ["//visibility:public"],
    srcs = glob(["src/java/one/profiler/*.java"])
)

java_binary(
    name = "test",
    visibility = ["//visibility:public"],
    main_class = "one.profiler.Test",
    resources = [":libasyncProfiler.so"],
    runtime_deps = [
      ":async_profiler",
    ],
)
"""
)

new_local_repository(
  name = "aeneas",
  path = "/home/timur/projects/aeneas",
  build_file_content = """
load("@rules_java//java:defs.bzl", "java_import")

java_import(
    name = "aeneas",
    jars = ["stokelib.jar"],
    visibility = ["//visibility:public"],
)
  """,
)
