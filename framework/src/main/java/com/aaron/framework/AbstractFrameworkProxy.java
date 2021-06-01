package com.aaron.framework;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;

public class AbstractFrameworkProxy implements InvocationHandler {

    private static Logger log = LoggerFactory.getLogger(AbstractFrameworkProxy.class);

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        log.info("Proxy is executing method: " + method.getName());
        return null;
    }
}
