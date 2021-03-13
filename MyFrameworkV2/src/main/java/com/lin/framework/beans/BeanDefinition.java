package com.lin.framework.beans;

import lombok.Data;

/**
 * 用来存储配置文件中的信息
 */
@Data
public class BeanDefinition {
    private String beanClassName;
    private boolean lazyInit = false;
    private String factoryBeanName;
}
