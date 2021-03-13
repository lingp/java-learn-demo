package com.lin.framework.servlet;

import com.lin.framework.annotation.*;
import com.sun.javafx.tools.packager.Param;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URL;
import java.util.*;

public class DisplatcherServlet extends HttpServlet {

    // 保存application.properties配置的内容
    Properties properties = new Properties();

    // 保存扫描的所有的类名
    private List<String> classList = new ArrayList<>();

    // 保存所有扫描到的对象, ioc容器
    private Map<String, Object> ioc = new HashMap<>();

    // 获取 url 和 控制器+method的对应关系
    private Map<String, Handler> handlerMapping = new HashMap<>();

    @Override
    public void init() throws ServletException {
        // 1.加载配置
        loadConfig();

        // 2.扫描相关的类
        // 获取需要扫描的包名
        String scanPackage = properties.getProperty("scanPackage");
        doScanner(scanPackage);

        // 3.为所有 扫描到的类 创建对象，并放置到IOC容器中
        doInstance();

        // 4.完成依赖注入
        doAutowired();

        // 5.初始化HandlerMapping
        doHandlerMapping();
    }


    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
//        super.doGet(req, resp);
        this.doPost(req, resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        // 获取url TODO
        String url = req.getRequestURI();
        // 判断url是否已经配置了
        if (handlerMapping.containsKey(url)) {
            Handler handler = handlerMapping.get(url);

            // 参数处理 TODO
            // 所有的形参的类型
            Class<?>[] paramsType = handler.getParamTypes();
            Object[] paramsValues = new Object[paramsType.length];

            Map<String,String[]> params = req.getParameterMap();

            for (Map.Entry<String, String[]> param: params.entrySet()) {
                String value = Arrays.toString(param.getValue()).replaceAll("\\[|\\]","")
                        .replaceAll("\\s",",");
                if(!handler.paramIndexMapping.containsKey(param.getKey())){
                    continue;
                }

                int index = handler.paramIndexMapping.get(param.getKey());
                paramsValues[index] = value; // 这里应该根据类型去做转化 paramTypes[index] TODO, http过来都是字符串
            }

            try {
                Object rsData = handler.method.invoke(handler.controller, paramsValues);
                resp.getWriter().write(rsData.toString());
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            } catch (InvocationTargetException e) {
                e.printStackTrace();
            }



        } else {
            resp.getWriter().write("url not found");
        }
    }

