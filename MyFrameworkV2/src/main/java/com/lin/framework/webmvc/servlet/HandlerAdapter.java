package com.lin.framework.webmvc.servlet;

import com.lin.framework.annotation.Params;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationTargetException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public class HandlerAdapter {
    public boolean supports(Object handler) {
        return (handler instanceof HandlerMapping);
    }

    public ModelAndView handle(HttpServletRequest req, HttpServletResponse resp, Object handler) throws InvocationTargetException, IllegalAccessException {
        HandlerMapping handlerMapping = (HandlerMapping) handler;

        // 形参列表
        Map<String, Integer> paramMapping = new HashMap<>();
        Annotation[][] pa = handlerMapping.getMethod().getParameterAnnotations();
        for(int i=0; i < pa.length; i++) {
            for (Annotation a: pa[i]) {
                if (a instanceof Params) {
                    String paramName = ((Params)a).value();
                    if (!"".equals(paramName.trim())) {
                        paramMapping.put(paramName, i);
                    }
                }
            }
        }

        Class<?>[] paramsTypes = handlerMapping.getMethod().getParameterTypes();
        for (int i = 0; i < paramsTypes.length; i++) {
            Class<?> type = paramsTypes[i];
            if (type == HttpServletRequest.class || type == HttpServletResponse.class) {
                paramMapping.put(type.getName(), i);
            }
        }


        // 请求参数
        Map<String, String[]> reqParameterMap = req.getParameterMap();

        // 实参列表
        Object[] paramValues = new Object[paramsTypes.length];
        for (Map.Entry<String, String[]> param: reqParameterMap.entrySet()) {
            String value = Arrays.toString(param.getValue()).replaceAll("\\[|\\]","").replaceAll("\\s","");

            if (!paramMapping.containsKey(param.getKey())) {
                continue;
            }

            int index = paramMapping.get(param.getKey());
            paramValues[index] = caseStringValue(value, paramsTypes[index]);
        }

        if (paramMapping.containsKey(HttpServletRequest.class.getName())) {
            int reqIndex = paramMapping.get(HttpServletRequest.class.getName());
            paramValues[reqIndex] = req;
        }

        if (paramMapping.containsKey(HttpServletResponse.class.getName())) {
            int respIndex = paramMapping.get(HttpServletResponse.class.getName());
            paramValues[respIndex] = resp;
        }

        Object result = handlerMapping.getMethod().invoke(handlerMapping.getController(), paramValues);

        if (result == null) {
            return null;
        }

        boolean isModelAndView = handlerMapping.getMethod().getReturnType() == ModelAndView.class;
        if (isModelAndView) {
            return (ModelAndView) result;
        } else {
            return null;
        }
    }

    private Object caseStringValue(String value, Class<?> clazz) {
        if(clazz == String.class){
            return value;
        }else if(clazz == Integer.class){
            return Integer.valueOf(value);
        }else if(clazz == int.class){
            return Integer.valueOf(value).intValue();
        }else {
            return null;
        }
    }


}
