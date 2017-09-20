
package br.com.diegosilva.grpc;

import br.com.diegosilva.grpc.hello.*;
import br.com.diegosilva.grpc.services.AutenticacaoImpl;
import br.com.diegosilva.grpc.services.UsuarioServiceImpl;
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
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;


public class Main {

  private static final Logger logger = Logger.getLogger(Main.class.getName());

  private static class OperacoesUsuario{
    public static final int INCLUSAO = 0;
    public static final int EXCLUCAO = 1;
  }

  private Server server;

  private static PublishSubject<Usuario> usuariosAutenticadosPublisher
          = PublishSubject.create();

  private static final int port  = 50051;

  private void start() throws IOException {

    server = ServerBuilder.forPort(port)
            .addService(new AutenticacaoImpl())
            .addService(new UsuarioServiceImpl())
        .build()
        .start();

    logger.info("Servidor iniciado, escutando na porta " + port);

    Runtime.getRuntime().addShutdownHook(new Thread(() -> {
      // Use stderr here since the logger may have been reset by its JVM shutdown hook.
      System.err.println("*** shutting down gRPC server since JVM is shutting down");
      Main.this.stop();
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
    final Main server = new Main();
    server.start();
    server.blockUntilShutdown();
  }

}
