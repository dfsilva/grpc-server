package br.com.diegosilva.grpc.services;

import akka.actor.*;
import akka.util.Timeout;
import br.com.diegosilva.grpc.actors.AutenticacaoActor;
import br.com.diegosilva.grpc.hello.*;
import io.grpc.stub.StreamObserver;
import scala.concurrent.duration.Duration;

import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

import static akka.pattern.PatternsCS.ask;

public class AutenticacaoServiceImpl
        extends AutenticacaoGrpc.AutenticacaoImplBase{

    private ActorSystem system;
    private ActorRef authActor;
    private static final Logger logger = Logger.getLogger(AutenticacaoServiceImpl.class.getName());


    public AutenticacaoServiceImpl(ActorSystem system, ActorRef authActor) {
        super();
        this.system = system;
        this.authActor = authActor;
    }

    @Override
    public void autenticar(AutenticacaoRequest request,
                           StreamObserver<AutenticacaoResponse> responseObserver) {
        ask(authActor, new AutenticacaoActor.Login(request.getUsuario()),
                new Timeout(Duration.create(5, TimeUnit.SECONDS))).thenApplyAsync(o -> {
                    logger.info("Retorno do servico: "+o);
            responseObserver.onNext((AutenticacaoResponse)o);
            responseObserver.onCompleted();
            return o;
        });
    }

    @Override
    public void sair(SairRequest request, StreamObserver<SairResponse> responseObserver) {
        ask(authActor, new AutenticacaoActor.Logoff(request.getNome()),
                new Timeout(Duration.create(5, TimeUnit.SECONDS))).thenApplyAsync(o -> {
            responseObserver.onNext((SairResponse) o);
            responseObserver.onCompleted();
            return o;
        });
    }

}

