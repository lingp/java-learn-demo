package com.lin.framework.aop;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;

public class JdkDynamicAopProxy implements AopProxy, InvocationHandler {
    private AdviseSupport adviseSupport;

    public JdkDynamicAopProxy(AdviseSupport adviseSupport) {
        this.adviseSupport = adviseSupport;
    }

    @Override
    public Object getProxy() {
        return null;
    }

    @Override
    public Object getProxy(ClassLoader classLoader) {
        return null;
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        return null;
    }
}
