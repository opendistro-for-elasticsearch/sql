#!/bin/bash

echo "This script will only remove the default DSN and Driver entries from your ODBC configuration."
echo "You will be responsible for removing installed files from the system."
if [[ $EUID -ne 0 ]]; then
   echo "ERROR: This script must be run as root"
   exit 1
fi

# check for "Yes"
while true; do
    read -p "Do you want to continue? (Y/y) " yn
    case $yn in
        [Yy]* ) break;;
        [Nn]* ) exit;;
        * ) echo "Please answer yes or no.";;
    esac
done

# Run dsn_installer uninstall
${BASH_SOURCE%/*}/dsn_installer uninstall
if [ $? -ne 0 ]; then
    echo "Error while removing DSN and Driver entries."
else
    echo "DSN and Driver entries have been removed successfully."
fi
