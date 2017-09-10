
package br.com.diegosilva.grpc.hello;

import io.grpc.Server;
import io.grpc.ServerBuilder;
import io.grpc.stub.StreamObserver;
import io.reactivex.*;
import io.reactivex.functions.Consumer;
import io.reactivex.functions.Function;
import io.reactivex.functions.Predicate;
import io.reactivex.subjects.PublishSubject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;


public class GameServer {
  private static final Logger logger = Logger.getLogger(GameServer.class.getName());

  private Server server;

  private static List<String> usuariosAutenticados = new ArrayList<>();
  private static PublishSubject<String> usuariosAutenticadosPublisher = PublishSubject.create();

  private void start() throws IOException {

    int port = 50051;

    server = ServerBuilder.forPort(port)
            .addService(new AutenticacaoImpl())
            .addService(new UsuarioServiceImpl())
        .build()
        .start();

    logger.info("Server started, listening on " + port);

    Runtime.getRuntime().addShutdownHook(new Thread(() -> {
      // Use stderr here since the logger may have been reset by its JVM shutdown hook.
      System.err.println("*** shutting down gRPC server since JVM is shutting down");
      GameServer.this.stop();
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
    final GameServer server = new GameServer();
    server.start();
    server.blockUntilShutdown();
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

        usuariosAutenticadosPublisher.onNext(request.getUsuario());

        response.setCodigo(0);
        response.setMessage("Usuário autenticado");
      }

      responseObserver.onNext(response.build());
      responseObserver.onCompleted();

    }
  }

  static class UsuarioServiceImpl extends UsuariosGrpc.UsuariosImplBase{

    @Override
    public void listarUsuarios(Usuario request, StreamObserver<Usuario> responseObserver) {

      Observable.fromIterable(usuariosAutenticados)
              .filter(new Predicate<String>() {
        @Override
        public boolean test(String s) throws Exception {
          return !s.equals(request.getNome());
        }
      }).concatMap(new Function<String, ObservableSource<String>>() {
        @Override
        public ObservableSource<String> apply(String s) throws Exception {
          return Observable.just(s).delay(2, TimeUnit.SECONDS);
        }
      }).subscribe(new Consumer<String>() {
        @Override
        public void accept(String s) throws Exception {
          responseObserver.onNext(Usuario.newBuilder().setNome(s).build());
        }
      });

      usuariosAutenticadosPublisher.subscribe(new Consumer<String>() {
        @Override
        public void accept(String s) throws Exception {
          responseObserver.onNext(Usuario.newBuilder().setNome(s).build());
        }
      });

    }
  }
}
