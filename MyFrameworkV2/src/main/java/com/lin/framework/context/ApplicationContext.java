package com.lin.framework.context;

import com.lin.framework.annotation.Autowired;
import com.lin.framework.annotation.Controller;
import com.lin.framework.annotation.Service;
import com.lin.framework.aop.*;
import com.lin.framework.beans.BeanDefinition;
import com.lin.framework.beans.BeanWrapper;
import com.lin.framework.beans.config.BeanPostProcessor;
import com.lin.framework.beans.support.BeanDefinitionReader;
import com.lin.framework.context.support.DefaultListableBeanFactory;
import com.lin.framework.core.BeanFactory;

import java.lang.reflect.Field;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;

public class ApplicationContext extends DefaultListableBeanFactory implements BeanFactory {

    private String[] configLocations;
    private BeanDefinitionReader reader;

    // 单例的IOC容器的缓存
    private Map<String, Object> singletonBeanCacheMap = new ConcurrentHashMap<>();
    // 通用的IOC容器
    private Map<String, BeanWrapper> beanWrapperMap = new ConcurrentHashMap<>();

    public ApplicationContext(String... configLocations) {
        this.configLocations = configLocations;
        try {
            refresh();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void refresh() throws Exception {
        // 1.定位配置文件
        reader = new BeanDefinitionReader(this.configLocations);

        // 2.加载配置文件，扫描相关的类，封装成BeanDefinition
        List<BeanDefinition> beanDefinitions = reader.loadBeanDefinitions();

        // 3.注册，把配置信息放到容器里面
        doRegisterBeanDefinition(beanDefinitions);

        // 4.把非延时加载的类，提前初始化
        doAutowrited();
    }

    private void doRegisterBeanDefinition(List<BeanDefinition> beanDefinitions) throws Exception {
        for (BeanDefinition beanDefinition : beanDefinitions) {
            if (super.beanDefinitionMap.containsKey(beanDefinition.getFactoryBeanName())) {
                throw new Exception("The " + beanDefinition.getFactoryBeanName() + " is exist!");
            }
            super.beanDefinitionMap.put(beanDefinition.getFactoryBeanName(), beanDefinition);
        }
    }

    private void doAutowrited() {
        for (Map.Entry<String, BeanDefinition> beanDefinitionEntry :
                super.beanDefinitionMap.entrySet()) {
            String beanName = beanDefinitionEntry.getKey();
            if (!beanDefinitionEntry.getValue().isLazyInit()) {
                try {
                    getBean(beanName);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }

    /**
     * 依赖注入，从这里开始，通过读取BeanDefinition中的信息
     * 然后，通过反射机制创建一个实例并返回
     * 用一个BeanWrapper来进行一次包装
     * 装饰器模式：
     * 1.保留原来的OOP关系
     * 2.可以对其进行扩展，增强功能
     *
     * @param beanName
     * @return
     * @throws Exception
     */
    @Override
    public Object getBean(String beanName) throws Exception {
        BeanDefinition beanDefinition = this.beanDefinitionMap.get(beanName);

        try {
            // 生成通知事件
            BeanPostProcessor beanPostProcessor = new BeanPostProcessor();

            Object instance = instantiateBean(beanDefinition);
            if (null == instance) {
                return null;
            }

            //在实例初始化以前调用一次
            beanPostProcessor.postProcessBeforeInitialization(instance, beanName);
            BeanWrapper beanWrapper = new BeanWrapper(instance);
            this.beanWrapperMap.put(beanName, beanWrapper);
            //在实例初始化后调用一次
            beanPostProcessor.postProcessAfterInitialization(instance, beanName);
            poulateBean(beanName, instance);

            return this.beanWrapperMap.get(beanName).getWrappedInstance();

        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    private void poulateBean(String beanName, Object instance) {
        Class clazz = instance.getClass();
        if (!(clazz.isAnnotationPresent(Controller.class) || clazz.isAnnotationPresent(Service.class))) {
            return;
        }
        Field[] fields = clazz.getDeclaredFields();
        for (Field field: fields) {
            if (!field.isAnnotationPresent(Autowired.class)) {
                continue;
            }
            Autowired autowired = field.getAnnotation(Autowired.class);
            String autowiredBeanName = autowired.value().trim();
            if ("".equals(autowiredBeanName)) {
                autowiredBeanName = field.getType().getName();
            }
            field.setAccessible(true);
            try {
                field.set(instance, this.beanWrapperMap.get(autowiredBeanName).getWrappedInstance());
            } catch (IllegalAccessException e ) {
                e.printStackTrace();
            }
        }
    }

    /**
     * 根据BeanDefinition 返回一个实例
     *
     * @param beanDefinition
     * @return
     */
    private Object instantiateBean(BeanDefinition beanDefinition) {
        Object instance = null;
        String className = beanDefinition.getBeanClassName();

        try {
            if (this.singletonBeanCacheMap.containsKey(className)) {
                instance = this.singletonBeanCacheMap.get(className);
            } else {
                Class<?> clazz = Class.forName(className);
                instance = clazz.newInstance();

                AdviseSupport config = instantionAopConfig(beanDefinition);
                config.setTargetClass(clazz);
                config.setTarget(instance);

//                if (config.pointCutMatch()) { // TODO
//                    instance = createProxy(config).getProxy();
//                }

                this.singletonBeanCacheMap.put(beanDefinition.getFactoryBeanName(), instance);
            }
            return instance;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public Object getBean(Class<?> beanClass) throws Exception {
        return getBean(beanClass.getName());
    }

    private AdviseSupport instantionAopConfig(BeanDefinition beanDefinition) {
        AopConfig config = new AopConfig();
        config.setPointCut(reader.getConfig().getProperty("pointCut"));
        config.setAspectClass(reader.getConfig().getProperty("aspectClass"));
        config.setAspectBefore(reader.getConfig().getProperty("aspectBefore"));
        config.setAspectAfter(reader.getConfig().getProperty("aspectAfter"));
        config.setAspectAfterThrow(reader.getConfig().getProperty("aspectAfterThrow"));
        config.setAspectAfterThrowingName(reader.getConfig().getProperty("aspectAfterThrowingName"));

        return new AdviseSupport(config);
    }

    private AopProxy createProxy(AdviseSupport config) {
        Class targetClass = config.getTargetClass();
        if (targetClass.getInterfaces().length > 0) {
            return new JdkDynamicAopProxy(config);
        }
        return new CglibAopProxy(config);
    }

    public String[] getBeanDefinitionNames() {
        return this.beanDefinitionMap.keySet().toArray(new  String[this.beanDefinitionMap.size()]);
    }

    public Properties getConfig(){
        return this.reader.getConfig();
    }
}
