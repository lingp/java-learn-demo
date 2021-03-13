package com.lin.framework.aop;

public class CglibAopProxy implements AopProxy {

    private AdviseSupport config;

    public CglibAopProxy(AdviseSupport config) {
        this.config = config;
    }


    @Override
    public Object getProxy() {
        return null;
    }

    @Override
    public Object getProxy(ClassLoader classLoader) {
        return null;
    }
}
