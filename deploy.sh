#!/usr/bin/env bash
set -euo pipefail

if [ $# -lt 1 ]; then
  echo "Usage: $0 <vm-ip> [ssh-user]"
  echo "Example: $0 192.168.1.100 ubuntu"
  exit 1
fi

VM_IP="$1"
VM_USER="${2:-$USER}"

echo "Deploying to $VM_USER@$VM_IP ..."
ssh "$VM_USER@$VM_IP" "
  cd ~/MovieReviews
  if [ ! -d .git ]; then
    echo 'Repo not found on VM — cloning ...'
    cd ~
    git clone https://github.com/linuskronenberger/MovieReviews.git
    cd ~/MovieReviews
  else
    git pull
  fi
  docker compose up --build -d
  docker image prune -f
"

echo "Done. App should be reachable at http://$VM_IP:8080"
