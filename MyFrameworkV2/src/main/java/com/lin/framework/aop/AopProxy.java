package com.lin.framework.aop;

public interface AopProxy {
    Object getProxy();
    Object getProxy(ClassLoader classLoader);
}
