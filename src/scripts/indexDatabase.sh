#!/bin/bash

export BASE_PATH="$HOME"

export LIB_CLASSPATH="$BASE_PATH/meiSearchBackend/lib"

# Create classpath by dynamically listing all jars in the lib folder
CLASSPATH=$(find "$LIB_CLASSPATH" -name "*.jar" | tr '\n' ':')

# Add the output directory to the classpath
CLASSPATH="$CLASSPATH:$BASE_PATH/meiSearchBackend/out/production/meiSearchBackend"

# Compile all Java files
javac -d "$BASE_PATH/meiSearchBackend/out/production/meiSearchBackend" -cp "$CLASSPATH" $(find $BASE_PATH/meiSearchBackend/src -name "*.java")

# Run the Indexer class with arguments
java -cp "$CLASSPATH" workers.Indexer "${JDBC_URL}" "${DB_USER}" "${DB_PASS}" "${HOST}" "${PORT}"
