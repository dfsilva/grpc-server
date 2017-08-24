
package br.com.diegosilva.grpc.hello;

import io.grpc.Server;
import io.grpc.ServerBuilder;
import io.grpc.stub.StreamObserver;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;


public class HelloWorldServer {
  private static final Logger logger = Logger.getLogger(HelloWorldServer.class.getName());

  private Server server;

  private static List<String> usuariosAutenticados = new ArrayList<>();

  private void start() throws IOException {

    int port = 50051;

    server = ServerBuilder.forPort(port)
        .addService(new GreeterImpl())
            .addService(new AutenticacaoImpl())
        .build()
        .start();

    logger.info("Server started, listening on " + port);

    Runtime.getRuntime().addShutdownHook(new Thread(() -> {
      // Use stderr here since the logger may have been reset by its JVM shutdown hook.
      System.err.println("*** shutting down gRPC server since JVM is shutting down");
      HelloWorldServer.this.stop();
      System.err.println("*** server shut down");
    }));
  }

  private void stop() {
    if (server != null) {
      server.shutdown();
    }
  }

  private void blockUntilShutdown() throws InterruptedException {
    if (server != null) {
      server.awaitTermination();
    }
  }

  public static void main(String[] args) throws IOException, InterruptedException {
    final HelloWorldServer server = new HelloWorldServer();
    server.start();
    server.blockUntilShutdown();
  }

  static class GreeterImpl extends GreeterGrpc.GreeterImplBase {

    @Override
    public void sayHello(HelloRequest req, StreamObserver<HelloReply> responseObserver) {
      HelloReply reply = HelloReply.newBuilder().setMessage("Hello " + req.getName()).build();
      responseObserver.onNext(reply);
      responseObserver.onCompleted();
    }
  }

  static class AutenticacaoImpl
          extends AutenticacaoGrpc.AutenticacaoImplBase {

    public void autenticar(AutenticacaoRequest request,
                           StreamObserver<AutenticacaoResponse> responseObserver) {

      AutenticacaoResponse.Builder response = AutenticacaoResponse.newBuilder();

      if(usuariosAutenticados.contains(request.getUsuario())){
        //retorna erro, usuario já autenticado
        response.setCodigo(-1);
        response.setMessage("Já existe um usuário autenticado com este login");
      }else{//retorna sucesso e adiciona o usuario
        usuariosAutenticados.add(request.getUsuario());
        response.setCodigo(0);
        response.setMessage("Usuário autenticado");
      }

    }
  }
}
