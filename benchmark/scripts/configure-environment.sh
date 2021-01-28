#!/bin/bash

# Get Elasticsearch version as an argument.
if (( $# != 1 ))
then
    echo "Usage: configure-environment.sh [elasticsearch-version]"
    exit 1
fi
ES_VERSION=$1

# Install Prerequisites
sudo add-apt-repository ppa:openjdk-r/ppa
sudo apt-get update
sudo apt-get install openjdk-11-jdk
sudo apt-get install unzip
sudo apt-get install curl
sudo apt-get install apt-transport-https
sudo apt-get -y install cmake

# Setup Elasticsearch
wget -qO - https://d3g5vo6xdbdb9a.cloudfront.net/GPG-KEY-opendistroforelasticsearch | sudo apt-key add -
echo "deb https://d3g5vo6xdbdb9a.cloudfront.net/apt stable main" | sudo tee -a   /etc/apt/sources.list.d/opendistroforelasticsearch.list
wget https://artifacts.elastic.co/downloads/elasticsearch/elasticsearch-oss-${ES_VERSION}-amd64.deb
sudo dpkg -i elasticsearch-oss-${ES_VERSION}-amd64.deb
sudo apt-get update
sudo apt-get install opendistroforelasticsearch

# Setup MySQL
sudo apt-get update
sudo apt-get install mysql-server
sudo mysql_secure_installation

# Setup Cassandra
sudo apt-get install openjdk-8-jdk
update-alternatives --set java /usr/lib/jvm/java-8-openjdk-amd64/jre/bin/java
echo "deb http://downloads.apache.org/cassandra/debian 40x main" | sudo tee -a /etc/apt/sources.list.d/cassandra.sources.list deb http://downloads.apache.org/cassandra/debian 40x main
curl https://downloads.apache.org/cassandra/KEYS | sudo apt-key add -
sudo apt-get update
sudo apt-get install cassandra
nodetool status

# Build dbgen
unzip ./tpch-dbgen.zip
cd tpch-dbgen
make
cd ..
