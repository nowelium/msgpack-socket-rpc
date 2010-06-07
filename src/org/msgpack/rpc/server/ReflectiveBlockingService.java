package org.msgpack.rpc.server;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.msgpack.rpc.BlockingService;
import org.msgpack.rpc.Request;
import org.msgpack.rpc.Response;
import org.msgpack.rpc.ServiceException;

public class ReflectiveBlockingService implements BlockingService {
    
    private final Map<String, Method> methods = new HashMap<String, Method>();
    
    private final Object target;
    
    private final Class<?> targetClass;
    
    public ReflectiveBlockingService(Object target){
        this.target = target;
        this.targetClass = target.getClass();
    }
    
    protected Object getTarget(){
        return target;
    }
    
    protected Class<?> getTargetClass(){
        return targetClass;
    }
    
    private Method getsMethod(String methodName) throws ServiceException {
        if(methods.containsKey(methodName)){
            return methods.get(methodName);
        }
        for(Method method: targetClass.getDeclaredMethods()){
            // FIXME: duplicated methods
            String name = method.getName();
            if(name.equals(methodName)){
                methods.put(name, method);
                return method;
            }
        }
        throw new ServiceException("no such method:" + methodName);
    }
    
    public final Response call(Request request) throws ServiceException {
        Method method = getsMethod(request.getMethod());
        
        try {
            List<?> params = request.getParams();
            Object[] args = null;
            if(null != params){
                args = params.toArray();
            }
            
            Object handleResult = method.invoke(target, args);
            
            Response response = new Response();
            response.setId(request.getId());
            response.setResult(handleResult);
            return response;
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
            throw new ServiceException(e.getMessage());
        } catch (IllegalAccessException e) {
            e.printStackTrace();
            throw new ServiceException(e.getMessage());
        } catch (InvocationTargetException e) {
            e.printStackTrace();
            throw new ServiceException(e.getMessage());
        }
    }
}
