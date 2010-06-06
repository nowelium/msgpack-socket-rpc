package org.msgpack.rpc;

public class ServiceException extends Exception {

    private static final long serialVersionUID = 1L;
    
    public ServiceException(String message){
        super(message);
    }
    
    public ServiceException(Throwable t){
        super(t);
    }

}
