package com.lin.framework.beans.config;

public class BeanPostProcessor {
    /**
     * 为在Bean的初始化前提供回调入口
     * @param bean
     * @param beanName
     * @return
     * @throws Exception
     */
    public Object postProcessBeforeInitialization(Object bean, String beanName) throws Exception {
        return bean;
    }

    /**
     * 为在 Bean 的初始化之后提供回调入口
     * @param bean
     * @param beanName
     * @return
     * @throws Exception
     */
    public Object postProcessAfterInitialization(Object bean, String beanName) throws Exception {
        return bean;
    }
}
