package br.com.diegosilva.grpc.services;

import br.com.diegosilva.grpc.hello.SairRequest;
import br.com.diegosilva.grpc.hello.SairResponse;
import br.com.diegosilva.grpc.hello.Usuario;
import br.com.diegosilva.grpc.hello.UsuariosGrpc;
import io.grpc.stub.StreamObserver;

public class UsuarioServiceImpl extends UsuariosGrpc.UsuariosImplBase {

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

//        usuariosAutenticadosPublisher.subscribe(new Consumer<Usuario>() {
//            @Override
//            public void accept(Usuario usuario) throws Exception {
//                responseObserver.onNext(usuario);
//            }
//        });
    }

    @Override
    public void sair(SairRequest request, StreamObserver<SairResponse> responseObserver) {
        //usuariosAutenticados.remove(request.getNome());
        //        usuariosAutenticadosPublisher.onNext(Usuario.newBuilder().setOp(Main.OperacoesUsuario.EXCLUCAO)
        //                .setNome(request.getNome()).build());
    }
}
