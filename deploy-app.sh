#!/bin/bash

# Variables
APP_NAME="clockapp"               # systemd service name
VERSION="2025.11.0"
LOCAL_APP_PATH="workdir/build/tmp/work/cortexa76-poky-linux/clockapp/${VERSION}/package/usr/bin/clockapp"     # local path to the app binary
REMOTE_USER="root"                     # remote device username
REMOTE_HOST="192.168.1.221"    # remote device IP or hostname
REMOTE_TEMP_PATH="/tmp/$APP_NAME"      # temporary location on remote device
REMOTE_TARGET_PATH="/usr/bin/$APP_NAME"

# Copy app to remote device
echo "Copying $APP_NAME to $REMOTE_HOST..."
scp "$LOCAL_APP_PATH" "$REMOTE_USER@$REMOTE_HOST:$REMOTE_TEMP_PATH"

# SSH into remote device and deploy
ssh "$REMOTE_USER@$REMOTE_HOST" << EOF
    echo "Stopping $APP_NAME..."
    systemctl stop "$APP_NAME"

    echo "Moving app to $REMOTE_TARGET_PATH..."
    mv "$REMOTE_TEMP_PATH" "$REMOTE_TARGET_PATH"
    chmod +x "$REMOTE_TARGET_PATH"

    echo "$APP_NAME deployed successfully."
    systemctl start "$APP_NAME"
EOF

