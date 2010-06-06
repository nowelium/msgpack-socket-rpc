package org.msgpack.rpc.server;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.util.logging.Logger;

import org.msgpack.Packer;
import org.msgpack.UnpackException;
import org.msgpack.Unpacker;
import org.msgpack.rpc.Request;
import org.msgpack.rpc.Response;

class SocketHandler implements Runnable {
    
    protected static final Logger logger = Logger.getLogger(SocketHandler.class.getName());
    
    protected final Socket socket;
    
    protected final RpcForwarder forwarder;
    
    public SocketHandler(Socket socket, RpcForwarder forwarder){
        this.socket = socket;
        this.forwarder = forwarder;
    }
    
    public void run(){
        logger.info("new client connection [" + socket + "]");
        
        InputStream in = null;
        OutputStream out = null;
        try {
            in = socket.getInputStream();
            out = socket.getOutputStream();
            
            //while(socket.isConnected()){
            if(socket.isConnected()){
                // TODO: this should not be hardcoded to 1024 bytes
                final ByteBuffer tmp = ByteBuffer.allocateDirect(1024);
                // TODO: this should not be hardcoded to 4096 bytes
                final ByteBuffer buffer = ByteBuffer.allocateDirect(4096);
                final ReadableByteChannel channel = Channels.newChannel(in);
                
                while(0 < channel.read(tmp)){
                    tmp.flip();
                    buffer.put(tmp);
                    buffer.flip();
                    tmp.clear();
                }
                if(!socket.isInputShutdown()){
                    socket.shutdownInput();
                }
                
                //buffer.compact();
                byte[] data = new byte[buffer.remaining()];
                buffer.get(data);
                
                forward(data, out);
                socket.shutdownOutput();
            }
        } catch(IOException e){
            e.printStackTrace();
            logger.warning("Error while reading/writing:" + e);
        } finally {
            logger.info("connection closed.[" + socket + "]");

            try {
                if(null != in){
                    in.close();
                }
                
                if(null != out){
                    out.close();
                }

                socket.close();
            } catch(IOException e){
                logger.warning("Error while closing I/O:" + e);
            }
        }
    }
    
    protected void forward(final byte[] input, final OutputStream out) throws IOException {
        final Unpacker unpacker = new Unpacker();
        unpacker.wrap(input);
        if(!unpacker.execute()){
            // TODO
            return;
        }
        try {
            final Request request = new Request();
            request.messageUnpack(unpacker);
            
            Response response = forwarder.doRPC(request);
            
            Packer packer = new Packer(out);
            packer.pack(response);
            out.flush();
        } catch(UnpackException e){
            logger.warning("Error while unserialized data:" + e);
        }
    }
}
