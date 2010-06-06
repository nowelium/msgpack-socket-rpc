package org.msgpack.rpc;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

public abstract class BlockingService implements Service {
    
    private final Map<String, Method> methods = new HashMap<String, Method>();
    
    private Method getsMethod(Class<?> clazz, String methodName) throws ServiceException {
        if(methods.containsKey(methodName)){
            return methods.get(methodName);
        }
        for(Method method: clazz.getDeclaredMethods()){
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
        Class<?> clazz = getClass().getDeclaringClass();
        Method method = getsMethod(clazz, request.getMethod());
        
        try {
            Object handleResult = method.invoke(this, request.getParams());
            
            Response response = new Response();
            response.setId(request.getId());
            response.setResult(handleResult);
            return response;
        } catch (IllegalArgumentException e) {
            throw new ServiceException(e.getMessage());
        } catch (IllegalAccessException e) {
            throw new ServiceException(e.getMessage());
        } catch (InvocationTargetException e) {
            throw new ServiceException(e.getMessage());
        }
    }
    
}
