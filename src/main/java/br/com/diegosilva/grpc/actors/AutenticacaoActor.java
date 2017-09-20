package br.com.diegosilva.grpc.actors;

import akka.actor.AbstractActor;
import akka.event.Logging;
import akka.event.LoggingAdapter;

import java.util.ArrayList;
import java.util.List;

public class AutenticacaoActor extends AbstractActor{

    private final LoggingAdapter LOG = Logging.getLogger(getContext().system(), this);

    private List<String> usuariosAutenticados = new ArrayList<>();

    @Override
    public Receive createReceive() {
        return receiveBuilder().match(Login.class, lc ->{

        }).build();
    }


    private void realizarLogin(Login login){


    }

    public static class Login{

        final String nomeUsuario;

        public Login(String nomeUsuario) {
            this.nomeUsuario = nomeUsuario;
        }
    }
}
