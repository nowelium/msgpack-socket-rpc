package org.msgpack.rpc.client;

import java.net.UnknownHostException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.msgpack.rpc.RpcController;

public class RpcControllerFactory {
    
    protected final ExecutorService executor;
    
    public RpcControllerFactory(){
        this.executor = Executors.newCachedThreadPool();
    }

    public RpcController createTCP(final String host, final int port){
        return new RpcController(){
            public Connection getConnection() throws UnknownHostException {
                return new SocketRpcClient(host, port, executor);
            }
        };
    }
}
