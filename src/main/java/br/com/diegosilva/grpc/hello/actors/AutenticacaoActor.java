package br.com.diegosilva.grpc.hello.actors;

import akka.actor.*;
import akka.cluster.pubsub.DistributedPubSub;

import java.io.Serializable;

public class AutenticacaoActor extends AbstractLoggingActor {

    private ActorRef pubSubMediator;
    private ActorRef usuariosActor;
    private ActorRef responderPara;


    public AutenticacaoActor(){
        super();
        log().info("Construtor de AutenticacaoActor");
        pubSubMediator = DistributedPubSub.get(getContext().system()).mediator();
        usuariosActor = getContext().system().actorOf(Props.create(UsuariosActor.class));
    }

    @Override
    public Receive createReceive() {
        return receiveBuilder()
                .match(Login.class, this::realizarLogin)
                .match(Logoff.class, this::realizarLogoff)
                .match(UsuarioJaExiste.class, this::tratarUsuarioExistente)
                .match(UsuarioNaoExiste.class, this::tratarUsuarioInexistente)
                .build();
    }

    private void realizarLogin(Login mensagem){

    }

    private void realizarLogoff(Logoff mensagem){

    }

    public void tratarUsuarioExistente(UsuarioJaExiste mensagem){

    }

    public void tratarUsuarioInexistente(UsuarioNaoExiste mensagem){

    }


    //Tipos de Mensagens
    public static class Login implements Serializable{
        final String nome;

        public Login(String nome){
            this.nome = nome;
        }
    }

    public static class Logoff implements Serializable{
        final String nome;

        public Logoff(String nome){
            this.nome = nome;
        }
    }

    public static class UsuarioJaExiste implements Serializable{
        final String nome;

        public UsuarioJaExiste(String nome){
            this.nome = nome;
        }
    }

    public static class UsuarioNaoExiste implements Serializable{
        final String nome;

        public UsuarioNaoExiste(String nome){
            this.nome = nome;
        }
    }

    public static ActorRef getActorRef(ActorSystem system){
        return system.actorOf(Props.create(AutenticacaoActor.class));
    }



}
