package br.com.diegosilva.grpc.actors;

import akka.actor.AbstractActor;
import akka.actor.AbstractLoggingActor;
import akka.actor.ActorRef;
import akka.cluster.pubsub.DistributedPubSub;
import akka.cluster.pubsub.DistributedPubSubMediator;
import akka.event.Logging;
import akka.event.LoggingAdapter;
import br.com.diegosilva.grpc.hello.Usuario;
import io.grpc.stub.StreamObserver;

public class UsuariosPublisherActor extends AbstractLoggingActor {

    private StreamObserver<Usuario> streamObserver;
    public UsuariosPublisherActor(StreamObserver<Usuario> observer) {
        super();
        this.streamObserver = observer;
        ActorRef mediator = DistributedPubSub.get(getContext().system()).mediator();
        mediator.tell(new DistributedPubSubMediator.Subscribe("usuario_entrou", getSelf()), getSelf());
    }

    @Override
    public Receive createReceive() {
        return receiveBuilder()
                .match(Usuario.class, msg ->{
                    log().info("Chegou mensagem no subscriber "+msg);
                    streamObserver.onNext(msg);
                })
                .match(DistributedPubSubMediator.SubscribeAck.class, msg ->
                        log().info("Está inscrito no tópico "+msg))
                .build();
    }
}
