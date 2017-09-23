package br.com.diegosilva.grpc.actors;

import akka.actor.*;
import akka.event.Logging;
import akka.event.LoggingAdapter;
import br.com.diegosilva.grpc.Main;
import br.com.diegosilva.grpc.hello.AutenticacaoResponse;
import br.com.diegosilva.grpc.hello.Usuario;
import redis.clients.jedis.Jedis;

import java.io.Serializable;

public class AutenticacaoActor extends AbstractActor {

    private final LoggingAdapter LOG = Logging.getLogger(getContext().system(), this);
    private Jedis jedis;
    private ActorRef singleton;

    @Override
    public void preStart() throws Exception {
        super.preStart();
        jedis = new Jedis();
        singleton = SingletonActor.getActorRef(getContext().system());
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
            jedis.publish("usuario_entrou", Usuario.newBuilder().setOp(Main.OperacoesUsuario.INCLUSAO)
                                            .setNome(login.nome).build().toByteString().toStringUtf8());

            //singleton.tell(new SingletonActor.Adicionar(), getSelf());

            response.setCodigo(0);
            response.setMessage("Usuário autenticado");
            sender().tell(response.build(), getSelf());
        }
    }

    private void realizarLogoff(Logoff logoff){
        jedis.srem("usuarios", logoff.nome);
        jedis.publish("usuario_entrou", Usuario.newBuilder().setOp(Main.OperacoesUsuario.EXCLUCAO)
                .setNome(logoff.nome).build().toByteString().toStringUtf8());
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
