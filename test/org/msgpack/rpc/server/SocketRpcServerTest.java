package org.msgpack.rpc.server;

import java.util.concurrent.Executors;

import org.junit.Assert;
import org.junit.Test;
import org.msgpack.rpc.RpcController;
import org.msgpack.rpc.client.RpcControllerFactory;
import org.msgpack.rpc.client.ServiceProxy;

public class SocketRpcServerTest {
    
    @Test
    public void testTcp() throws Exception {
        SocketRpcServer server = new SocketRpcServer(19850, Executors.newSingleThreadExecutor());
        server.registerReflectiveBlockingService(new ReflectiveBlockingService(new TestBlockingService()));
        server.start();
        
        RpcController controller = new RpcControllerFactory().createTCP("localhost", 19850);
        synchronized(this){
            testRPC(controller);
        }
        server.stop();
    }
    
    protected void testRPC(RpcController controller){
        testBloclingServiceProxy(controller);
    }
    
    protected void testBloclingServiceProxy(RpcController contoller){
        ServiceProxy proxy = new ServiceProxy(contoller);
        TestService service = proxy.createBlocingService(TestService.class);
        Assert.assertEquals(service.test1(), "test1");
        service.test2();
        service.test3(123);
        service.test4(456);
        Assert.assertEquals(service.test4(456), 456);
    }

}
