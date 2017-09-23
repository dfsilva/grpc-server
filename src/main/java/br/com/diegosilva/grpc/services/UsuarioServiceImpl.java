package br.com.diegosilva.grpc.services;

import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.util.Timeout;
import br.com.diegosilva.grpc.actors.AutenticacaoActor;
import br.com.diegosilva.grpc.hello.*;
import com.google.protobuf.ByteString;
import com.google.protobuf.InvalidProtocolBufferException;
import io.grpc.stub.StreamObserver;
import io.reactivex.Observable;
import io.reactivex.ObservableSource;
import io.reactivex.functions.Consumer;
import io.reactivex.functions.Function;
import io.reactivex.functions.Predicate;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPubSub;
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

        Jedis jedis = new Jedis();

        Set<String> usuarios = jedis.smembers("usuarios");
                Observable.fromIterable(usuarios)
                .filter(new Predicate<String>() {
                    @Override
                    public boolean test(String s) throws Exception {
                        return !s.equals(request.getNome());
                    }
                }).concatMap(new Function<String, ObservableSource<String>>() {
            @Override
            public ObservableSource<String> apply(String s) throws Exception {
                return Observable.just(s).delay(1, TimeUnit.SECONDS);
            }
        }).subscribe(new Consumer<String>() {
            @Override
            public void accept(String s) throws Exception {
                responseObserver.onNext(Usuario.newBuilder().setNome(s).build());
            }
        });

        jedis.subscribe(new JedisPubSub() {
            @Override
            public void onMessage(String channel, String message) {
                try {
                    Usuario usuario = Usuario.parseFrom(ByteString.copyFromUtf8(message));
                    System.out.println("Deu certo a conversao: "+usuario.getNome());
                    System.out.println(usuario);
                    responseObserver.onNext(usuario);
                } catch (InvalidProtocolBufferException e) {
                    e.printStackTrace();
                }
            }
        },"usuario_entrou");
    }
}
