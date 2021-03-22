package com.lin.demo.aspect;

import com.lin.framework.aop.aspect.JoinPoint;
import lombok.extern.slf4j.Slf4j;

import java.util.Arrays;

@Slf4j
public class LogAspect {

    public void before(JoinPoint joinPoint) {
        //这个方法中的逻辑， 是由我们自己写的
        log.info("Invoker Before Method!!!" +
                "\nTargetObject:" + joinPoint.getThis() +
                "\nArgs:" + Arrays.toString(joinPoint.getArguments()));
    }

    public void after(JoinPoint joinPoint){
        log.info("Invoker After Method!!!" +
                "\nTargetObject:" + joinPoint.getThis() +
                "\nArgs:" + Arrays.toString(joinPoint.getArguments()));
    }

    public void afterThrowing(JoinPoint joinPoint, Throwable ex){
        log.info("出现异常" +
                "\nTargetObject:" + joinPoint.getThis() +
                "\nArgs:" + Arrays.toString(joinPoint.getArguments()) +
                "\nThrows:" + ex.getMessage());
    }
}
