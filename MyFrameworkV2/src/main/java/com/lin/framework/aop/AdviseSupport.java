package com.lin.framework.aop;


import com.lin.framework.aop.aspect.AfterReturningAdvice;
import com.lin.framework.aop.aspect.AfterThrowingAdvice;
import com.lin.framework.aop.aspect.MethodBeforeAdvice;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class AdviseSupport {
    private Class targetClass;
    private Object target;
    private Pattern pointCutClassPattern;

    private transient Map<Method, List<Object>> methodCache;

    private AopConfig config;

    public AdviseSupport(AopConfig config) {
        this.config = config;
    }

    public Class getTargetClass() {
        return targetClass;
    }

    public void setTargetClass(Class targetClass) {
        this.targetClass = targetClass;
        parse();
    }

    public Object getTarget() {
        return target;
    }

    public void setTarget(Object target) {
        this.target = target;
    }

    public List<Object> getInterceptorsAndDynamicInterceptionAdvice(Method method, Class<?>
            targetClass) throws Exception {
        List<Object> cached = methodCache.get(method);

        if (cached == null) {
            Method m = targetClass.getMethod(method.getName(), method.getParameterTypes());
            cached = methodCache.get(m);

            this.methodCache.put(m, cached);
        }

        return cached;
    }

    public boolean pointCutMatch() {
        return pointCutClassPattern.matcher(this.targetClass.toString()).matches();
    }

    private void parse() {
        String pointCut = config.getPointCut()
                .replaceAll("\\.", "\\\\.")
                .replaceAll("\\\\.\\*", ".*")
                .replaceAll("\\(", "\\\\(")
                .replaceAll("\\)", "\\\\)");
        String pointCutForClass = pointCut.substring(0, pointCut.lastIndexOf("\\(") - 4);
        pointCutClassPattern = Pattern.compile("class " +
                pointCutForClass.substring(pointCutForClass.lastIndexOf(" ") + 1));

        methodCache = new HashMap<Method, List<Object>>();
        Pattern pattern = Pattern.compile(pointCut);

        try {
            Class aspectClass = Class.forName(config.getAspectClass());
            Map<String, Method> aspectMethods = new HashMap<String, Method>();
            for (Method m : aspectClass.getMethods()) {
                aspectMethods.put(m.getName(), m);
            }

            for (Method m : targetClass.getMethods()) {

                String methodString = m.toString();
                if (methodString.contains("throws")) {
                    methodString = methodString.substring(0, methodString.lastIndexOf("throws")).trim();
                }
                Matcher matcher = pattern.matcher(methodString);
                if (matcher.matches()) {
                    // 满足切面规则的类，添加到AOP配置中
                    List<Object> advices = new LinkedList<Object>();
                    // 前置通知
                    if (!(null == config.getAspectBefore() || "".equals(config.getAspectBefore().trim()))) {
                        advices.add(new MethodBeforeAdvice(
                                aspectMethods.get(config.getAspectBefore()),
                                aspectClass.newInstance()
                        ));
                    }

                    // 后置通知
                    if (!(null == config.getAspectAfter() || "".equals(config.getAspectAfter().trim()))) {
                        advices.add(new AfterReturningAdvice(
                                aspectMethods.get(config.getAspectAfter()),
                                aspectClass.newInstance()
                        ));
                    }

                    // 异常通知
                    if (!(null == config.getAspectAfterThrow() || "".equals(config.getAspectAfterThrow().trim()))) {
                        AfterThrowingAdvice afterThrowingAdvice = new
                                AfterThrowingAdvice(aspectMethods.get(config.getAspectAfterThrow()),
                                aspectClass.newInstance());

                        afterThrowingAdvice.setThrowingName(config.getAspectAfterThrowingName());
                        advices.add(afterThrowingAdvice);
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }


    }


}
