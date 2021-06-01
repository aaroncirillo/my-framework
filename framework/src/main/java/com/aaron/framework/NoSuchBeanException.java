package com.aaron.framework;

public class NoSuchBeanException extends Exception {
    public NoSuchBeanException(String msg) {
        super(msg);
    }

    public NoSuchBeanException(String msg, Throwable throwable) {
        super(msg, throwable);
    }
}
