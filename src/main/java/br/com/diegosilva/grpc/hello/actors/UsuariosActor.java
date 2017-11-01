package br.com.diegosilva.grpc.hello.actors;

import akka.actor.AbstractLoggingActor;
import akka.actor.ActorRef;
import akka.cluster.ddata.*;
import br.com.diegosilva.grpc.hello.Usuario;
import scala.concurrent.duration.Duration;

import java.io.Serializable;
import java.util.concurrent.TimeUnit;

public class UsuariosActor extends AbstractLoggingActor{

    private ActorRef bancoDadosReplicator =
            DistributedData.get(context().system()).replicator();
    private final Key<ORSet<String>> usuariosKey =
            ORSetKey.create("usuarios_key");

    private final Replicator.WriteConsistency estrategiaEscrita =
            new Replicator.WriteMajority(
                    Duration.create(3, TimeUnit.SECONDS));
    private final Replicator.ReadConsistency estrategiaLeitura =
            new Replicator.ReadMajority(
                    Duration.create(3, TimeUnit.SECONDS));

    @Override
    public Receive createReceive() {
        return receiveBuilder()
                .match(ListarUsuarios.class, this::listarUsuarios)
                .match(AdicionarUsuario.class, this::adicionarUsuario)
                .match(RemoverUsuario.class, this::removerUsuario)
                .match(UsuarioExiste.class, this::isUsuarioExiste)
                .build();
    }

    private void listarUsuarios(ListarUsuarios msg){

    }

    private void adicionarUsuario(AdicionarUsuario msg){

    }

    private void removerUsuario(RemoverUsuario msg){

    }

    private void isUsuarioExiste(UsuarioExiste msg){

    }

    //Mensagens
    public static class ListarUsuarios implements Serializable{
        final String nomeUsuarioLogado;

        public ListarUsuarios(String nomeUsuarioLogado) {
            this.nomeUsuarioLogado = nomeUsuarioLogado;
        }
    }

    public static class AdicionarUsuario implements Serializable{
        final Usuario usuario;

        public AdicionarUsuario(Usuario usuario) {
            this.usuario = usuario;
        }
    }

    public static class RemoverUsuario implements Serializable{
        final String nomeUsuario;

        public RemoverUsuario(String nomeUsuario) {
            this.nomeUsuario = nomeUsuario;
        }
    }

    public static class UsuarioExiste implements Serializable{
        final String nomeUsuario;

        public UsuarioExiste(String nomeUsuario) {
            this.nomeUsuario = nomeUsuario;
        }
    }

}
