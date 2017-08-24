
package br.com.diegosilva.grpc.hello;

import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.StatusRuntimeException;

import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;


public class AutenticacaoCliente {
    private static final Logger logger = Logger.getLogger(AutenticacaoCliente.class.getName());

    private final ManagedChannel channel;
    private final AutenticacaoGrpc.AutenticacaoBlockingStub blockingStub;

    public AutenticacaoCliente(String host, int port) {
        this(ManagedChannelBuilder.forAddress(host, port)
                .usePlaintext(true)
                .build());
    }

    AutenticacaoCliente(ManagedChannel channel) {
        this.channel = channel;
        blockingStub = AutenticacaoGrpc.newBlockingStub(channel);
    }

    public void shutdown() throws InterruptedException {
        channel.shutdown().awaitTermination(5, TimeUnit.SECONDS);
    }

    public void autenticar(String usuario) {
        logger.info("Vai tentar autenticar-se " + usuario + " ...");

        AutenticacaoRequest request = AutenticacaoRequest.newBuilder()
                .setUsuario(usuario).build();

        AutenticacaoResponse response;

        try {
            response = blockingStub.autenticar(request);

            if(response.getCodigo() < 0){
                logger.info("Erro de autenticacao: "+response.getMessage());
            }else{
                logger.info(response.getMessage());
            }

        } catch (StatusRuntimeException e) {
            logger.log(Level.WARNING, "Chamada RPC falhou: {0}", e.getStatus());
            return;
        }
    }

    public static void main(String[] args) throws Exception {
        AutenticacaoCliente client = new AutenticacaoCliente("localhost", 50051);
        try {
            String user = "world";
            if (args.length > 0) {
                user = args[0];
            }
            client.autenticar(user);
        } finally {
            client.shutdown();
        }
    }
}
