#!/bin/bash

export INSTANCE_URL=ec2-user@ec2-18-216-198-21.us-east-2.compute.amazonaws.com
export HOME_URL=$HOME/jacob/job/specialCollections/meiSearchBackend
export PEM_PATH=$HOME/jacob/coding/private/melodySearchBackend.pem

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
