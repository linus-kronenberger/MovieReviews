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
ssh "$VM_USER@$VM_IP" 'bash -s' <<'DEPLOY'
  set -eu
  if [ ! -d ~/MovieReviews/.git ]; then
    echo 'Repo not found on VM — cloning ...'
    git clone https://github.com/linus-kronenberger/MovieReviews.git ~/MovieReviews
  fi
  cd ~/MovieReviews
  git remote set-url origin https://github.com/linus-kronenberger/MovieReviews.git
  test -z "$(git status --porcelain --untracked-files=no)"
  git switch master
  git pull --ff-only origin master
  docker compose up --build -d --remove-orphans
DEPLOY

echo "Done. App should be reachable at http://$VM_IP:8080"