    /**
     * 加载配置
     */
    private void loadConfig() {
        InputStream inputStream = this.getClass().getClassLoader().getResourceAsStream("application.properties");
        try {
            properties.load(inputStream);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * 扫描所有的类
     */
    private void doScanner(String scanPackage) {
        // 转化为文件路径
        String filePath = scanPackage.replaceAll("\\.", "/");
        // 获取classPath
        URL url = this.getClass().getClassLoader().getResource("/" + filePath);
        File classPath = new File(url.getFile());
        // 遍历获取所有的类
        for (File file: classPath.listFiles()) {
            if (file.isDirectory()) {
                doScanner(scanPackage + "." + file.getName());
            } else {
                if (!file.getName().endsWith(".class")) {
                    continue;
                }
                String className = (scanPackage + "." + file.getName().replace(".class", ""));
                classList.add(className);
            }
        }

        System.out.println(classList.toString());
    }

    /**
     * 初始化IOC容器
     */
    private void doInstance() {
        if (classList.isEmpty()) {
            return;
        }
        try {
            // 初始化所有@Controller和@Service类，为后面依赖注入做准备
            for (String className: classList) {
                Class<?> clazz = Class.forName(className);

                if (clazz.isAnnotationPresent(Controller.class)) {
                    Object instance = clazz.newInstance();
                    String beanName = toLowerCaseFirstOne(clazz.getSimpleName());
                    ioc.put(beanName, instance);
                } else if (clazz.isAnnotationPresent(Service.class)) {
                    Service service = clazz.getAnnotation(Service.class);
                    // 判断是service注解有设置value
                    String beanName = service.value();
                    if ("".equals(beanName.trim())) {
                        beanName = toLowerCaseFirstOne(clazz.getSimpleName());
                    }
                    Object instance = clazz.newInstance();
                    ioc.put(beanName, instance);

                    // 根据接口赋值
                    for (Class<?> i : clazz.getInterfaces()) {
                        if (ioc.containsKey(i.getName())) {
                            throw new Exception("The “" + i.getName() + " 已存在!!");
                        }
                        ioc.put(toLowerCaseFirstOne(i.getName()), instance);
                    }
                }
            }
            System.out.println(ioc.toString());
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    /**
     * 依赖注入
     */
    private void doAutowired() {
        if (ioc.isEmpty()) {
            return;
        }
        for (Map.Entry<String, Object> entry: ioc.entrySet()) {
            // 所有的，特定的 字段，包括private/protected/default
            Field[] fields = entry.getValue().getClass().getDeclaredFields();
            for (Field field: fields) {
                if (!field.isAnnotationPresent(Autowired.class)) {
                    continue;
                }
                Autowired autowired = field.getAnnotation(Autowired.class);
                // 获取Autowired设置的value
                String beanName = autowired.value().trim();
                if ("".equals(beanName)) { // Autowired没有设置value，则读取属性的类型
                    beanName = field.getType().getName();
                }
                beanName = toLowerCaseFirstOne(beanName);

                //暴力访问，只要加了@Autowired注解，都要强制赋值
                field.setAccessible(true);
                try {
                    //用反射机制，动态给字段赋值
                    field.set(entry.getValue(),ioc.get(beanName));
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    /**
     * 初始化andlerMapping
     */
    private void doHandlerMapping() {
        if (ioc.isEmpty()) {
            return;
        }
        for (Map.Entry<String, Object> entry: ioc.entrySet()) {
            Class<?> clazz = entry.getValue().getClass();

            if (!clazz.isAnnotationPresent(Controller.class)) {
                continue;
            }

            String baseUrl = "";
            // 获取 类上面的@RequestMapping("/test")
            if (clazz.isAnnotationPresent(RequestMapping.class)) {
                RequestMapping requestMapping = clazz.getAnnotation(RequestMapping.class);
                baseUrl = requestMapping.value();

                // 获取 public 方法上的 @RequestMapping("/test")
                for(Method method: clazz.getMethods()) {
                    if (!method.isAnnotationPresent(RequestMapping.class)) {
                        continue;
                    }
                    RequestMapping methodRequestMapping = method.getAnnotation(RequestMapping.class);
                    String url = ("/" + baseUrl + "/" + methodRequestMapping.value()).replaceAll("/+", "/");
                    handlerMapping.put(url, new Handler(url, method, entry.getValue()));
                }
            }
        }

        System.out.println(handlerMapping.toString());
    }

    public class Handler {
        private String url;
        private Method method;
        private Object controller;
        // 所有的参数类型
        private Class<?> [] paramTypes;

        //形参列表 有 RequestMapping 注解的
        //参数的名字作为key,参数的顺序，位置作为值
        private Map<String,Integer> paramIndexMapping;

        public Handler(String url, Method method, Object controller) {
            this.url = url;
            this.method = method;
            this.controller = controller;

            paramTypes = method.getParameterTypes();

            paramIndexMapping = new HashMap<String, Integer>();
            putParamIndexMapping(method);
        }

        /**
         * 获取形参的位置
         * @param method
         */
        private void putParamIndexMapping(Method method) {
            //因为一个参数可以有多个注解，而一个方法又有多个参数
            Annotation[][] parameterAnnotations = method.getParameterAnnotations();
            for (int i = 0; i < parameterAnnotations.length; i++) {
                for (Annotation a: parameterAnnotations[i]) {
                    if (a instanceof Params) {
                        String paramaName = ((Params) a).value();
                        if (!"".equals(paramaName.trim())) {
                            paramIndexMapping.put(paramaName, i);
                        }
                    }
                }
            }
        }

        public Class<?>[] getParamTypes() {
            return paramTypes;
        }

        public void setParamTypes(Class<?>[] paramTypes) {
            this.paramTypes = paramTypes;
        }

        public Map<String, Integer> getParamIndexMapping() {
            return paramIndexMapping;
        }

        public void setParamIndexMapping(Map<String, Integer> paramIndexMapping) {
            this.paramIndexMapping = paramIndexMapping;
        }

        public String getUrl() {
            return url;
        }

        public void setUrl(String url) {
            this.url = url;
        }

        public Method getMethod() {
            return method;
        }

        public void setMethod(Method method) {
            this.method = method;
        }

        public Object getController() {
            return controller;
        }

        public void setController(Object controller) {
            this.controller = controller;
        }
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
            return (new StringBuilder()).append(Character.toLowerCase(s.charAt(0))).append(s.substring(1)).toString();
    }

}
