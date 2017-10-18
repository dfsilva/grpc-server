package br.com.diegosilva.grpc.hello.services;

import br.com.diegosilva.grpc.hello.*;
import io.grpc.stub.StreamObserver;
import io.reactivex.Observable;
import io.reactivex.ObservableSource;
import io.reactivex.functions.Consumer;
import io.reactivex.functions.Function;
import io.reactivex.functions.Predicate;

import java.util.concurrent.TimeUnit;

public class UsuarioServiceImpl extends UsuariosGrpc.UsuariosImplBase{

    @Override
    public void listarUsuarios(Usuario request, StreamObserver<Usuario> responseObserver) {

//        Observable.fromIterable(usuariosAutenticados)
//                .filter(new Predicate<String>() {
//                    @Override
//                    public boolean test(String s) throws Exception {
//                        return !s.equals(request.getNome());
//                    }
//                }).concatMap(new Function<String, ObservableSource<String>>() {
//            @Override
//            public ObservableSource<String> apply(String s) throws Exception {
//                return Observable.just(s).delay(1, TimeUnit.SECONDS);
//            }
//        }).subscribe(new Consumer<String>() {
//            @Override
//            public void accept(String s) throws Exception {
//                responseObserver.onNext(Usuario.newBuilder().setNome(s).build());
//            }
//        });
//
//        usuariosAutenticadosPublisher.subscribe(new Consumer<Usuario>() {
//            @Override
//            public void accept(Usuario usuario) throws Exception {
//                responseObserver.onNext(usuario);
//            }
//        });
    }

    @Override
    public void sair(SairRequest request, StreamObserver<SairResponse> responseObserver) {
//        usuariosAutenticados.remove(request.getNome());
//        usuariosAutenticadosPublisher.onNext(Usuario.newBuilder().setOp(GameServer.OperacoesUsuario.EXCLUCAO)
//                .setNome(request.getNome()).build());
    }
}