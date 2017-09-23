package br.com.diegosilva.grpc.actors;

import akka.actor.AbstractActor;
import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.cluster.singleton.ClusterSingletonProxy;
import akka.cluster.singleton.ClusterSingletonProxySettings;
import akka.event.Logging;
import akka.event.LoggingAdapter;

import java.io.Serializable;

public class SingletonActor extends AbstractActor {

    private final LoggingAdapter LOG = Logging.getLogger(getContext().system(), this);

    @Override
    public Receive createReceive() {
        return receiveBuilder().match(Adicionar.class, lc -> {
            LOG.info("Adicionado");
        }).matchAny(obj->{
            LOG.info("Adicionado");
        }).build();
    }


    public static ActorRef getActorRef(ActorSystem system){
        ClusterSingletonProxySettings proxySettings =
                ClusterSingletonProxySettings.create(system).withRole("compute");
        return system.actorOf(ClusterSingletonProxy.props("/user/master",
                proxySettings), "masterProxy");
    }

    public static class Adicionar implements Serializable {

    }
}
