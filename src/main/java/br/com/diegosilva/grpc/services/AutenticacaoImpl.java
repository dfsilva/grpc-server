package br.com.diegosilva.grpc.services;

import akka.actor.*;
import akka.util.Timeout;
import br.com.diegosilva.grpc.actors.AutenticacaoActor;
import br.com.diegosilva.grpc.hello.AutenticacaoGrpc;
import br.com.diegosilva.grpc.hello.AutenticacaoRequest;
import br.com.diegosilva.grpc.hello.AutenticacaoResponse;
import io.grpc.stub.StreamObserver;
import scala.concurrent.duration.Duration;

import java.util.concurrent.TimeUnit;

import static akka.pattern.PatternsCS.ask;

public class AutenticacaoImpl
        extends AutenticacaoGrpc.AutenticacaoImplBase {

    private ActorSystem system;

    public AutenticacaoImpl(ActorSystem system) {
        super();
        this.system = system;
    }

    @Override
    public void autenticar(AutenticacaoRequest request,
                           StreamObserver<AutenticacaoResponse> responseObserver) {

        ActorRef authActor = AutenticacaoActor.getActorRef(system);
        ask(authActor, new AutenticacaoActor.Login(request.getUsuario()),new Timeout(Duration.create(5, TimeUnit.SECONDS))).thenApplyAsync(o -> {
            responseObserver.onNext((AutenticacaoResponse)o);
            responseObserver.onCompleted();
            return o;
        });
    }

}

