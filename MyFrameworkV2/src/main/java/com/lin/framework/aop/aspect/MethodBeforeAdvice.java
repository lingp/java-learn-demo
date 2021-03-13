package com.lin.framework.aop.aspect;

import com.lin.framework.aop.intercept.Advice;
import com.lin.framework.aop.intercept.MethodInterceptor;
import com.lin.framework.aop.intercept.MethodInvocation;

import java.lang.reflect.Method;

public class MethodBeforeAdvice extends AbstractAspectJAdvice implements Advice, MethodInterceptor {

    private JoinPoint joinPoint;

    public MethodBeforeAdvice(Method aspectMethod, Object aspectTarget) {
        super(aspectMethod, aspectTarget);
    }

    public void before(Method method, Object[] args, Object target) throws Throwable {
        invokeAdviceMethod(this.joinPoint, null, null);
    }

    @Override
    public Object invoke(MethodInvocation methodInvocation) throws Throwable {
        this.joinPoint = methodInvocation;
        this.before(methodInvocation.getMethod(), methodInvocation.getArguments(), methodInvocation.getThis());
        return methodInvocation.proceed();
    }
}
