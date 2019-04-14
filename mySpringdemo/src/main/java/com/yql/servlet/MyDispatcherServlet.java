package com.yql.servlet;

import com.yql.aop.*;
import com.yql.mvcframework.*;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.net.URL;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MyDispatcherServlet extends HttpServlet {
    /**
     * 配置信息
     */
    private Properties properties=new Properties();
    /**
     * 类的class字符串
     */
    private List<String> classNames=new ArrayList<>();
    /**
     * ioc容器
     */
    private Map<String,Object> ioc=new HashMap<>();
    /**
     * handler
     */
    //private Map<String,Method> handlerMapping = new HashMap<>();
    private List<Handler> handlerMapping = new ArrayList<>();
    @Override
    public void init(ServletConfig config) throws ServletException {
        //1、获取配置文件信息
            doLoadConfig(config.getInitParameter("contextConfigLocation"));
        //2、根据配置文件信息递归扫描所有文件
            doScanner(properties.getProperty("scanPackage"));
        //3、实例化IOC容器
            doInstance();
        //4、实例化依赖注入
            doAutorited();
        //5、初始化handler
            initHandler();
    }

    private void initHandler() {
        if(ioc.isEmpty()){return;}
        for(Map.Entry<String,Object> entry:ioc.entrySet()){
            Class<?> clazz=entry.getValue().getClass();
            if(!clazz.isAnnotationPresent(MyController.class)){continue;}
            String baseUrl="";
            if(clazz.isAnnotationPresent(MyRequestMapping.class)){
                baseUrl=clazz.getAnnotation(MyRequestMapping.class).value();
            }
            /*for(Method method:methods){
                if(!method.isAnnotationPresent(MyRequestMapping.class)){continue;}
                MyRequestMapping mapping=method.getAnnotation(MyRequestMapping.class);
                String url =baseUrl+mapping.value();
                url=url.replaceAll("/+","/");
                handlerMapping.put(url,method);
                //System.out.println("Mapping: "+url+","+method);
            }*/

            Object o=entry.getValue();
            Method[] methods = clazz.getMethods();
            for(Method method:methods){
                if(!method.isAnnotationPresent(MyRequestMapping.class)){continue;}
                MyRequestMapping mapping=method.getAnnotation(MyRequestMapping.class);
                String regex =("/"+baseUrl+mapping.value()).replaceAll("/+","/");
                Pattern pattern = Pattern.compile(regex);
                handlerMapping.add(new Handler(o,method,pattern));
                //System.out.println("Mapping: "+url+","+method);
            }
        }
    }

    private Object checkAspect() {
        for (Map.Entry<String,Object> entry:ioc.entrySet()){
            MyAspect aspect=entry.getValue().getClass().getAnnotation(MyAspect.class);
            if(aspect==null){continue;}
            Method[] methods=entry.getValue().getClass().getMethods();
            for(Method method:methods){
                MyPointcut pointcut=method.getAnnotation(MyPointcut.class);
                if(pointcut==null){continue;}
                String v=pointcut.value().trim();
                if("".equals(v)){continue;}
                if(v.equals(MyRequestMapping.class.getName())){
                    return entry.getValue();
                }
            }
        }
        return null;
    }

    private void doAutorited() {
        if(ioc.isEmpty()){return;}
        for (Map.Entry<String,Object> entry:ioc.entrySet()) {
            //强制注入，无论字段是否是public或private修饰
            Field[] fields = entry.getValue().getClass().getDeclaredFields();
            for (Field field:fields) {
                if(!field.isAnnotationPresent(MyAutowired.class)){continue;}
                MyAutowired autowired=field.getAnnotation(MyAutowired.class);
                String beanName=autowired.value().trim();
                if("".equals(beanName)){
                    beanName=lowerFirstCase(field.getType().getSimpleName());
                }
                //强制赋值
                field.setAccessible(true);
                try {
                    //给字段赋值，第一个参数为实例对象，第二个参数为值。
                    field.set(entry.getValue(),ioc.get(beanName));
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                    continue;
                }
            }
        }
    }

    private void doInstance() {
        if(classNames.isEmpty()){return;}
        try {
            for (String className:classNames) {
                Class<?> clazz = Class.forName(className);
                //实例化对象
                //判断类是否有注解
                if(clazz.isAnnotationPresent(MyController.class)){
                    String beanName = clazz.getSimpleName();
                    //首字母小写
                    beanName=lowerFirstCase(beanName);
                    //装进ioc容器中
                    ioc.put(beanName,clazz.newInstance());
                }else if(clazz.isAnnotationPresent(MyService.class)){
                    //判断是否有自定义值
                    MyService service = clazz.getAnnotation(MyService.class);
                    String beanName=service.value();//优先选择自定义值
                    if("".equals(beanName.trim())){
                        beanName=lowerFirstCase(clazz.getSimpleName());
                    }
                    Object instance=clazz.newInstance();
                    ioc.put(beanName,instance);
                    //判断是否是接口
                    Class<?>[] interfaces=clazz.getInterfaces();
                    for (Class<?> i:interfaces) {
                        //蒋接口类名作为key
                        ioc.put(lowerFirstCase(i.getSimpleName()),instance);
                    }
                }else if(clazz.isAnnotationPresent(MyBean.class)){
                    String beanName = clazz.getSimpleName();
                    //首字母小写
                    beanName=lowerFirstCase(beanName);
                    //装进ioc容器中
                    ioc.put(beanName,clazz.newInstance());
                }else {
                    continue;
                }
            }
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InstantiationException e) {
            e.printStackTrace();
        }
    }

    private void doScanner(String packageName) {
        URL url = this.getClass().getClassLoader()
                .getResource("/"+packageName.replaceAll("\\.","/"));
        File classDir=new File(url.getFile());
        for (File file:classDir.listFiles()) {
            if(file.isDirectory()){
                doScanner(packageName+"."+file.getName());
            }else {
                String className=packageName+"."+file.getName().replace(".class","");
                classNames.add(className);
            }
        }
    }

    private void doLoadConfig(String configLocation) {
        InputStream is = this.getClass().getClassLoader().getResourceAsStream(configLocation);
        try {
            properties.load(is);
        } catch (IOException e) {
            e.printStackTrace();
        }finally {
            if(is!=null){
                try {
                    is.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public String lowerFirstCase(String str){
        char[] chars=str.toCharArray();
        chars[0]+=32;
        return String.valueOf(chars);
    }
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        this.doPost(req, resp);
    }

    /**
     * 运行时阶段
     * @param req
     * @param resp
     * @throws ServletException
     * @throws IOException
     */
    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
       /* String url = req.getRequestURI();
        String contextPath = req.getContextPath();
        url=url.replaceAll(contextPath,"").replaceAll("/+","/");
        if(!handlerMapping.containsKey(url)){
            resp.getWriter().write("404 Not Found");
            return;
        }
        Method method = handlerMapping.get(url);
        System.out.println("++++++++++++++++++++++++++++++++++++++++++++++++++++++--"+method);*/
        try {
            req.setCharacterEncoding("UTF-8");
            resp.setContentType("text/html; charset=utf-8");
            doDispatch(req,resp);
        } catch (Exception e) {
            e.printStackTrace();
            resp.getWriter().write("500 服务器出错");
        }
    }

    private void doDispatch(HttpServletRequest req, HttpServletResponse resp) throws IOException, InvocationTargetException, IllegalAccessException {
            Handler handler=getHandler(req);
            if(handler==null){
                resp.getWriter().write("404 Not Found");
                return;
            }
            //获取方法的参数列表
            Class<?>[] paramTypes=handler.method.getParameterTypes();
            //保存需要自动保存的参数值
            Object[] paramValues = new Object[paramTypes.length];
            //从req请求中获取参数
            Map<String,String[]> params = req.getParameterMap();
            for(Map.Entry<String,String[]> param : params.entrySet()){
                String paramValue = Arrays.toString(param.getValue()).replaceAll("\\[|\\]","");
                if(!handler.paramIndexMapping.containsKey(param.getKey())){continue;}
                //如果找到匹配的参数，则开始赋值。
                int index = handler.paramIndexMapping.get(param.getKey());
                paramValues[index] = myConvert(paramTypes[index],paramValue);
            }
            //设置方法中的req和resp对象
            int reqIndex=handler.paramIndexMapping.get(HttpServletRequest.class.getName());
            paramValues[reqIndex] = req;
            int respIndex = handler.paramIndexMapping.get(HttpServletResponse.class.getName());
            paramValues[respIndex] = resp;
            Integer modelIndex = handler.paramIndexMapping.get(Model.class.getName());
            if(modelIndex!=null){
                paramValues[modelIndex] = new Model();
            }

            Object isAspect=checkAspect();
            Object methodResult=null;
            if(isAspect!=null){
                Object proxyController=new AopAspectProxy().getInstance(handler.controller.getClass(),isAspect);
                //获取父类的class对象
                Class clazz=proxyController.getClass().getSuperclass();
                //获取父类所有字段
                Field[] fields=clazz.getDeclaredFields();
                //解决父类autowrite注解字段为null的问题
                for(Field field:fields){
                    MyAutowired autowired=field.getAnnotation(MyAutowired.class);
                    if(autowired==null){continue;}
                    boolean flg=field.isAccessible();
                    field.setAccessible(true);
                    String beanName=autowired.value().trim();
                    if("".equals(beanName)){
                        beanName=lowerFirstCase(field.getType().getSimpleName());
                    }
                    field.set(proxyController,ioc.get(beanName));
                    field.setAccessible(flg);
                }
                methodResult=handler.method.invoke(proxyController,paramValues);
            }else {
                //调用访问的方法
                methodResult=handler.method.invoke(handler.controller,paramValues);
            }
            if(methodResult instanceof String){
                try {
                    Model model=null;
                    if(modelIndex!=null){
                        model=(Model)paramValues[modelIndex];
                    }
                    if(model!=null && model.getAttr().size()>0){
                        for(Map.Entry<String,Object> attr:model.getAttr().entrySet()){
                            req.getSession().setAttribute(attr.getKey(),attr.getValue());
                            //System.out.println(req.getSession().getAttribute(attr.getKey()));
                        }
                    }
                    req.getRequestDispatcher("/"+methodResult.toString()+".jsp").forward(req, resp);
                } catch (ServletException e) {
                    e.printStackTrace();
                }
            }
    }

    private Object myConvert(Class<?> paramType, String paramValue) {
        if(paramType == Integer.class){
            return Integer.valueOf(paramValue);
        }
        return paramValue;
    }

    private Handler getHandler(HttpServletRequest req) {
        String url = req.getRequestURI();
        String contextPath = req.getContextPath();
        int index=contextPath.length();
        url=url.substring(index);
        url=url.replaceAll("/+","/");
        if(handlerMapping.isEmpty()){return null;}
        for (Handler handler:handlerMapping) {
            Matcher matcher=handler.pattern.matcher(url);
            if(!matcher.matches()){continue;}
            return handler;
        }
        return null;

    }

    private class Handler{
        private Object controller;//保存方法对应的实例
        private Method method;//保存映射的方法
        private Pattern pattern;//url正则匹配
        private Map<String,Integer> paramIndexMapping;

        public Handler(Object controller, Method method, Pattern pattern) {
            this.controller = controller;
            this.method = method;
            this.pattern = pattern;
            this.paramIndexMapping = new HashMap<>();
            putParamIndexMapping(method);
        }

        private void putParamIndexMapping(Method method) {
            //提取方法中所有参数的class对象
            Class<?>[] clazzs = method.getParameterTypes();
            Parameter[] parameters=method.getParameters();
            //提取方法中有注解的参数
            Annotation[] [] ap = method.getParameterAnnotations();
            for(int i=0;i<ap.length;i++){
                Annotation[] annotations = ap[i];
                if(annotations.length>0){
                    for(Annotation annotation:annotations){
                        if(annotation instanceof MyRequestParm){
                            String paramName=((MyRequestParm) annotation).value().trim();
                            if(!"".equals(paramName)){
                                paramIndexMapping.put(paramName,i);
                            }
                        }
                    }
                }else {
                    if(clazzs[i] == HttpServletRequest.class || clazzs[i] == HttpServletResponse.class || clazzs[i]==Model.class )
                        paramIndexMapping.put(clazzs[i].getName(),i);
                    else
                        paramIndexMapping.put(parameters[i].getName(),i);
                }
            }
        }
    }
}
