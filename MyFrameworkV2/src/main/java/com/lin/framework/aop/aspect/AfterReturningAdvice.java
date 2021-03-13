package com.lin.framework.aop.aspect;

import com.lin.framework.aop.intercept.Advice;
import com.lin.framework.aop.intercept.MethodInterceptor;
import com.lin.framework.aop.intercept.MethodInvocation;

import java.lang.reflect.Method;

public class AfterReturningAdvice extends AbstractAspectJAdvice implements Advice, MethodInterceptor {

    private JoinPoint joinPoint;

    public AfterReturningAdvice(Method aspectMethod, Object aspectTarget) {
        super(aspectMethod, aspectTarget);
    }

    @Override
    public Object invoke(MethodInvocation methodInvocation) throws Throwable {
        Object retVal = methodInvocation.proceed();
        this.joinPoint = methodInvocation;
        this.afterReturning(retVal, methodInvocation.getMethod(), methodInvocation.getArguments(), methodInvocation.getThis());
        return retVal;
    }

    private void afterReturning(Object returnValue, Method method, Object[] arguments, Object target) throws Throwable {
        invokeAdviceMethod(joinPoint, returnValue, null);
    }


}
