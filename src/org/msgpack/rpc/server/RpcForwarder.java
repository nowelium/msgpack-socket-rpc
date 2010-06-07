package org.msgpack.rpc.server;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.msgpack.rpc.BlockingService;
import org.msgpack.rpc.Request;
import org.msgpack.rpc.Response;
import org.msgpack.rpc.ServiceException;
import org.msgpack.rpc.server.annotation.Service;

class RpcForwarder {
    
    protected final Map<String, BlockingService> blockingService = new HashMap<String, BlockingService>();
    
    public void registerReflectiveBlockingService(ReflectiveBlockingService service){
        Service target = service.getTargetClass().getAnnotation(Service.class);
        if(null == target){
            throw new IllegalArgumentException("Service annotation was nul;");
        }
        registerBlockingService(target.value().getName(), service);
    }
    
    public void registerBlockingService(BlockingService service){
        Service target = service.getClass().getAnnotation(Service.class);
        if(null == target){
            throw new IllegalArgumentException("Service annotation was null");
        }
        
        registerBlockingService(target.getClass().getName(), service);
    }
    
    protected void registerBlockingService(String serviceName, BlockingService service){
        blockingService.put(serviceName, service);
    }
    
    public Response doRPC(Request request) throws IOException {
        try {
            return callMethod(request);
        } catch(Throwable t){
            Response response = new Response();
            response.setId(request.getId());
            response.setErrorMessage(t.getMessage());
            return response;
        }
    }
    
    protected Response callMethod(Request request) throws ServiceException {
        if(!blockingService.containsKey(request.getServiceName())){
            throw new ServiceException("service not found:" + request.getServiceName());
        }
        
        BlockingService service = blockingService.get(request.getServiceName());
        return service.call(request);
    }

}
