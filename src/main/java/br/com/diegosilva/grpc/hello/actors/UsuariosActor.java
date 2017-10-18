package br.com.diegosilva.grpc.hello.actors;

import akka.actor.AbstractLoggingActor;

import java.io.Serializable;

public class UsuariosActor extends AbstractLoggingActor{

    @Override
    public Receive createReceive() {
        return receiveBuilder()
                .match(ListarUsuarios.class, this::listarUsuarios)
                .build();
    }

    private void listarUsuarios(ListarUsuarios msg){

    }

    public static class ListarUsuarios implements Serializable{
        final String nomeUsuarioLogado;

        public ListarUsuarios(String nomeUsuarioLogado) {
            this.nomeUsuarioLogado = nomeUsuarioLogado;
        }
    }

}
