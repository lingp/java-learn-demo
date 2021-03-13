package com.lin.framework.core;

/**
 * 单例工厂的顶层设计
 */
public interface BeanFactory {
    Object getBean(String beanName) throws Exception;

    public Object getBean(Class<?> beanClass) throws Exception;
}
