package br.com.diegosilva.grpc.actors;

import akka.actor.*;
import akka.cluster.pubsub.DistributedPubSub;
import akka.cluster.pubsub.DistributedPubSubMediator;
import akka.event.Logging;
import akka.event.LoggingAdapter;
import br.com.diegosilva.grpc.Main;
import br.com.diegosilva.grpc.hello.AutenticacaoResponse;
import br.com.diegosilva.grpc.hello.Usuario;
import redis.clients.jedis.Jedis;

import java.io.Serializable;

public class AutenticacaoActor extends AbstractActor {

    private final LoggingAdapter log = Logging.getLogger(getContext().system(), this);
    private ActorRef mediator;
    private ActorRef usuariosActor;
    private ActorRef replyTo;

    public AutenticacaoActor(){
        super();
        log.info("Construtor de AutenticacaoActor");
        mediator =  DistributedPubSub.get(getContext().system()).mediator();
        usuariosActor = getContext().getSystem().actorOf(Props.create(UsuariosActor.class));
    }

    @Override
    public void preStart() throws Exception {
        super.preStart();
    }

    @Override
    public Receive createReceive() {
        return receiveBuilder()
                .match(Login.class, this::realizarLogin)
                .match(Logoff.class, this::realizarLogoff)
                .match(UsuarioJaExiste.class, this::usuarioJaExiste)
                .match(UsuarioNaoExiste.class, this::usuarioNaoExiste)
                .build();
    }

    private void usuarioNaoExiste(UsuarioNaoExiste usuarioNaoExiste) {
        log.info("Usuario não existe, Enviando mensagem para o mediator");
        AutenticacaoResponse.Builder response = AutenticacaoResponse.newBuilder();

        Usuario usuario = Usuario.newBuilder().setOp(Main.OperacoesUsuario.INCLUSAO)
                .setNome(usuarioNaoExiste.nome).build();

        mediator.tell(new DistributedPubSubMediator.Publish("usuario_entrou", usuario), getSelf());
        usuariosActor.tell(new UsuariosActor.AdicionarUsuario(usuario), getSelf());

        response.setCodigo(0);
        response.setMessage("Usuário autenticado");

        replyTo.tell(response.build(), getSelf());
    }

    private void usuarioJaExiste(UsuarioJaExiste usuarioJaExiste) {
        AutenticacaoResponse.Builder response = AutenticacaoResponse.newBuilder();
        response.setCodigo(-1);
        response.setMessage("Já existe um usuário autenticado com este login");
        replyTo.tell(response.build(), getSelf());
    }

    @Override
    public void postStop() throws Exception {
        super.postStop();
        log.info("Parando o Ator");
    }

    private void realizarLogin(Login login) {
        log.info("Realizando login, vai enviar mensagem para verificar se o usuario existe");
        replyTo = getSender();
        usuariosActor.tell(new UsuariosActor.UsuarioExiste(login.nome), getSelf());
    }

    private void realizarLogoff(Logoff logoff){
        usuariosActor.tell(new UsuariosActor.RemoverUsuario(logoff.nome), self());
        mediator.tell(new DistributedPubSubMediator.Publish("usuario_entrou", Usuario.newBuilder().setOp(Main.OperacoesUsuario.EXCLUCAO)
                        .setNome(logoff.nome).build()), self());
    }


    //Mensagens
    public static class Login implements Serializable {
        final String nome;
        public Login(String nome) {
            this.nome = nome;
        }
    }

    public static class Logoff implements Serializable {
        final String nome;
        public Logoff(String nome) {
            this.nome = nome;
        }
    }

    public static class UsuarioJaExiste implements Serializable {
        final String nome;
        public UsuarioJaExiste(String nome) {
            this.nome = nome;
        }
    }

    public static class UsuarioNaoExiste implements Serializable {
        final String nome;
        public UsuarioNaoExiste(String nome) {
            this.nome = nome;
        }
    }

    public static ActorRef getActorRef(ActorSystem system) {
        return system.actorOf(Props.create(AutenticacaoActor.class));
    }

}
