package br.com.diegosilva.grpc.actors;

import akka.actor.AbstractActor;
import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.actor.Props;
import akka.cluster.singleton.ClusterSingletonProxy;
import akka.cluster.singleton.ClusterSingletonProxySettings;
import akka.event.Logging;
import akka.event.LoggingAdapter;
import br.com.diegosilva.grpc.hello.AutenticacaoResponse;
import redis.clients.jedis.Jedis;

import java.io.Serializable;

public class UsuariosActor extends AbstractActor {

    private final LoggingAdapter LOG = Logging.getLogger(getContext().system(), this);
    private Jedis jedis;
    private ActorRef singleton;

    @Override
    public void preStart() throws Exception {
        super.preStart();
        jedis = new Jedis();

//        singleton = getContext().actorOf(ClusterSingletonProxy.props("user/master",
//                ClusterSingletonProxySettings.create(getContext().getSystem())));
    }

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

        AutenticacaoResponse.Builder response = AutenticacaoResponse.newBuilder();

        if (jedis.sismember("usuarios", login.nome)) {
            response.setCodigo(-1);
            response.setMessage("Já existe um usuário autenticado com este login");
            sender().tell(response.build(), getSelf());
        } else {//retorna sucesso e adiciona o usuario
            jedis.sadd("usuarios", login.nome);

          //  usuariosAutenticadosPublisher
//                                    .onNext(Usuario.newBuilder().setOp(Main.OperacoesUsuario.INCLUSAO)
//                                            .setNome(request.getUsuario()).build());

//            singleton.tell(new SingletonActor.Adicionar(), getSelf());

            response.setCodigo(0);
            response.setMessage("Usuário autenticado");
            sender().tell(response.build(), getSelf());
        }
    }

    public static class Login implements Serializable {
        final String nome;
        public Login(String nome) {
            this.nome = nome;
        }
    }

    public static ActorRef getActorRef(ActorSystem system) {
        return system.actorOf(Props.create(UsuariosActor.class));
    }

}
