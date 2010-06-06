package org.msgpack.rpc;

public interface Service {
    public Response call(Request request) throws ServiceException;
}
