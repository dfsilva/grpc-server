FROM openjdk:8u111-jre-alpine
MAINTAINER GRPC Server
RUN apk --update add bash openssl apr
ADD build/install/grpc-server /grpc-server
ENTRYPOINT bin/grpc-server
WORKDIR /grpc-server
