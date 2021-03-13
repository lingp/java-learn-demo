package com.lin.demo.service.impl;

import com.lin.demo.service.TestService;
import com.lin.framework.annotation.Service;

@Service
public class TestServiceImpl implements TestService {
    @Override
    public void sayHello() {
        System.out.println("hello world");
    }
}
