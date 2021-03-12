SOURCES = $(wildcard src/com/stoke/*.java) $(wildcard src/com/stoke/**/*.java)
DEPS = libs/guava-20.0.jar
TARGET = stokelib.jar

BUILD_DIR = build
stoke:
	mkdir -p $(BUILD_DIR)
	javac $(SOURCES) -d $(BUILD_DIR) -cp $(DEPS)
	cd $(BUILD_DIR) && jar -cf $(TARGET) *
	mv $(BUILD_DIR)/$(TARGET) $(TARGET)
	rm -r $(BUILD_DIR)

clean:
	rm -rf $(BUILD_DIR) $(TARGET)
