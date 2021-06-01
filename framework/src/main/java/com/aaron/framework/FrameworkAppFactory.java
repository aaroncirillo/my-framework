package com.aaron.framework;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class FrameworkAppFactory {

    private static Logger log = LoggerFactory.getLogger(FrameworkAppFactory.class);
    private static boolean initialized;

    public static Object initialize(String... packages) {
        if(initialized)
            throw new UnsupportedOperationException("Error: application has already been initialized");
        try {
            App app = new App();
            Object o = app.scanPackages(packages);
            app.createBeans();
            app.injectProxies();
            app.setProxyReferences();
            return o;
        } catch (Exception e) {
            log.error("", e);
        }
        return null;
    }

}
