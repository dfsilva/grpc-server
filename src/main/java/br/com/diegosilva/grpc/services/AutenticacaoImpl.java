package br.com.diegosilva.grpc.services;

import akka.actor.*;
import akka.util.Timeout;
import br.com.diegosilva.grpc.actors.AutenticacaoActor;
import br.com.diegosilva.grpc.hello.AutenticacaoGrpc;
import br.com.diegosilva.grpc.hello.AutenticacaoRequest;
import br.com.diegosilva.grpc.hello.AutenticacaoResponse;
import io.grpc.stub.StreamObserver;
import scala.concurrent.duration.Duration;

import java.util.concurrent.CompletionStage;
import java.util.concurrent.TimeUnit;

import static akka.pattern.PatternsCS.ask;

public class AutenticacaoImpl
        extends AutenticacaoGrpc.AutenticacaoImplBase {

    private ActorSystem system;


    //        final Props creator =  Props.create(new Creator<AbstractActor>() {
//            @Override
//            public AbstractActor create() throws Exception {
//                return new AbstractActor() {
//                    @Override
//                    public Receive createReceive() {
//                        return receiveBuilder()
//                                .match(SucessoAutenticacao.class, v -> {
////
//
//                                })
//                                .match(ErroAutenticacao.class, v -> {
//
//                                }).build();
//                    }
//
//                    @Override
//                    public void postStop() throws Exception {
//                        super.postStop();
//                    }
//                };
//            }
//        });

//        ActorRef serviceActor = system.actorOf(creator);

//        if(usuariosAutenticados.contains(request.getUsuario())){
//            //retorna erro, usuario já autenticado
//
//        }else{//retorna sucesso e adiciona o usuario
//            usuariosAutenticados.add(request.getUsuario());
//        }

    public AutenticacaoImpl(ActorSystem system) {
        super();
        this.system = system;
    }

    @Override
    public void autenticar(AutenticacaoRequest request,
                           StreamObserver<AutenticacaoResponse> responseObserver) {
        ActorSelection authActor = AutenticacaoActor.getActorSelection(system);

        System.out.println("inicio");
        ask(authActor, new AutenticacaoActor.Login(request.getUsuario()),new Timeout(Duration.create(5, TimeUnit.SECONDS))).thenApplyAsync(o -> {
            return o;
        });

        System.out.println("fim");
    }

}

