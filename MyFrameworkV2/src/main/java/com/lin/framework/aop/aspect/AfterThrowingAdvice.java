package com.lin.framework.aop.aspect;

import com.lin.framework.aop.intercept.Advice;
import com.lin.framework.aop.intercept.MethodInterceptor;
import com.lin.framework.aop.intercept.MethodInvocation;

import java.lang.reflect.Method;

public class AfterThrowingAdvice extends AbstractAspectJAdvice implements Advice, MethodInterceptor {
    public AfterThrowingAdvice(Method aspectMethod, Object aspectTarget) {
        super(aspectMethod, aspectTarget);
    }

    @Override
    public Object invoke(MethodInvocation methodInvocation) throws Throwable {
        return null;
    }
}
