package com.yql.demo.entry;

import com.yql.aop.*;
import com.yql.mvcframework.Model;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import java.util.ArrayList;
import java.util.List;

@MyBean
@MyAspect
public class LogAspect {

    private static final Logger logger= LogManager.getLogger(LogAspect.class);

    @MyPointcut("com.yql.mvcframework.MyRequestMapping")
    public void  controller(){}

    @MyBefore("controller()")
    public void before(JoinPoint joinPoint){
        List<String> list=new ArrayList<>();
        for(Object obj:joinPoint.getArgs()){
            if(obj instanceof ServletRequest || obj instanceof ServletResponse || obj instanceof Model) list.add(obj.getClass().getSimpleName()+"对象");
            else list.add(obj.toString());
        }
        logger.debug("用户id：{}，方法签名：{}，方法参数：{}",1,joinPoint.getMethod().getName(), list.toString());
    }
}
