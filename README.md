# Projeto de Demonstração Cliente/Servidor utilizando GRPC

1 - Compilar o projeto

 `$ ./gradlew installDist`
 
2 - Rodar o servidor

`$ ./build/install/grpc-server/bin/hello-world-server`

3 - Em outro console execute

`$ ./build/install/grpc-server/bin/hello-world-client`

Iniciar utilizando docker

`$ docker run \
     --name grpc-server \
     -p 50001:50051 \
     -d \
     diegosiuniube/grpc-server:1.0`
 
