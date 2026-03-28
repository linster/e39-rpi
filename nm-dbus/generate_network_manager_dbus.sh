#!/bin/bash

# https://docs.docker.com/engine/storage/bind-mounts/#start-a-container-with-a-bind-mount


docker build . -t networkmanagerapibuilder && \
docker run \
  --privileged \
  --mount type=bind,src="$PWD",dst=/network-manager/outbind networkmanagerapibuilder
#  --volume "$PWD":/network-manager/outbind networkmanagerapibuilder
#  --mount type=bind,source=`pwd`/output,target=/outbind,readonly networkmanagerapibuilder