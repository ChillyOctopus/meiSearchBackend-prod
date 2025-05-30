#!/bin/bash
echo "This step is optional but generally recommended."
echo "Ensure you have no other containers or volumes in the system."
echo
# Removes all docker processes
for OUTPUT in $(sudo docker ps -aq)
do
  sudo docker stop $OUTPUT
  sudo docker rm $OUTPUT
done

sudo docker system prune -a --volumes # Prune everything as it takes up space overtime.
