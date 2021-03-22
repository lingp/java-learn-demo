package com.lin.framework.webmvc.servlet;

import com.lin.framework.annotation.Controller;
import com.lin.framework.annotation.RequestMapping;
import com.lin.framework.context.ApplicationContext;
import lombok.extern.slf4j.Slf4j;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


// Servlet 作为一个MVC的启动入口
@Slf4j
public class DispatcherServlet extends HttpServlet {

    private final String LOCATION = "contextConfigLocation";

    private List<HandlerMapping> handlerMappings = new ArrayList<HandlerMapping>();

    private Map<HandlerMapping, HandlerAdapter> handlerAdapters = new HashMap<HandlerMapping, HandlerAdapter>();

    private List<ViewResolver> viewResolvers = new ArrayList<ViewResolver>();

    private ApplicationContext context;

    @Override
    public void init(ServletConfig config) throws ServletException {
        // 初始化容器
        context = new ApplicationContext(config.getInitParameter(LOCATION));
        initStrategies(context);
    }

    // 初始化策略
    private void initStrategies(ApplicationContext context) {
        // 文件上传解析
        initMultipartResolver(context);

        // 本地化解析
        initLocaleResolver(context);

        // 主题解析
        initThemeResolver(context);

        // HandlerMapping 保存 Controller 中配置的 RequestMapping 和 Method 的一个对应关系 done
        // 通过 HandlerMapping， 将请求映射到处理器
        initHandlerMappings(context);

        // HandlerAdapters 用来动态匹配 Method 参数， 包括类转换， 动态赋值
        // 通过 HandlerAdapter 进行多类型的参数动态匹配
        initHandlerAdapters(context);

        // 如果执行过程中遇到异常， 将交给HandlerExceptionResolver 来解析
        initHandlerExceptionResolvers(context);

        // 直接解析请求到视图名
        initRequestToViewNameTranslator(context);

        //通过 ViewResolvers 实现动态模板的解析
        //通过 viewResolver 解析逻辑视图到具体视图实现 done
        initViewResolvers(context);

        //flash 映射管理器
        initFlashMapManager(context);
    }

    private void initFlashMapManager(ApplicationContext context) {
    }

    private void initViewResolvers(ApplicationContext context) {
        String templateRoot = context.getConfig().getProperty("templateRoot");
        String templateRootPath = this.getClass().getClassLoader().getResource(templateRoot).getFile();

        File templateRootDir = new File(templateRootPath);

        for (File template : templateRootDir.listFiles()) {
            this.viewResolvers.add(new ViewResolver(templateRoot));
        }

    }

    private void initRequestToViewNameTranslator(ApplicationContext context) {
    }

    private void initHandlerExceptionResolvers(ApplicationContext context) {
    }

    // HandlerAdapters 用来动态匹配 Method 参数， 包括类转换， 动态赋值
    // 通过 HandlerAdapter 进行多类型的参数动态匹配
    private void initHandlerAdapters(ApplicationContext context) {
        for (HandlerMapping handlerMapping: this.handlerMappings) {
            this.handlerAdapters.put(handlerMapping, new HandlerAdapter());
        }
    }

    // 对 Controller 中配置的 RequestMapping 和 Method 进行一一对应
    private void initHandlerMappings(ApplicationContext context) {
        // 获取容器中取到的所有controller
        String[] beanNames = context.getBeanDefinitionNames();

        try {
            for (String beanName: beanNames) {
                Object controller = context.getBean(beanName);
                Class<?> clazz = controller.getClass();

                if (!clazz.isAnnotationPresent(Controller.class)) {
                    continue;
                }

                String baseUrl = "";

                if (clazz.isAnnotationPresent(RequestMapping.class)) {
                    RequestMapping requestMapping = clazz.getAnnotation(RequestMapping.class);
                    baseUrl = requestMapping.value();
                }

                // 扫描所有的public方法
                Method[] methods = clazz.getMethods();
                for (Method method: methods) {
                    if (!method.isAnnotationPresent(RequestMapping.class)) {
                        continue;
                    }

                    RequestMapping requestMapping = method.getAnnotation(RequestMapping.class);
                    String regex = ("/" + baseUrl
                            + requestMapping.value()
                            .replaceAll("\\*", ".*"))
                            .replaceAll("/+", "/");
                    Pattern pattern = Pattern.compile(regex);
                    this.handlerMappings.add(new HandlerMapping(pattern, controller, method));
                    log.info("Mapping: " + regex + ", " + method);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    private void initThemeResolver(ApplicationContext context) {
    }

    private void initLocaleResolver(ApplicationContext context) {
    }

    private void initMultipartResolver(ApplicationContext context) {
    }


    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        this.doPost(req, resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        try {
            doDispatch(req, resp);
        }catch (Exception e){
            resp.getWriter().write("<font size='25' color='blue'>500 Exception</font><br/>Details:<br/>" + Arrays.toString(e.getStackTrace()).replaceAll("\\[|\\]","")
                    .replaceAll("\\s","\r\n") + "<font color='green'><i>Copyright@GupaoEDU</i></font>");
            e.printStackTrace();
        }
    }

    private void doDispatch(HttpServletRequest req, HttpServletResponse resp) throws InvocationTargetException, IllegalAccessException, IOException {
        HandlerMapping handler = getHandler(req);
        if (handler == null) {
            processDispatchResult(req, resp, new ModelAndView("404"));
            return;
        }

        HandlerAdapter handlerAdapter = getHandlerAdapter(handler);

        // 获取返回值
        ModelAndView modelAndView = handlerAdapter.handle(req, resp, handler);

        // 输出
        processDispatchResult(req, resp, modelAndView);
    }

    private void processDispatchResult(HttpServletRequest request,HttpServletResponse response,
                                       ModelAndView mv) throws IOException {

        if (null == mv) {
            return;
        }

        if (this.viewResolvers.isEmpty()) {
            return;
        }

        if (this.viewResolvers != null) {
            for (ViewResolver viewResolver: this.viewResolvers) {
                View view = viewResolver.resolveViewName(mv.getViewName(), null);
                if (view != null) {
                    view.render(mv.getModel(),request,response);
                    return;
                }
            }
        }
    }

    private HandlerAdapter getHandlerAdapter(HandlerMapping handler) {
        if (this.handlerAdapters.isEmpty()) {
            return null;
        }
        HandlerAdapter handlerAdapter = this.handlerAdapters.get(handler);
        if (handlerAdapter.supports(handler)) {
            return handlerAdapter;
        }
        return null;
    }

    private HandlerMapping getHandler(HttpServletRequest req) {
        if (this.handlerMappings.isEmpty()) {
            return null;
        }

        String url = req.getRequestURI();
        String contextPath = req.getContextPath();
        url = url.replace(contextPath, "").replaceAll("/+", "/");

        for (HandlerMapping handler: this.handlerMappings) {
            Matcher matcher = handler.getPattern().matcher(url);
            if (!matcher.matches()) {
                continue;
            }
            return handler;
        }

        return null;
    }
}
