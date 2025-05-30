#!/bin/bash

export BASE_PATH="$HOME/meiSearchBackend" # PRODUCTION
#export BASE_PATH="$HOME/IdeaProjects/meiSearchBackend" # DEVELOPMENT

export LIB_CLASSPATH="$BASE_PATH/lib"

# Create classpath by dynamically listing all jars in the lib folder
CLASSPATH=$(find "$LIB_CLASSPATH" -name "*.jar" | tr '\n' ':')

# Add the output directory to the classpath
CLASSPATH="$CLASSPATH:$BASE_PATH/out/production/meiSearchBackend"

# Compile all Java files
javac -d "$BASE_PATH/out/production/meiSearchBackend" -cp "$CLASSPATH" $(find $BASE_PATH/src -name "*.java")

# Run the Indexer class with arguments
java -cp "$CLASSPATH" workers.Indexer "jdbc:postgresql://melodysearchmeidatabase.ct8ig60g4q3f.us-east-2.rds.amazonaws.com:5432/meiDatabase" "postgres" "ScouredElmContempt8" "localhost" "5000"
