package org.msgpack.rpc.server;

import org.junit.Assert;
import org.junit.Test;
import org.msgpack.rpc.server.annotation.Service;

public class RpcForwarderTest {
    
    public static interface TestService {
    }
    
    public static class NoAnnotationBlockingService implements TestService {
    }
    
    @Service(TestService.class)
    public static class NoMethodBlockingService implements TestService {
    }

    @Test
    public void registerBlockingService_no_annos() {
        RpcForwarder forwarder = new RpcForwarder();
        try {
            forwarder.registerReflectiveBlockingService(new ReflectiveBlockingService(new NoAnnotationBlockingService()));
            Assert.fail();
        } catch(IllegalArgumentException e){
            e.printStackTrace();
        }
    }
    
    @Test
    public void registerBlockingService(){
        RpcForwarder forwarder = new RpcForwarder();
        try {
            forwarder.registerReflectiveBlockingService(new ReflectiveBlockingService(new NoMethodBlockingService()));
        } catch(IllegalArgumentException e){
            Assert.fail();
        }
    }
}
