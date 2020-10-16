#!/bin/bash

# Install Prerequisites
sudo add-apt-repository ppa:openjdk-r/ppa
sudo apt update
sudo apt install openjdk-11-jdk
sudo apt install unzip
sudo apt install curl
sudo apt install apt-transport-https
sudo apt-get -y install cmake

# Setup Elasticsearch
wget -qO - https://d3g5vo6xdbdb9a.cloudfront.net/GPG-KEY-opendistroforelasticsearch | sudo apt-key add -
echo "deb https://d3g5vo6xdbdb9a.cloudfront.net/apt stable main" | sudo tee -a   /etc/apt/sources.list.d/opendistroforelasticsearch.list
wget https://artifacts.elastic.co/downloads/elasticsearch/elasticsearch-oss-7.9.1-amd64.deb
sudo dpkg -i elasticsearch-oss-7.9.1-amd64.deb
sudo apt-get update
sudo apt install opendistroforelasticsearch
echo "Opendistroforelasticsearch is installed successfully."
echo "----------------------------------------------------------------------------------------------------------"

# Setup MySQL
sudo apt-get update
sudo apt-get install mysql-server
sudo mysql_secure_installation
echo "Mysql is installed successfully."
echo "----------------------------------------------------------------------------------------------------------"

# Setup Cassandra
sudo apt install openjdk-8-jdk
update-alternatives --set java /usr/lib/jvm/java-8-openjdk-amd64/jre/bin/java
echo "deb http://downloads.apache.org/cassandra/debian 40x main" | sudo tee -a /etc/apt/sources.list.d/cassandra.sources.list deb http://downloads.apache.org/cassandra/debian 40x main
curl https://downloads.apache.org/cassandra/KEYS | sudo apt-key add -
sudo apt-get update
sudo apt-get install cassandra
nodetool status
echo "Cassandra is installed successfully."
echo "----------------------------------------------------------------------------------------------------------"

# Build dbgen
cd ../tpch-dbgen
make
cd ..
echo "dbgen is built successfully."
echo "----------------------------------------------------------------------------------------------------------"
