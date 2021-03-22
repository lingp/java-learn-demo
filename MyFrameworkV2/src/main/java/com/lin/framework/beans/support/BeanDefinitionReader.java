package com.lin.framework.beans.support;

import com.lin.framework.beans.BeanDefinition;
import lombok.Data;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

/**
 * 对配置文件进行查找，读取，解析
 */
@Data
public class BeanDefinitionReader {
    private List<String> registyBeanClasses = new ArrayList<>();
    private Properties config = new Properties();
    // 固定key，指定遍历的目录
    private final String SCAN_PACKAGE = "scanPackage";

    public BeanDefinitionReader(String... locations) {
        // 根据配置获取路径的文件流
        InputStream inputStream = this.getClass()
                .getClassLoader()
                .getResourceAsStream(locations[0].replace("classpath:", ""));
        try {
            // 加载配置
            config.load(inputStream);
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (null != inputStream) {
                try {
                    inputStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        doScanner(config.getProperty(SCAN_PACKAGE));
    }

    /**
     * 遍历文件夹，获取java类
     * @param scanPackage
     */
    private void doScanner(String scanPackage) {
        URL url = this.getClass().getClassLoader().getResource(
                "/" + scanPackage.replaceAll("\\.", "/"));
        File classPath = new File(url.getFile());
        for (File file: classPath.listFiles()) {
            if (file.isDirectory()) {
                doScanner(scanPackage + "." + file.getName());
            } else {
                if (!file.getName().endsWith(".class")) {
                    continue;
                }
                String className = (scanPackage + "." + file.getName().replace(".class", ""));
                registyBeanClasses.add(className);
            }
        }
    }

    /**
     * 将配置文件中扫描到的所有的配置信息转换为BeanDefinition对象，用于之后IOC操作方便
     * @return
     */
    public List<BeanDefinition> loadBeanDefinitions() {
        List<BeanDefinition> result = new ArrayList<BeanDefinition>();

        try {
            for (String className: registyBeanClasses) {
                Class<?> beanClass = Class.forName(className);
                if (beanClass.isInterface()) {
                    continue;
                }
                // 把配置信息解析成一个 BeanDefinition
                // 加入
                result.add(doCreateBeanDefinition(toLowerCaseFirstOne(beanClass.getSimpleName()), beanClass.getName()));

                Class<?>[] interfaces = beanClass.getInterfaces();

                for (Class<?> i: interfaces) {
                    result.add(doCreateBeanDefinition(i.getName(), beanClass.getName()));
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }


        return result;
    }

    /**
     * 把配置信息解析成一个 BeanDefinition
     * @param factoryBeanName
     * @param beanClassName
     * @return
     */
    private BeanDefinition doCreateBeanDefinition(String factoryBeanName, String beanClassName) {
        BeanDefinition beanDefinition = new BeanDefinition();
        beanDefinition.setFactoryBeanName(factoryBeanName);
        beanDefinition.setBeanClassName(beanClassName);
        return beanDefinition;
    }


    /**
     * 首字母转小写
     * @param s
     * @return
     */
    private String toLowerCaseFirstOne(String s){
        if(Character.isLowerCase(s.charAt(0)))
            return s;
        else
            return (new StringBuilder())
                    .append(Character.toLowerCase(s.charAt(0)))
                    .append(s.substring(1))
                    .toString();
    }

}
