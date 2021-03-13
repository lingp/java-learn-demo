package com.lin.demo.controller;

import com.lin.demo.service.TestService;
import com.lin.framework.annotation.Autowired;
import com.lin.framework.annotation.Controller;
import com.lin.framework.annotation.Params;
import com.lin.framework.annotation.RequestMapping;

@Controller
@RequestMapping("/test")
public class TestController {
    @Autowired
    private TestService testService;

    @RequestMapping("/test")
    public String test() {
        testService.sayHello();
        return "test";
    }

    @RequestMapping("/test2")
    public String test2(@Params("name") String name) {
        System.out.println(name);
        return name;
    }
}
