package com.lin.framework.webmvc.servlet;

import lombok.Data;

import java.util.Map;

@Data
public class ModelAndView {

    private String viewName;
    private Map<String, ?> model;

    public ModelAndView(String viewName) {
        this(viewName, null);
    }

    public ModelAndView(String viewName, Map<String, ?> model) {
        this.viewName = viewName;
        this.model = model;
    }


}
