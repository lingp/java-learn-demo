package com.lin.framework.context;

/**
 *  通过解耦的方式获得IOC容器的顶层设计
 *  通过一个监听器扫描所有的类，只要实现该接口
 *  将自动调用setApplicationContext方法，从而将IOC容器注入到目标类
 */
public interface ApplicationContextAware {
    void setApplicationContext(ApplicationContext applicationContext);
}
