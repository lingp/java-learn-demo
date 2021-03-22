package com.lin.framework.aop;

import com.lin.framework.aop.intercept.MethodInvocation;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.List;

public class JdkDynamicAopProxy implements AopProxy, InvocationHandler {
    private AdviseSupport config;

    public JdkDynamicAopProxy(AdviseSupport config) {
        this.config = config;
    }

    @Override
    public Object getProxy() {
        return getProxy(this.config.getTargetClass().getClassLoader());
    }

    @Override
    public Object getProxy(ClassLoader classLoader) {
        return Proxy.newProxyInstance(classLoader, this.config.getTargetClass().getInterfaces(), this);
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        List<Object> interceptorsAndDynamicMethodMatchers = config.getInterceptorsAndDynamicInterceptionAdvice(method,
                this.config.getTargetClass());
        MethodInvocation invocation = new MethodInvocation(proxy,
                this.config.getTarget(),
                method,
                args,
                this.config.getTargetClass(),
                interceptorsAndDynamicMethodMatchers);

        return invocation.proceed();
    }
}
