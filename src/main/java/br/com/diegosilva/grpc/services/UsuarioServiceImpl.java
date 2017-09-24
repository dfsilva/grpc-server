package br.com.diegosilva.grpc.services;

import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.actor.Props;
import akka.util.Timeout;
import br.com.diegosilva.grpc.Main;
import br.com.diegosilva.grpc.actors.AutenticacaoActor;
import br.com.diegosilva.grpc.actors.UsuariosActor;
import br.com.diegosilva.grpc.hello.*;
import io.grpc.stub.StreamObserver;
import io.reactivex.Observable;
import io.reactivex.ObservableSource;
import io.reactivex.functions.Consumer;
import io.reactivex.functions.Function;
import io.reactivex.functions.Predicate;
import redis.clients.jedis.Jedis;
import scala.concurrent.duration.Duration;

import java.util.Set;
import java.util.concurrent.TimeUnit;

import static akka.pattern.PatternsCS.ask;


public class UsuarioServiceImpl extends UsuariosGrpc.UsuariosImplBase {

    private ActorSystem system;

    public UsuarioServiceImpl(ActorSystem system) {
        super();
        this.system = system;
    }

    @Override
    public void listarUsuarios(Usuario request, StreamObserver<Usuario> responseObserver) {

        ActorRef usuariosActor = system.actorOf(Props.create(UsuariosActor.class, () -> {
            return new UsuariosActor(responseObserver);
        }), "subscriber_"+request.getNome());

        ask(usuariosActor, new UsuariosActor.ListarUsuarios(request.getNome()),
                new Timeout(Duration.create(5, TimeUnit.SECONDS))).thenApplyAsync(o -> {

            Set<String> usuarios = (Set<String>) o;
            Observable.fromIterable(usuarios)
                    .filter(new Predicate<String>() {
                        @Override
                        public boolean test(String s) throws Exception {
                            return !s.equals(request.getNome());
                        }
                    }).concatMap(new Function<String, ObservableSource<String>>() {
                @Override
                public ObservableSource<String> apply(String s) throws Exception {
                    return Observable.just(s).delay(500, TimeUnit.MILLISECONDS);
                }
            }).subscribe(new Consumer<String>() {
                @Override
                public void accept(String s) throws Exception {
                    responseObserver.onNext(Usuario.newBuilder().setNome(s).build());
                }
            });

            return o;
        });
    }
}
