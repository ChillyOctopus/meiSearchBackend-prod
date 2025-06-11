#!/bin/bash
echo "This step is optional but generally recommended."
echo
# Removes all docker processes with our name
for OUTPUT in $(docker ps -q -f "name=mei-search-backend-*")
do
  sudo docker stop $OUTPUT
  sudo docker rm $OUTPUT
done

# Prunes ALL unused containers, regardless of stopping reason
sudo docker system prune -a
