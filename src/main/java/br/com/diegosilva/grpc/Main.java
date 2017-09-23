package br.com.diegosilva.grpc;

import akka.actor.ActorSystem;
import akka.actor.PoisonPill;
import akka.actor.Props;
import akka.cluster.singleton.ClusterSingletonManager;
import akka.cluster.singleton.ClusterSingletonManagerSettings;
import br.com.diegosilva.grpc.actors.AutenticacaoActor;
import br.com.diegosilva.grpc.actors.SingletonActor;
import br.com.diegosilva.grpc.hello.Usuario;
import br.com.diegosilva.grpc.services.AutenticacaoServiceImpl;
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

    public static class OperacoesUsuario {
        public static final int INCLUSAO = 0;
        public static final int EXCLUCAO = 1;
    }

    private Server server;
    private static final int port = 50051;

    private void start() throws IOException {

        Config config = ConfigFactory.load();

        ActorSystem system = ActorSystem.create("ClusterSystem", config);


        ClusterSingletonManagerSettings settings =
                ClusterSingletonManagerSettings.create(system).withRole("compute");

        system.actorOf(
                ClusterSingletonManager.props(
                        Props.create(SingletonActor.class),
                        PoisonPill.getInstance(),
                        settings),"master");

        server = ServerBuilder.forPort(port)
                .addService(new AutenticacaoServiceImpl(system, AutenticacaoActor.getActorRef(system)))
                .addService(new UsuarioServiceImpl(system))
                .build()
                .start();

        logger.info("Servidor iniciado, escutando na porta " + port);

        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            System.err.println("*** Desligando o servidor");
            stop();
            System.err.println("*** Servidor desligado");
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
