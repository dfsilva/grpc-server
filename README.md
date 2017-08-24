# Projeto de Demonstração Cliente/Servidor utilizando GRPC

1 - Compilar o projeto

 `$ ./gradlew installDist`
 
2 - Rodar o servidor

`$ ./build/install/grpc-server/bin/hello-world-server`

3 - Em outro console execute

`$ ./build/install/grpc-server/bin/hello-world-client`

# Para executar o código de Autenticaçao atualizado na aula de 23/08/2017

1 - Compilar o projeto

No linux
 `$ ./gradlew installDist`
 
No windows
 `gradlew.bat installDist` 
 
2 - Rodar o servidor

No linux
`$ ./build/install/grpc-server/bin/hello-world-server`

No windows
`build/install/grpc-server/bin/hello-world-server.bat`

3 - Em outro console execute o código para autenticacao

No linux
`$ ./build/install/grpc-server/bin/autenticacao-cliente`

No windows
`build/install/grpc-server/bin/autenticacao-cliente.bat`
 
