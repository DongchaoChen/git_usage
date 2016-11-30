#!/bin/bash

# Parse the docker and artifactory credentials from the gradle.properties file
dos2unix -n /gradle_user_home/gradle.properties /gradle_user_home/gradle-unix.properties
for line in $(cat /gradle_user_home/gradle-unix.properties)
do
  if [[ $line =~ ^docker_username=(.*)$ ]]
    then
    DOCKER_USER=${BASH_REMATCH[1]};
  fi
  if [[ $line =~ ^docker_password=(.*)$ ]]
    then
    DOCKER_PASS=${BASH_REMATCH[1]};
  fi
  if [[ $line =~ ^docker_email=(.*)$ ]]
    then
    DOCKER_EMAIL=${BASH_REMATCH[1]};
  fi
done
rm /gradle_user_home/gradle-unix.properties

# Edit docker.service so it listens to requests made on port 2376
sudo sed -i "s/-H fd:\/\//-H tcp:\/\/0\.0\.0\.0:2376 -H unix:\/\\/\/var\/run\/docker.sock/" /lib/systemd/system/docker.service

# Relaod and restart the docker daemon
sudo systemctl daemon-reload
sudo service docker restart

# Login to docker hub
docker login -e $DOCKER_EMAIL -u $DOCKER_USER -p $DOCKER_PASS

# Copy the docker hub credential file to the vagrant user's home directory so we don't have to input credentials when pulling images
sudo cp -r /root/.docker /home/vagrant/
sudo chown -R vagrant:vagrant /home/vagrant/.docker/

# Run the dev deploy script
sudo chmod +x dev-deploy.sh
sudo chmod +x cloud-config.sh
sudo ./dev-deploy.sh

CASSANDRA_PORT=9042
VAGRANT_MOUNT=/tmp/cassandra
echo "vagrant mount folder is " $VAGRANT_MOUNT

echo "Clearing out old cassandra container..."
sudo docker kill cassandra &>/dev/null || true
sudo docker rm -f cassandra &>/dev/null || true

echo "Starting up single node cassandra cluster..."
sudo docker run --restart always \
  -d --memory="2G" --memory-swap="-1" -v $VAGRANT_MOUNT:/var/lib/cassandra --name cassandra -p 9042:9042 \
  --ulimit memlock=-1 \
  --ulimit nofile=100000 \
  --ulimit nproc=32768  \
  qlik/cassandra-content-service:latest
echo "Done. Cassandra is now listening at 127.0.0.1:$CASSANDRA_PORT"

echo "Stopping other docker images not used for content-service"
sudo docker stop baseservices_logstash_1 &>/dev/null || true
sudo docker stop baseservices_logspout_1 &>/dev/null || true
sudo docker stop baseservices_dns_1 &>/dev/null || true
sudo docker stop consul-server &>/dev/null || true
sudo docker stop registrator &>/dev/null || true
