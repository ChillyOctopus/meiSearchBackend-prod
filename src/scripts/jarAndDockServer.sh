#!/bin/bash

export BASE_PATH=$HOME/meiSearchBackend

cd $BASE_PATH

# Make a directory
mkdir -p tmp_jar_contents

# Move into it
cd tmp_jar_contents

# Unpack jar file into directory
jar xf $BASE_PATH/out/artifacts/meiSearchBackend_jar/meiSearchBackend.jar

# Copy the manifest file to the correct location
cp $BASE_PATH/META-INF/manifest.txt $BASE_PATH/out/production/meiSearchBackend/manifest.txt

# Create the application JAR including the extracted contents
jar -cvfm myapp.jar $BASE_PATH/out/production/meiSearchBackend/manifest.txt -C ./ .

# Move the created JAR file to the appropriate directory after
mv myapp.jar $BASE_PATH/src/serverCode
# Move out of the directory and clean it
cd ..
rm -rf tmp_jar_contents

# Remove existing Docker containers if they exist and rebuild them, then remove the jar
docker-compose down
echo
source $BASE_PATH/src/scripts/rmAllDocker.sh # Run the remove all docker script inside this shell
echo
docker-compose up --build -d
rm $BASE_PATH/src/serverCode/myapp.jar

# Send a curl command to ping the java server and make the music index
sleep 5
echo
echo "ping:"
curl -H 'Accept: application/json' \
     -H 'Content-Type: application/json' \
     -H 'Cache-Control: no-Cache' \
     -H 'Connection: close' \
     -X GET http://0.0.0.0:5000/ping
echo
echo "Sleeping for 20s to allow ElasticSearch to set up..."
sleep 20
# Define the curl command
curl_cmd="curl -s -H 'Accept: application/json' \
    -H 'Content-Type: application/json' \
    -H 'Cache-Control: no-Cache' \
    -H 'Connection: close' \
    -d '' \
    -X POST http://0.0.0.0:5000/createMusicIndex"
# Run and check until "success": true
max_attempts=10
echo "Attempting to create music index... ($max_attempts max attempts)"
while [ $max_attempts -gt 0 ];
do
    response=$(eval "$curl_cmd")
    echo
    echo "$response"

    success=$(echo "$response" | jq -r '.success')
    if [ "$success" == "true" ]; then
        echo "Success! Music index created."
        break
    fi

    ((max_attempts--))
    echo "Creation failed. $max_attempts more retries, sleeping 3 seconds."
    sleep 3
done
echo
echo "If created, the music index is empty. Run src/scripts/indexDatabase.sh to populate it."
echo
