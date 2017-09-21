package br.com.diegosilva.grpc;

import akka.actor.ActorSystem;
import akka.actor.Props;
import br.com.diegosilva.grpc.actors.AutenticacaoActor;
import br.com.diegosilva.grpc.hello.Usuario;
import br.com.diegosilva.grpc.services.AutenticacaoImpl;
import br.com.diegosilva.grpc.services.UsuarioServiceImpl;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import io.grpc.Server;
import io.grpc.ServerBuilder;
import io.reactivex.subjects.PublishSubject;

import java.io.IOException;
import java.util.logging.Logger;


public class Main {

    private static final Logger logger = Logger.getLogger(Main.class.getName());

    private static class OperacoesUsuario {
        public static final int INCLUSAO = 0;
        public static final int EXCLUCAO = 1;
    }

    private Server server;

    private static PublishSubject<Usuario> usuariosAutenticadosPublisher
            = PublishSubject.create();

    private static final int port = 50051;


    private void start() throws IOException {


        Config config = ConfigFactory.parseString(
                "akka.remote.netty.tcp.port=" + 2552).withFallback(
                ConfigFactory.load());
        ActorSystem system = ActorSystem.create("ClusterSystem", config);

        AutenticacaoActor.getActorRef(system);

        server = ServerBuilder.forPort(port)
                .addService(new AutenticacaoImpl(system))
                .addService(new UsuarioServiceImpl())
                .build()
                .start();

        logger.info("Servidor iniciado, escutando na porta " + port);

        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            // Use stderr here since the logger may have been reset by its JVM shutdown hook.
            System.err.println("*** shutting down gRPC server since JVM is shutting down");
            stop();
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
