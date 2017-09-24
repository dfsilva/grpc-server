package br.com.diegosilva.grpc.actors;

import akka.actor.AbstractActor;

import akka.actor.*;
import akka.cluster.pubsub.DistributedPubSub;
import akka.cluster.pubsub.DistributedPubSubMediator;
import akka.event.Logging;
import akka.event.LoggingAdapter;
import br.com.diegosilva.grpc.Main;
import br.com.diegosilva.grpc.hello.Usuario;
import io.grpc.stub.StreamObserver;
import io.reactivex.Observable;
import io.reactivex.ObservableSource;
import io.reactivex.functions.Consumer;
import io.reactivex.functions.Function;
import io.reactivex.functions.Predicate;
import redis.clients.jedis.Jedis;

import java.io.Serializable;
import java.util.Set;
import java.util.concurrent.TimeUnit;


public class UsuariosActor extends AbstractActor {

    private LoggingAdapter log = Logging.getLogger(getContext().system(), this);
    private StreamObserver<Usuario> streamObserver;
    private Jedis jedis;

    public UsuariosActor(StreamObserver<Usuario> observer) {
        super();
        this.streamObserver = observer;
        ActorRef mediator = DistributedPubSub.get(getContext().system()).mediator();
        mediator.tell(new DistributedPubSubMediator.Subscribe("usuario_entrou", getSelf()), getSelf());
    }

    @Override
    public void preStart() throws Exception {
        super.preStart();
        jedis = new Jedis();
    }

    @Override
    public Receive createReceive() {
        return receiveBuilder()
                .match(Usuario.class, msg ->{
                    log.info("Chegou mensagem no subscriber "+msg);
                    streamObserver.onNext(msg);
                })
                .match(ListarUsuarios.class, msg->{
                    listarUsuarios(msg);
                })
                .match(DistributedPubSubMediator.SubscribeAck.class, msg ->
                        log.info("Inscrito no topico "+msg))
                .build();
    }


    private void listarUsuarios(ListarUsuarios msg){
        Set<String> usuarios = jedis.smembers("usuarios");
        sender().tell(usuarios, getSelf());
    }

    public static class ListarUsuarios implements Serializable {
        final String nomeUsuarioLogado;
        public ListarUsuarios(String nome) {
            this.nomeUsuarioLogado = nome;
        }
    }
}