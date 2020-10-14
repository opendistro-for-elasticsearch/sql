#!/bin/bash

# Install Prerequisites
sudo add-apt-repository ppa:openjdk-r/ppa
sudo apt update
sudo apt install openjdk-14-jdk
sudo apt install curl
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

# Setup MongoDB
sudo apt-get install gnupg
wget -qO - https://www.mongodb.org/static/pgp/server-4.4.asc | sudo apt-key add -
echo "deb [ arch=amd64,arm64 ] https://repo.mongodb.org/apt/ubuntu focal/mongodb-org/4.4 multiverse" | sudo tee /etc/apt/sources.list.d/mongodb-org-4.4.list
sudo apt-get update
sudo apt-get install -y mongodb-org
echo "MongoDB is installed successfully."
echo "----------------------------------------------------------------------------------------------------------"

# Build dbgen
cd tpch-dbgen
make
cd ..
echo "dbgen is built successfully."
echo "----------------------------------------------------------------------------------------------------------"
