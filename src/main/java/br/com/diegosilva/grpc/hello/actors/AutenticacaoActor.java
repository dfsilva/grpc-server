package br.com.diegosilva.grpc.hello.actors;

import akka.actor.*;
import akka.cluster.pubsub.DistributedPubSub;
import akka.cluster.pubsub.DistributedPubSubMediator;
import br.com.diegosilva.grpc.hello.AutenticacaoResponse;
import br.com.diegosilva.grpc.hello.GameServer;
import br.com.diegosilva.grpc.hello.Usuario;

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
        log().info("Realizando login, vai enviar mensagem para verificar se o usuario existe");
        responderPara = getSender();
        usuariosActor.tell(new UsuariosActor.UsuarioExiste(mensagem.nome), getSelf());
    }

    private void realizarLogoff(Logoff mensagem){

    }

    public void tratarUsuarioExistente(UsuarioJaExiste mensagem){
        AutenticacaoResponse.Builder response = AutenticacaoResponse.newBuilder();
        response.setCodigo(-1);
        response.setMessage("Já existe um usuário autenticado com este login");
        responderPara.tell(response.build(), getSelf());
    }

    public void tratarUsuarioInexistente(UsuarioNaoExiste mensagem){
        log().info("Usuario não existe, Enviando mensagem para o mediador");
        AutenticacaoResponse.Builder response = AutenticacaoResponse.newBuilder();
        Usuario usuario = Usuario.newBuilder().setOp(GameServer.OperacoesUsuario.INCLUSAO)
                .setNome(mensagem.nome).build();
        pubSubMediator.tell(new DistributedPubSubMediator
                .Publish("usuario_entrou", usuario), getSelf());
        usuariosActor.tell(new UsuariosActor.AdicionarUsuario(usuario), getSelf());

        response.setCodigo(0);
        response.setMessage("Usuario autenticado");

        responderPara.tell(response.build(), getSelf());

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
