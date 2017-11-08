package br.com.diegosilva.grpc.hello.actors;

import akka.actor.AbstractLoggingActor;
import akka.actor.ActorRef;
import akka.cluster.Cluster;
import akka.cluster.ddata.*;
import br.com.diegosilva.grpc.hello.Usuario;
import scala.concurrent.duration.Duration;

import java.io.Serializable;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

public class UsuariosActor extends AbstractLoggingActor{

    private final Cluster no = Cluster.get(context().system());

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

    private Object ultimaMensagemRecebida;

    @Override
    public Receive createReceive() {
        return receiveBuilder()
                .match(ListarUsuarios.class, this::listarUsuarios)
                .match(AdicionarUsuario.class, this::adicionarUsuario)
                .match(RemoverUsuario.class, this::removerUsuario)
                .match(UsuarioExiste.class, this::isUsuarioExiste)
                .match(Replicator.GetSuccess.class, this::eUmResponseGetUsuarios,
                        this::recebeuGetSucesso)
                .match(Replicator.GetFailure.class, this::eUmResponseGetUsuarios,
                        this::recebeuGetFalha)
                .match(Replicator.NotFound.class, this::eUmResponseGetUsuarios,
                        this::recebeuNotFound)
                .match(Replicator.UpdateResponse.class, updateResponse -> {
                    log().info("Atualizacao de uma acao de update");
                    log().info(updateResponse.toString());
                })
                .build();
    }

    private void recebeuGetSucesso(Replicator.GetSuccess<ORSet<String>> response){
        log().info("Valores no banco de dados: {}", response.dataValue().getElements());
        ActorRef responderPara = (ActorRef) response.getRequest().get();

        if(ultimaMensagemRecebida instanceof UsuarioExiste){
          log().info("Verificando se o usuario existe");
          UsuarioExiste usuarioExiste = (UsuarioExiste) ultimaMensagemRecebida;
          ultimaMensagemRecebida = null;

          if(response.dataValue().getElements().contains(usuarioExiste.nomeUsuario)){
              responderPara.tell(new AutenticacaoActor
                      .UsuarioJaExiste(usuarioExiste.nomeUsuario), self());
          }else{
              responderPara.tell(new AutenticacaoActor
                      .UsuarioNaoExiste(usuarioExiste.nomeUsuario), self());
          }

        }else{
            responderPara.tell(response.dataValue().getElements(), self());
        }
    }

    private void recebeuGetFalha(Replicator.GetFailure<ORSet<String>> response){
        log().info("Erro ao obter a listagem");
        bancoDadosReplicator.tell(new Replicator
                .Get<>(usuariosKey, estrategiaLeitura,
                Optional.of(sender())), getSelf());
    }

    private void recebeuNotFound(Replicator.NotFound<ORSet<String>> response){
        //TODO: IMPLEMENTAR
    }

    private void listarUsuarios(ListarUsuarios msg){
        bancoDadosReplicator.tell(new Replicator
                .Get<>(usuariosKey, estrategiaLeitura,
                Optional.of(sender())), getSelf());
    }

    private void adicionarUsuario(AdicionarUsuario msg){

        log().info("Adicionando o usuario {}", msg);

        Replicator.Update<ORSet<String>> update = new Replicator.Update<ORSet<String>>(
                usuariosKey,
                ORSet.create(),
                estrategiaEscrita,
                atual -> atual.add(no, msg.usuario.getNome())
        );

        bancoDadosReplicator.tell(update, getSelf());

    }

    private void removerUsuario(RemoverUsuario msg){
        log().info("Remover o usuario {}", msg);

        Replicator.Update<ORSet<String>> update = new Replicator.Update<ORSet<String>>(
                usuariosKey,
                ORSet.create(),
                estrategiaEscrita,
                atual -> atual.remove(no, msg.nomeUsuario)
        );

        bancoDadosReplicator.tell(update, getSelf());

    }

    private void isUsuarioExiste(UsuarioExiste msg){
        log().info("Enviando mensagem para replicator, verificar se o usuario existe");
        ultimaMensagemRecebida = msg;
        bancoDadosReplicator.tell(new Replicator.Get<>(usuariosKey,
                estrategiaLeitura, Optional.of(sender())), getSelf());
    }

    private boolean eUmResponseGetUsuarios(Replicator.GetResponse response){
        return response.key().equals(usuariosKey)
                && (response.getRequest().orElse(null) instanceof ActorRef);
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
