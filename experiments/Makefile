SOURCES = $(wildcard src/eflect/*.java) $(wildcard src/eflect/**/*.java) $(wildcard src/eflect/**/**/*.java)
EFLECT_JAR =  ../eflect.jar
EXTERNAL_JARS = $(subst $(DUMMY) $(DUMMY),:,$(wildcard resources/jar/*.jar))
DEPS = $(EFLECT_JAR):$(EXTERNAL_JARS)
TARGET = eflect-experiments.jar

BUILD_DIR = build
eflect:
	mkdir -p $(BUILD_DIR)
	javac $(SOURCES) -d $(BUILD_DIR) -cp $(DEPS)
	cd $(BUILD_DIR) && jar -cf $(TARGET) *
	mv $(BUILD_DIR)/$(TARGET) $(TARGET)
	rm -r $(BUILD_DIR)

clean:
	rm -rf $(BUILD_DIR) $(TARGET)
