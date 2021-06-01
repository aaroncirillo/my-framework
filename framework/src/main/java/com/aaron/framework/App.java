package com.aaron.framework;

import javassist.util.proxy.MethodHandler;
import javassist.util.proxy.ProxyFactory;
import javassist.util.proxy.ProxyObject;
import org.reflections.Reflections;
import org.reflections.scanners.FieldAnnotationsScanner;
import org.reflections.scanners.MethodAnnotationsScanner;
import org.reflections.scanners.SubTypesScanner;
import org.reflections.scanners.TypeAnnotationsScanner;
import org.reflections.util.ClasspathHelper;
import org.reflections.util.ConfigurationBuilder;
import org.reflections.util.FilterBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class App {
    private static Logger log = LoggerFactory.getLogger(App.class);
    private final Map<String, Reflections> reflectionsByPackage = new ConcurrentHashMap<>();
    private final Set<Class<?>> frameworkAppAnnotatedClasses = new HashSet<>();
    private final Set<Method> frameworkBeanAnnotatedFields = new HashSet<>();
    private final Set<Method> frameworkInjectAnnotatedFields = new HashSet<>();
    private final Map<String, Object> beans = new ConcurrentHashMap<>();
    private final Map<String, Object> proxies = new ConcurrentHashMap<>();
    App() {
    };

    public <T> T getMain(Class<T> clazz) {
        Class main = (Class) frameworkAppAnnotatedClasses.toArray()[0];
        return clazz.cast(beans.get(main.getName()));
    }

    public <T> T getBean(Class<T> clazz) throws NoSuchBeanException {
        if(!proxies.containsKey(clazz.getName()))
            throw new NoSuchBeanException("Error: requested bean not found " + clazz.getName());
        return clazz.cast(proxies.get(clazz.getName()));
    }

    App scanPackages(String... packages) throws NoSuchMethodException, InvocationTargetException, InstantiationException, IllegalAccessException {
        for(String pkg : packages) {
            if(reflectionsByPackage.containsKey(pkg))
                continue;
            else {
                Reflections reflections = new Reflections(new ConfigurationBuilder()
                        .setUrls(ClasspathHelper.forPackage(pkg))
                        .setScanners(new SubTypesScanner(), new TypeAnnotationsScanner(),
                                new FieldAnnotationsScanner(), new MethodAnnotationsScanner())
                        .filterInputsBy(new FilterBuilder().includePackage(pkg)));
                frameworkAppAnnotatedClasses.addAll(reflections.getTypesAnnotatedWith(FrameworkApp.class));
                for(Class clazz : frameworkAppAnnotatedClasses) {
                    log.info("got FrameworkApp annotated class: " + clazz.getName());
                }
                frameworkBeanAnnotatedFields.addAll(reflections.getMethodsAnnotatedWith(FrameworkBean.class));
                for(Method method : frameworkBeanAnnotatedFields) {
                    log.info("got FrameworkBean annotated method: " + method.getName());
                }
                frameworkInjectAnnotatedFields.addAll(reflections.getMethodsAnnotatedWith(FrameworkInject.class));
                for(Method method : frameworkInjectAnnotatedFields) {
                    log.info("got FrameworkInject annotated field: " + method.getName());
                }
            }
        }
        if(frameworkAppAnnotatedClasses.size() != 1)
            throw new UnsupportedOperationException("Error: must annotate a single class with @FrameworkApp");
        Class clazz = (Class) frameworkAppAnnotatedClasses.toArray()[0];
        Constructor ctor = clazz.getConstructor();
        Object o = ctor.newInstance();
        beans.put(clazz.getName(), o);
        log.info("main class " + o.toString());
        return this;
    }

    boolean createBeans() throws NoSuchMethodException, InvocationTargetException, InstantiationException, IllegalAccessException {
        for(Method m : frameworkBeanAnnotatedFields) {
            Class clazz = m.getReturnType();
            Constructor ctor = clazz.getConstructor();
            Object o = ctor.newInstance();
            beans.put(clazz.getName(), o);
            log.info("created bean " + o.toString());
        }
        return true;
    }

    boolean injectProxies() throws InvocationTargetException, IllegalAccessException, InstantiationException, NoSuchMethodException {
        for(Method m : frameworkInjectAnnotatedFields) {
            if(m.getName().startsWith("set")) {
                if(m.getParameterCount() != 1)
                    throw new UnsupportedOperationException("Error: setter found with unexpected number of arguments, should be 1 " + m.getName());
                Class<?> clazz = m.getParameterTypes()[0];
                Object proxy = javassistProxy(clazz);
                for(String className : beans.keySet()) {
                    if(clazz.getName().equals(className)) {
                        Object targetBean = beans.get(m.getDeclaringClass().getName());
                        m.invoke(targetBean, proxy);
                        log.info("injected proxy " + proxy.getClass().getName() + " into bean " + targetBean.getClass().getName());
                        proxies.put(className, proxy);
                    }
                }
            }
        }
        return true;
    }

    boolean setProxyReferences() throws InvocationTargetException, IllegalAccessException {
        for(String className : proxies.keySet()) {
            Method[] methods = proxies.get(className).getClass().getMethods();
            for(Method method : methods) {
                if(!method.getName().startsWith("set"))
                    continue;
                Class<?> arg = method.getParameterTypes()[0];
                for(String s : proxies.keySet()) {
                    if(s.equals(arg.getName())) {
                        method.invoke(proxies.get(className), proxies.get(s));
                    }
                }
            }
        }
        return true;
    }

    private <T> T javassistProxy(Class<T> clazz) throws InstantiationException, IllegalAccessException, NoSuchMethodException, InvocationTargetException {
        ProxyFactory factory = new ProxyFactory();
        factory.setSuperclass(clazz);
        Class target = factory.createClass();
        MethodHandler handler = new MethodHandler() {

            @Override
            public Object invoke(Object self, Method overridden, Method forwarder,
                                 Object[] args) throws Throwable {
                //log.info("proxy running " + overridden.getName());
                return forwarder.invoke(self, args);
            }
        };
        Constructor ctor = target.getConstructor();
        Object instance = ctor.newInstance();
        ((ProxyObject) instance).setHandler(handler);
        return (T) instance;
    }

}
