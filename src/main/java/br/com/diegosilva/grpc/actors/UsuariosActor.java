package br.com.diegosilva.grpc.actors;


import akka.actor.*;
import akka.cluster.Cluster;
import akka.cluster.ddata.*;
import br.com.diegosilva.grpc.hello.Usuario;
import scala.concurrent.duration.Duration;

import akka.cluster.ddata.Replicator;
import akka.cluster.ddata.Replicator.GetFailure;
import akka.cluster.ddata.Replicator.GetSuccess;
import akka.cluster.ddata.Replicator.ReadConsistency;
import akka.cluster.ddata.Replicator.ReadMajority;
import akka.cluster.ddata.Replicator.Update;
import akka.cluster.ddata.Replicator.WriteConsistency;
import akka.cluster.ddata.Replicator.WriteMajority;

import java.io.Serializable;
import java.util.Optional;

import static java.util.concurrent.TimeUnit.SECONDS;


public class UsuariosActor extends AbstractLoggingActor {

    private final Cluster node = Cluster.get(context().system());
    private final ActorRef replicator = DistributedData.get(context().system()).replicator();
    private final Key<ORSet<String>> dataKey = ORSetKey.create("usuarios_key");
    private final WriteConsistency writeMajority = new WriteMajority(Duration.create(3, SECONDS));
    private final static ReadConsistency readMajority = new ReadMajority(Duration.create(3, SECONDS));

    private Object lastReceive;

    @Override
    public void preStart() throws Exception {
        super.preStart();
    }

    @Override
    public Receive createReceive() {
        return receiveBuilder()
                .match(ListarUsuarios.class, this::listarUsuarios)
                .match(AdicionarUsuario.class, this::adicionarUsuario)
                .match(RemoverUsuario.class, this::removerUsuario)
                .match(UsuarioExiste.class, this::isUsuarioExiste)
                .match(GetSuccess.class, this::isResponseToGetUsuarios, this::receiveGetSuccess)
                .match(GetFailure.class, this::isResponseToGetUsuarios, this::receiveGetFailure)
                .match(Replicator.NotFound.class, this::isResponseToGetUsuarios, this::receiveNotFound)
                .match(Replicator.UpdateResponse.class, updateResponse -> {
                    log().info("Atualizacao de update");
                    log().info(updateResponse.toString());
                    self().tell(new ListarUsuarios(""), self());
                })

                .build();
    }

    private void isUsuarioExiste(UsuarioExiste usuarioExiste){
        log().info("Enviando mensagem para replicator, verificar se o usuario existe");
        lastReceive = usuarioExiste;
        Optional<Object> ctx = Optional.of(sender());
        replicator.tell(new Replicator.Get<>(dataKey, readMajority, ctx), getSelf());
    }

    private void adicionarUsuario(AdicionarUsuario msg){

        log().info("Adicionando o usuario {}", msg);

        Replicator.Update<ORSet<String>> update = new Update<>(
                dataKey,
                ORSet.create(),
                writeMajority,
                curr ->  curr.add(node, msg.usuario.getNome()));

        replicator.tell(update, self());
    }

    private void removerUsuario(RemoverUsuario msg){
        Replicator.Update<ORSet<String>> update = new Update<>(
                dataKey,
                ORSet.create(),
                writeMajority,
                curr ->  curr.remove(node, msg.nomeUsuario));
        replicator.tell(update, getSelf());
    }

    private void listarUsuarios(ListarUsuarios msg){
        Optional<Object> ctx = Optional.of(sender());
        replicator.tell(new Replicator.Get<>(dataKey, readMajority,
                ctx), self());
    }

    private boolean isResponseToGetUsuarios(Replicator.GetResponse<?> response) {
        return response.key().equals(dataKey) &&
                (response.getRequest().orElse(null) instanceof ActorRef);
    }

    private void receiveGetSuccess(Replicator.GetSuccess<ORSet<String>> g) {
        log().info("Valores no banco de dados: {}", g.dataValue().getElements());
        ActorRef replyTo = (ActorRef) g.getRequest().get();
        if(lastReceive instanceof UsuarioExiste){
            log().info("Verificando se o usuario existe");
            UsuarioExiste usuarioExiste = ((UsuarioExiste)lastReceive);
            lastReceive = null;
            if(g.dataValue().getElements().contains(usuarioExiste.nomeUsuario)){
                replyTo.tell(new AutenticacaoActor.UsuarioJaExiste(usuarioExiste.nomeUsuario), self());
            }else {
                replyTo.tell(new AutenticacaoActor.UsuarioNaoExiste(usuarioExiste.nomeUsuario), self());
            }
        }else{
            replyTo.tell(g.dataValue().getElements(), self());
        }
    }

    private void receiveGetFailure(Replicator.GetFailure<ORSet<String>> f) {
        log().info("Erro ao obter a listagem");
        Optional<Object> ctx = Optional.of(sender());
        replicator.tell(new Replicator.Get<>(dataKey, readMajority, ctx), self());
    }

    private void receiveNotFound(Replicator.NotFound<ORSet<String>> values){
        log().info("Nenhum usuario cadastrado");
        if(lastReceive instanceof UsuarioExiste){
            log().info("Enviando mensagem informando que o usuario nao está cadastrado");
            UsuarioExiste usuarioExiste = ((UsuarioExiste)lastReceive);
            lastReceive = null;
            ActorRef replyTo = (ActorRef) values.getRequest().get();
            replyTo.tell(new AutenticacaoActor.UsuarioNaoExiste(usuarioExiste.nomeUsuario), self());
        }
    }


    //#Mensagens
    public static class ListarUsuarios implements Serializable {
        final String nomeUsuarioLogado;
        public ListarUsuarios(String nome) {
            this.nomeUsuarioLogado = nome;
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