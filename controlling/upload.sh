#!/bin/bash
# This script copies the latest jar file from the ./latest-build directory
# to a specified directory on a remote server via scp.
#
# Usage: ./deploy.sh <remote_ip> <remote_user> <remote_destination_directory>

# Check if the correct number of arguments is provided.
if [ "$#" -ne 3 ]; then
    echo "Usage: $0 <remote_ip> <remote_user> <remote_destination_directory>"
    exit 1
fi

REMOTE_IP="$1"
REMOTE_USER="$2"
REMOTE_DEST="$3"

# Locate the most recently modified jar file in ./latest-build/
JAR_FILE=$(ls -t ./latest-build/*.jar 2>/dev/null | head -n 1)

if [ -z "$JAR_FILE" ]; then
    echo "Error: No jar file found in ./latest-build/"
    exit 1
fi

echo "Found jar file: $JAR_FILE"
echo "Copying to ${REMOTE_USER}@${REMOTE_IP}:${REMOTE_DEST}"

# Use scp to copy the jar file to the remote server.
scp "$JAR_FILE" "${REMOTE_USER}@${REMOTE_IP}:${REMOTE_DEST}"

# Check if the scp command succeeded.
if [ "$?" -eq 0 ]; then
    echo "Jar file copied successfully."
else
    echo "Error: Failed to copy jar file."
    exit 1
fi
