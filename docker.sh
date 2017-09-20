#! /bin/bash
./gradlew installDist && docker build --no-cache=true -f Dockerfile -t diegosiuniube/grpc-server:1.0 .
