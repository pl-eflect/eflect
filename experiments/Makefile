SOURCES = $(wildcard src/eflect/experiments/*.java) $(wildcard src/eflect/experiments/util/*.java)
EFLECT_JAR =  ../eflect.jar
EXTERNAL_DEPS = ./third_party/dacapo.jar:./third_party/libtensorflow.jar
BUILD_DIR = temp-build
CLASS_DIR = eflect
TARGET = eflect-experiments.jar

jar:
	mkdir -p $(BUILD_DIR)/$(CLASS_DIR)
	javac $(SOURCES) -d $(BUILD_DIR) -cp $(EFLECT_JAR):$(EXTERNAL_DEPS)
	cd $(BUILD_DIR) && jar -cf $(TARGET) $(CLASS_DIR)
	mv $(BUILD_DIR)/$(TARGET) $(TARGET)
	rm -r $(BUILD_DIR)

clean:
	rm -rf $(BUILD_DIR) $(TARGET)
