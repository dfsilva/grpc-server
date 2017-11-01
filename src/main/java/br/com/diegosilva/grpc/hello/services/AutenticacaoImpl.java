package br.com.diegosilva.grpc.hello.services;

import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import br.com.diegosilva.grpc.hello.*;
import io.grpc.stub.StreamObserver;

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

        AutenticacaoResponse.Builder response = AutenticacaoResponse.newBuilder();

//        if(usuariosAutenticados.contains(request.getUsuario())){
//            //retorna erro, usuario já autenticado
//            response.setCodigo(-1);
//            response.setMessage("Já existe um usuário autenticado com este login");
//        }else{//retorna sucesso e adiciona o usuario
//            usuariosAutenticados.add(request.getUsuario());
//
//
//            usuariosAutenticadosPublisher
//                    .onNext(Usuario.newBuilder().setOp(GameServer.OperacoesUsuario.INCLUSAO)
//                            .setNome(request.getUsuario()).build());
//
//            response.setCodigo(0);
//            response.setMessage("Usuário autenticado");
//        }

        responseObserver.onNext(response.build());
        responseObserver.onCompleted();

    }
}
