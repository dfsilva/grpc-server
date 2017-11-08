package br.com.diegosilva.grpc.hello.services;

import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.util.Timeout;
import br.com.diegosilva.grpc.hello.*;
import br.com.diegosilva.grpc.hello.actors.AutenticacaoActor;
import io.grpc.stub.StreamObserver;
import akka.actor.*;
import scala.concurrent.duration.Duration;

import static akka.pattern.PatternsCS.ask;

import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

public class AutenticacaoImpl
        extends AutenticacaoGrpc.AutenticacaoImplBase {

    private ActorSystem system;
    private ActorRef authActor;
    private static final Logger logger = Logger.getLogger(AutenticacaoImpl.class.getName());

    public AutenticacaoImpl(ActorSystem system, ActorRef actorRef){
        super();
        this.system = system;
        this.authActor = actorRef;
    }


    @Override
    public void autenticar(AutenticacaoRequest request,
                           StreamObserver<AutenticacaoResponse> responseObserver) {
        ask(authActor, new AutenticacaoActor.Login(request.getUsuario()),
                new Timeout(Duration.create(5, TimeUnit.SECONDS)))
                .thenApplyAsync(resposta -> {
                    responseObserver.onNext((AutenticacaoResponse) resposta);
                    responseObserver.onCompleted();

                    return resposta;
                });
    }
}
