package com.yql.aop;

import net.sf.cglib.proxy.Enhancer;
import net.sf.cglib.proxy.MethodInterceptor;
import net.sf.cglib.proxy.MethodProxy;

import java.lang.reflect.Method;

public class AopAspectProxy implements MethodInterceptor {

    private Object aspect;


    @SuppressWarnings("unchecked")
    public <T> T getInstance(Class<T> clazz,Object aspect){
        this.aspect=aspect;
        Enhancer enhancer=new Enhancer();
        enhancer.setSuperclass(clazz);
        enhancer.setCallback(this);
        return (T)enhancer.create();
    }

    @Override
    public Object intercept(Object o, Method method, Object[] objects, MethodProxy methodProxy) throws Throwable {
        Method[] methods=aspect.getClass().getMethods();
        for(Method method1:methods){
            MyBefore before=method1.getAnnotation(MyBefore.class);
            if(before!=null){
                JoinPoint joinPoint=new JoinPoint();
                joinPoint.setMethod(method);
                joinPoint.setArgs(objects);
                method1.invoke(aspect,joinPoint);
            }
        }
        Object result = methodProxy.invokeSuper(o,objects);//调用目标方法
        return result;
    }
}
