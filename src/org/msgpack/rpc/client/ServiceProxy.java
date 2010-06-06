package org.msgpack.rpc.client;

import java.io.IOException;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import org.msgpack.rpc.Request;
import org.msgpack.rpc.Response;
import org.msgpack.rpc.RpcController;
import org.msgpack.rpc.ServiceException;

public class ServiceProxy {
    
    protected final AtomicInteger id = new AtomicInteger(0);
    
    protected final RpcController controller;
    
    public ServiceProxy(RpcController controller){
        this.controller = controller;
    }
    
    // TODO:
    public <T> T createService(Class<T> interfaceClass, Responder responder){
        return createService(Thread.currentThread().getContextClassLoader(), interfaceClass, responder);
    }
    
    // TODO:
    public <T> T createService(ClassLoader classLoader, Class<T> interfaceClass, Responder responder){
        ServiceHandler handler = new ServiceHandler(interfaceClass, responder);
        return create(classLoader, interfaceClass, handler);
    }
    
    public <T> T createBlocingService(Class<T> interfaceClass){
        return createBlocingService(Thread.currentThread().getContextClassLoader(), interfaceClass);
    }
    
    public <T> T createBlocingService(ClassLoader classLoader, Class<T> interfaceClass){
        BlockingServiceHandler handler = new BlockingServiceHandler(interfaceClass);
        return create(classLoader, interfaceClass, handler);
    }
    
    protected <T> T create(ClassLoader classLoader, Class<T> interfaceClass, InvocationHandler handler){
        return interfaceClass.cast(Proxy.newProxyInstance(classLoader, new Class[]{interfaceClass}, handler));
    }
    
    public Object call(String serviceName, String methodName, Object...args) throws IOException, ServiceException, InterruptedException, ExecutionException {
        Request request = new Request();
        request.setId(id.incrementAndGet());
        request.setServiceName(serviceName);
        request.setMethod(methodName);
        request.setParams(args);
        
        RpcController.Connection connection = controller.getConnection();
        Response response = connection.call(request);
        if(null != response.getErrorMessage()){
            throw new ServiceException(response.getErrorMessage());
        }
        return response;
    }
    
    public Future<Object> asyncCall(String serviceName, String methodName, Object...args) throws IOException {
        final FutureResponder responder = new FutureResponder();
        asyncCall(responder, serviceName, methodName, args);
        return responder;
    }
    
    public void asyncCall(Responder responder, String serviceName, String methodName, Object...args) throws IOException {
        Request request = new Request();
        request.setId(id.incrementAndGet());
        request.setServiceName(serviceName);
        request.setMethod(methodName);
        request.setParams(args);
        
        RpcController.Connection connection = controller.getConnection();
        connection.send(request, new CallbackImpl(responder));
    }
    
    public static interface Responder {
        public void handleException(int id, ServiceException e);
        public void handleResponse(int id, Object object);
    }
    
    protected static class FutureResponder implements Responder, Future<Object> {
        
        protected final AtomicBoolean cancel = new AtomicBoolean(false);
        
        protected final AtomicBoolean done = new AtomicBoolean(false);
        
        protected final SynchronousQueue<Object> sync = new SynchronousQueue<Object>();

        public void handleException(int id, ServiceException exception) {
            sync.offer(exception);
        }

        public void handleResponse(int id, Object object) {
            sync.offer(object);
        }

        public boolean cancel(boolean mayInterruptIfRunning) {
            sync.clear();
            cancel.set(true);
            // TODO:
            return false;
        }

        public Object get() throws InterruptedException, ExecutionException {
            try {
                Object response = sync.poll();
                if(response instanceof ServiceException){
                    throw new ExecutionException((ServiceException) response);
                }
                return response;
            } finally {
                done.set(true);
            }
        }

        public Object get(long timeout, TimeUnit unit) throws InterruptedException, ExecutionException, TimeoutException {
            try {
                Object response = sync.poll(timeout, unit);
                if(response instanceof ServiceException){
                    throw new ExecutionException((ServiceException) response);
                }
                return response;
            } finally {
                done.set(true);
            }
        }

        public boolean isCancelled() {
            return cancel.get();
        }

        public boolean isDone() {
            return done.get();
        }
    }
    
    protected static class CallbackImpl implements RpcController.Callback {
        protected final Responder responder;
        protected CallbackImpl(Responder responder){
            this.responder = responder;
        }
        public void apply(Response response) {
            if(null != response.getErrorMessage()){
                responder.handleException(response.getId(), new ServiceException(response.getErrorMessage()));
                return ;
            }
            responder.handleResponse(response.getId(), response.getResult());
            return ;
        }
    }
    
    protected class ServiceHandler implements InvocationHandler {
        
        protected final Class<?> service;
        
        protected final Responder responder;
        
        protected ServiceHandler(Class<?> service, Responder responder){
            this.service = service;
            this.responder = responder;
        }
        
        public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
            asyncCall(responder, service.getName(), method.getName(), args);
            return null;
        }
    }
    
    protected class BlockingServiceHandler implements InvocationHandler {
        
        protected final Class<?> service;
        
        protected BlockingServiceHandler(Class<?> service){
            this.service = service;
        }
        
        public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
            return call(service.getName(), method.getName(), args);
        }
    }
}
