CC = gcc
CFLAGS = -fPIC -g

JAVA_HOME = $(shell readlink -f /usr/bin/javac | sed "s:bin/javac::")
JAVA_INCLUDE = $(JAVA_HOME)include
JAVA_LINUX_INCLUDE = $(JAVA_INCLUDE)/linux
JNI_INCLUDE = -I$(JAVA_INCLUDE) -I$(JAVA_LINUX_INCLUDE)

SOURCES = $(subst .c,.o,$(wildcard src/jrapl/*.c))
HEADERS = $(wildcard src/jrapl/*.h)

BUILD_DIR = build

.DEFAULT_GOAL = eflect

%.o: %.c
	$(CC) -c -o $@ $< $(CFLAGS) $(JNI_INCLUDE)

libCPUScaler.so: $(SOURCES)
	$(CC) -shared -Wl,-soname,$@ -o $@ $^ $(JNI_INCLUDE) -lc
	mkdir -p $(BUILD_DIR)
	mv $@ $(BUILD_DIR)/$@
	cp $(HEADERS) $(BUILD_DIR)

SOURCES = $(wildcard src/eflect/*.java) $(wildcard src/eflect/**/*.java) $(wildcard src/eflect/**/**/*.java)
CLASS_DIR = eflect
TARGET = eflect.jar

eflect: clean libCPUScaler.so
	mkdir -p $(BUILD_DIR)/$(CLASS_DIR)
	javac $(SOURCES) -d $(BUILD_DIR)
	cd $(BUILD_DIR) && jar -cf $(TARGET) *
	mv $(BUILD_DIR)/$(TARGET) $(TARGET)
	rm -r $(BUILD_DIR)

clean:
	rm -rf $(BUILD_DIR) $(RAPL_TARGET) $(EFLECT_TARGET)
