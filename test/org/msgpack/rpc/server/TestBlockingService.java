package org.msgpack.rpc.server;

import org.msgpack.rpc.server.annotation.Service;

@Service(TestService.class)
public class TestBlockingService implements TestService {
    
    public String test1(){
        System.out.println("!test1");
        return "test1";
    }
    
    public void test2(){
        System.out.println("!test2");
    }
    
    public void test3(int value){
        System.out.println("!test3");
        System.out.println("!test3 value:" + value);
    }
    
    public int test4(int value){
        System.out.println("!test4");
        System.out.println("!test4 value:" + value);
        return value;
    }

}
