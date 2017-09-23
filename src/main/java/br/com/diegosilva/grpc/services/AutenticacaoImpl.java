package br.com.diegosilva.grpc.services;

import akka.actor.*;
import akka.util.Timeout;
import br.com.diegosilva.grpc.actors.AutenticacaoActor;
import br.com.diegosilva.grpc.hello.*;
import io.grpc.stub.StreamObserver;
import scala.concurrent.duration.Duration;

import java.util.concurrent.TimeUnit;

import static akka.pattern.PatternsCS.ask;

public class AutenticacaoImpl
        extends AutenticacaoGrpc.AutenticacaoImplBase{

    private ActorSystem system;
    private ActorRef authActor;

    public AutenticacaoImpl(ActorSystem system, ActorRef authActor) {
        super();
        this.system = system;
        this.authActor = authActor;
    }

    @Override
    public void autenticar(AutenticacaoRequest request,
                           StreamObserver<AutenticacaoResponse> responseObserver) {
        ask(authActor, new AutenticacaoActor.Login(request.getUsuario()),
                new Timeout(Duration.create(5, TimeUnit.SECONDS))).thenApplyAsync(o -> {
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

