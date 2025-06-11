#!/bin/bash

export BASE_PATH=$HOME/meiSearchBackend

export LIB_CLASSPATH=$BASE_PATH/lib

# Create classpath by dynamically listing all jars in the lib folder
CLASSPATH=$(find "$LIB_CLASSPATH" -name "*.jar" | tr '\n' ':')

# Add the output directory to the classpath
CLASSPATH="$CLASSPATH:$BASE_PATH/out/production/meiSearchBackend"

# Compile all Java files
javac -d "$BASE_PATH/out/production/meiSearchBackend" -cp "$CLASSPATH" $(find $BASE_PATH/src -name "*.java")

# Run the Indexer class with arguments, it pulls DB creds from env
java -cp "$CLASSPATH" workers.Indexer "localhost" "5000"
