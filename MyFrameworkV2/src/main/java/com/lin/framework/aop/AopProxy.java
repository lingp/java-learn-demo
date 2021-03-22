package com.lin.framework.aop;

/**
 * 默认用JDK动态代理
 */
public interface AopProxy {
    Object getProxy();
    Object getProxy(ClassLoader classLoader);
}
