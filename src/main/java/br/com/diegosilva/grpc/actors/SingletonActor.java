package br.com.diegosilva.grpc.actors;

import akka.actor.AbstractActor;
import akka.event.Logging;
import akka.event.LoggingAdapter;

import java.io.Serializable;

public class SingletonActor extends AbstractActor {

    private final LoggingAdapter LOG = Logging.getLogger(getContext().system(), this);

    @Override
    public Receive createReceive() {
        return receiveBuilder().match(Adicionar.class, lc -> {
            LOG.info("Adicionado");
        }).build();
    }

    public static class Adicionar implements Serializable {

    }
}
