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
    private Jedis jedis;
    private ActorRef mediator;


    public AutenticacaoActor(){
        super();
        log.info("Construtor de AutenticacaoActor");
        mediator =  DistributedPubSub.get(getContext().system()).mediator();
    }

    @Override
    public void preStart() throws Exception {
        super.preStart();
        jedis = new Jedis();
    }

    @Override
    public Receive createReceive() {
        return receiveBuilder().match(Login.class, lc -> {
            realizarLogin(lc);
        }).match(Logoff.class, v->{
            realizarLogoff(v);
        }).build();
    }

    @Override
    public void postStop() throws Exception {
        super.postStop();
    }

    private void realizarLogin(Login login) {

        AutenticacaoResponse.Builder response = AutenticacaoResponse.newBuilder();

        if (jedis.sismember("usuarios", login.nome)) {
            response.setCodigo(-1);
            response.setMessage("Já existe um usuário autenticado com este login");
            sender().tell(response.build(), getSelf());
        } else {//retorna sucesso e adiciona o usuario
            jedis.sadd("usuarios", login.nome);

            log.info("Enviando mensagem para o mediator");
            mediator.tell(new DistributedPubSubMediator.Publish("usuario_entrou", Usuario.newBuilder().setOp(Main.OperacoesUsuario.INCLUSAO)
                            .setNome(login.nome).build()),
                    getSelf());

            response.setCodigo(0);
            response.setMessage("Usuário autenticado");
            sender().tell(response.build(), getSelf());
        }
    }

    private void realizarLogoff(Logoff logoff){
        jedis.srem("usuarios", logoff.nome);
        mediator.tell(new DistributedPubSubMediator.Publish("usuario_entrou", Usuario.newBuilder().setOp(Main.OperacoesUsuario.EXCLUCAO)
                        .setNome(logoff.nome).build()),
                getSelf());
    }

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

    public static ActorRef getActorRef(ActorSystem system) {
        return system.actorOf(Props.create(AutenticacaoActor.class));
    }

}
