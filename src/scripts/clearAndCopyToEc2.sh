#!/bin/bash

export INSTANCE_URL=INSTANCE_URL
export HOME_URL=HOME_URL
export PEM_PATH=PEM_PATH

# ssh into instance, clear it out
ssh -i $PEM_PATH $INSTANCE_URL << EOF
cd
rm -rf meiSearchBackend
mkdir meiSearchBackend
exit
EOF

# Copy necessary directories to instance
scp -r -i $PEM_PATH $HOME_URL/lib $INSTANCE_URL:meiSearchBackend
scp -r -i $PEM_PATH $HOME_URL/META-INF $INSTANCE_URL:meiSearchBackend
scp -r -i $PEM_PATH $HOME_URL/out $INSTANCE_URL:meiSearchBackend
scp -r -i $PEM_PATH $HOME_URL/src $INSTANCE_URL:meiSearchBackend

# Copy specific files to instance
scp -i $PEM_PATH $HOME_URL/docker-compose.yml $INSTANCE_URL:meiSearchBackend
scp -i $PEM_PATH $HOME_URL/Dockerfile $INSTANCE_URL:meiSearchBackend
