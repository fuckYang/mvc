package com.yql.demo.entry;

import com.yql.demo.service.DemoService;
import com.yql.mvcframework.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@MyController
@MyRequestMapping("/test")
public class TestController {
    @MyAutowired private DemoService demoService;

    @MyRequestMapping("/eidt.json")
    public String edit(HttpServletRequest req, HttpServletResponse resp, @MyRequestParm("name") String name){
        String result = demoService.get(name);
        try {
            //req.getRequestDispatcher("/index.jsp").forward(req, resp);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "index";
    }
    @MyRequestMapping("/add.json")
    public String add(HttpServletRequest req, HttpServletResponse resp, String name, Model model){
        String result = demoService.get(name);
        model.addAttr("result",result);
        model.addAttr("add",result);
        return "WEB-INF/123";
    }
    @MyRequestMapping("/update.json")
    public void update(HttpServletRequest req, HttpServletResponse resp, @MyRequestParm("name") String name){
        String result = demoService.get(name);
        try {
            resp.getWriter().write(result);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    @MyRequestMapping("/remove.json")
    public void remove(HttpServletRequest req, HttpServletResponse resp, @MyRequestParm("name") String name){
        String result = demoService.get(name);
        try {
            resp.getWriter().write(result);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
