package com.lin.framework.aop.aspect;

import java.lang.reflect.Method;

public class AbstractAspectJAdvice {
    private Method aspectMethod;
    private Object aspectTarget;

    public AbstractAspectJAdvice(Method aspectMethod, Object aspectTarget) {
        this.aspectMethod = aspectMethod;
        this.aspectTarget = aspectTarget;
    }

    protected Object invokeAdviceMethod(JoinPoint joinPoint, Object returnValue, Throwable ex) throws Throwable {
        Class<?>[] paramsTypes = this.aspectMethod.getParameterTypes();
        if (null == paramsTypes || paramsTypes.length == 0) {
            return this.aspectMethod.invoke(aspectTarget);
        } else {
            Object[] args = new Object[paramsTypes.length];
            for (int i = 0; i < paramsTypes.length; i++) {
                if (paramsTypes[i] == JoinPoint.class) {
                    args[i] = joinPoint;
                } else if (paramsTypes[i] == Throwable.class) {
                    args[i] = ex;
                } else if (paramsTypes[i] == Object.class) {
                    args[i] = returnValue;
                }
            }
            return this.aspectMethod.invoke(aspectTarget, args);
        }
    }

}
