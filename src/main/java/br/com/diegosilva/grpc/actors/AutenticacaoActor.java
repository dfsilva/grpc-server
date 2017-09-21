package br.com.diegosilva.grpc.actors;

import akka.actor.*;
import akka.event.Logging;
import akka.event.LoggingAdapter;
import br.com.diegosilva.grpc.hello.AutenticacaoRequest;
import br.com.diegosilva.grpc.hello.AutenticacaoResponse;
import br.com.diegosilva.grpc.services.AutenticacaoImpl;
import io.grpc.stub.StreamObserver;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class AutenticacaoActor extends AbstractActor {

    private final LoggingAdapter LOG = Logging.getLogger(getContext().system(), this);

    private List<String> usuariosAutenticados = new ArrayList<>();

    @Override
    public Receive createReceive() {
        return receiveBuilder().match(Login.class, lc -> {
            realizarLogin(lc);
        }).build();
    }

    @Override
    public void postStop() throws Exception {
        super.postStop();
    }

    private void realizarLogin(Login login) {

//        AutenticacaoResponse.Builder response = AutenticacaoResponse.newBuilder();
//
        if (usuariosAutenticados.contains(login.nome)) {
//            response.setCodigo(-1);
//            response.setMessage("Já existe um usuário autenticado com este login");
           // sender().tell(new AutenticacaoImpl.ErroAutenticacao(), getSelf());

            sender().tell("Erro", getSelf());
        } else {//retorna sucesso e adiciona o usuario
            usuariosAutenticados.add(login.nome);
           // sender().tell(new AutenticacaoImpl.SucessoAutenticacao(), getSelf());
          //  usuariosAutenticadosPublisher
//                                    .onNext(Usuario.newBuilder().setOp(Main.OperacoesUsuario.INCLUSAO)
//                                            .setNome(request.getUsuario()).build());
//            response.setCodigo(0);
//            response.setMessage("Usuário autenticado");
            sender().tell("Sucesso", getSelf());
        }
//
//        login.responseObserver.onNext(response.build());
//        login.responseObserver.onCompleted();


    }

    public static class Login implements Serializable {

        final String nome;

        public Login(String nome) {
            this.nome = nome;
        }
    }


    public static ActorRef getActorRef(ActorSystem system) {
       // return system.actorFor("autenticacaoActor");
        return system.actorOf(Props.create(AutenticacaoActor.class), "autenticacaoActor");
    }

    public static ActorSelection getActorSelection(ActorSystem system) {
        // return system.actorFor("autenticacaoActor");
        // return system.actorOf(Props.create(AutenticacaoActor.class));
        return system.actorSelection("autenticacaoActor");
    }
}
