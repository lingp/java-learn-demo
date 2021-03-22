package com.lin.framework.aop.aspect;

import com.lin.framework.aop.intercept.Advice;
import com.lin.framework.aop.intercept.MethodInterceptor;
import com.lin.framework.aop.intercept.MethodInvocation;

import java.lang.reflect.Method;

public class AfterThrowingAdvice extends AbstractAspectJAdvice implements Advice, MethodInterceptor {

    private String throwingName;
    private MethodInvocation methodInvocation;

    public AfterThrowingAdvice(Method aspectMethod, Object aspectTarget) {
        super(aspectMethod, aspectTarget);
    }

    public void setThrowingName(String aspectAfterThrowingName) {
        this.throwingName = aspectAfterThrowingName;
    }

    @Override
    public Object invoke(MethodInvocation methodInvocation) throws Throwable {
        try {
            return methodInvocation.proceed();
        } catch (Throwable e) {
            invokeAdviceMethod(methodInvocation, null, e.getCause());
            throw e;
        }
    }
}
