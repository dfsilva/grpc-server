#! /bin/bash
export BUILD_NUMBER=01
docker build --no-cache=true -f Dockerfile -t diegosiuniube/grpc-server:1.0 .
