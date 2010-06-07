package org.msgpack.rpc;

public interface BlockingService extends Service {
    
    public Response call(Request request) throws ServiceException;
    
}
