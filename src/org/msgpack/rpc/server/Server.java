package org.msgpack.rpc.server;

import java.io.IOException;

public interface Server {

    public void start() throws IOException;
    
    public void stop();
    
    public boolean isRunning();
}
