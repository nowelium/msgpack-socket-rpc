package org.msgpack.rpc.server;

import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

import org.msgpack.rpc.BlockingService;

public class SocketRpcServer implements Server {
    
    protected static final Logger logger = Logger.getLogger(SocketRpcServer.class.getName());
    
    protected final int port;
    
    protected final int backlog;
    
    protected final InetAddress bindAddr;
    
    protected final ExecutorService executor;
    
    protected final ServerThread serverThread = new ServerThread();
    
    protected final RpcForwarder forwarder = new RpcForwarder();
    
    public SocketRpcServer(int port, ExecutorService executorService){
        this(port, 0, null, executorService);
    }
    
    public SocketRpcServer(int port, int backlog, InetAddress bindAddr, ExecutorService executorService){
        this.port = port;
        this.backlog = backlog;
        this.bindAddr = bindAddr;
        this.executor = executorService;
        serverThread.setDaemon(true);
    }
    
    public void registerReflectiveBlockingService(ReflectiveBlockingService service){
        forwarder.registerReflectiveBlockingService(service);
    }
    
    public void registerBlockingService(BlockingService service){
        forwarder.registerBlockingService(service);
    }

    public void start() throws IOException {
        serverThread.start();
    }
    
    public void run() throws IOException {
        // disable threading
        serverThread.run();
    }

    public void stop() {
        serverThread.stopServer();
    }
    
    public boolean isRunning(){
        return serverThread.isRunning();
    }
    
    protected class ServerThread extends Thread {
        
        protected ServerSocket serverSocket;
        
        public void run(){
            try {
                startServer();
            } catch(IOException e){
                logger.warning("Error while running server" + e);
            }
        }
        
        protected void startServer() throws IOException {
            serverSocket = new ServerSocket(port, backlog, bindAddr);
            serverSocket.setReuseAddress(true);
            serverSocket.setPerformancePreferences(0, 1, 2);
            
            logger.info("Listening for requests on port: " + port);
            try {
                while(!serverSocket.isClosed()){
                    final Socket socket = serverSocket.accept();
                    socket.setReuseAddress(true);
                    socket.setTcpNoDelay(true);
                    socket.setKeepAlive(true);
                    socket.setPerformancePreferences(0, 1, 2);
                    executor.execute(new SocketHandler(socket, forwarder));
                }
            } finally {
                executor.shutdown();
                try {
                    if(!executor.awaitTermination(10, TimeUnit.SECONDS)){
                        executor.shutdownNow();
                    }
                } catch(InterruptedException e){
                    //
                }
                serverSocket.close();
            }
        }
        
        protected boolean isRunning(){
            if(null == serverSocket){
                return false;
            }
            return !serverSocket.isClosed();
        }
        
        protected void stopServer() {
            logger.info("Shutting down server");
            if(!executor.isShutdown()){
                executor.shutdownNow();
            }
            
            try {
                if(isRunning()){
                    serverSocket.close();
                }
            } catch(IOException e){
                logger.warning("Error while shutting down server:" + e);
            }
        }
    }

}
