CC = gcc
CFLAGS = -fPIC -g

SOURCE_DIR = src/jrapl
TEMP = $(SOURCE_DIR)/*.o
CPU_SCALER_TEMP = $(SOURCE_DIR)/CPUScaler.o $(SOURCE_DIR)/arch_spec.o $(SOURCE_DIR)/msr.o
RAPL_TARGET = *.so
CPU_SCALER_HEADERS_SOURCE = $(SOURCE_DIR)/CPUScaler.h $(SOURCE_DIR)/arch_spec.h $(SOURCE_DIR)/msr.h
CPU_SCALER_HEADERS = CPUScaler.h arch_spec.h msr.h

JAVA_HOME = $(shell readlink -f /usr/bin/javac | sed "s:bin/javac::")
JAVA_INCLUDE = $(JAVA_HOME)include
JAVA_LINUX_INCLUDE = $(JAVA_INCLUDE)/linux
JNI_INCLUDE = -I$(JAVA_INCLUDE) -I$(JAVA_LINUX_INCLUDE)

EFLECT_SOURCES = $(wildcard src/eflect/*.java) $(wildcard src/eflect/data/*.java) $(wildcard src/eflect/data/jiffies/*.java) $(wildcard src/eflect/util/*.java)
BUILD_DIR = temp-build
EFLECT_CLASS_DIR = eflect
EFLECT_TARGET = eflect.jar

clean:
	rm -rf $(BUILD_DIR) $(RAPL_TARGET) $(EFLECT_TARGET)

%.o: %.c
	$(CC) -c -o $@ $< $(CFLAGS) $(JNI_INCLUDE)

libCPUScaler.so: $(CPU_SCALER_TEMP)
	$(CC) -shared -Wl,-soname,$@ -o $@ $^ $(JNI_INCLUDE) -lc
	rm -f $(CPU_SCALER_TEMP)

eflect: clean libCPUScaler.so
	mkdir -p $(BUILD_DIR)/$(EFLECT_CLASS_DIR)
	javac $(EFLECT_SOURCES) -d $(BUILD_DIR)
	mv $(RAPL_TARGET) $(BUILD_DIR)
	cp $(CPU_SCALER_HEADERS_SOURCE) $(BUILD_DIR)
	cd $(BUILD_DIR) && jar -cf $(EFLECT_TARGET) $(EFLECT_CLASS_DIR) $(RAPL_TARGET) $(CPU_SCALER_HEADERS)
	mv $(BUILD_DIR)/$(EFLECT_TARGET) $(EFLECT_TARGET)
	rm -r $(BUILD_DIR)
