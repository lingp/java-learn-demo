package com.lin.demo.action;

import com.lin.demo.service.IQueryService;
import com.lin.framework.context.ApplicationContext;

public class TestIOC {
    public static void main(String[] args) throws Exception {
        ApplicationContext context = new ApplicationContext("application.properties");
        IQueryService queryService = (IQueryService) context.getBean("queryService");
        queryService.query("测试");
        System.out.println("hello world");
    }
}
