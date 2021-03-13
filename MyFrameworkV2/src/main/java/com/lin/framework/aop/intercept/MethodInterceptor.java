package com.lin.framework.aop.intercept;

public interface MethodInterceptor {
    Object invoke(MethodInvocation methodInvocation) throws Throwable;
}
