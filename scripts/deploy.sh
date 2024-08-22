#!/usr/bin/bash

ENV_FILE="./.env"

pushd ~/Desktop/github/HHBot/ || exit

git checkout develop

git pull origin develop

docker compose -f docker-compose.yml --env-file $ENV_FILE down --timeout=60 --remove-orphans
docker compose -f docker-compose.yml --env-file $ENV_FILE up --build --detach

popd || exit