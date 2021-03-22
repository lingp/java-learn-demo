package com.lin.demo.action;

import com.lin.framework.context.ApplicationContext;

public class TestIOC {
    public static void main(String[] args) {
        ApplicationContext context = new ApplicationContext("application.properties");
        System.out.println("hello world");
    }
}
