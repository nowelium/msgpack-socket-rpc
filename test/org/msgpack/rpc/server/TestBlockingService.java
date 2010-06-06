package org.msgpack.rpc.server;

import org.msgpack.rpc.BlockingService;

public class TestBlockingService extends BlockingService implements TestService {
    
    public String test1(){
        System.out.println("test1");
        return "test1";
    }
    
    public void test2(){
        System.out.println("test2");
    }
    
    public void test3(int value){
        System.out.println("test3");
        System.out.println("test3 value:" + value);
    }
    
    public int test4(int value){
        System.out.println("test4");
        return value;
    }

}
