package org.msgpack.rpc;

import java.io.IOException;
import java.net.UnknownHostException;
import java.util.concurrent.ExecutionException;

public interface RpcController {

    public Connection getConnection() throws UnknownHostException;
    
    public static interface Connection {
        public Response call(Request request) throws IOException, InterruptedException, ExecutionException;

        public void send(Request request, Callback callback) throws IOException;
    }
    
    public static interface Callback {
        public void apply(Response response);
    }
}
