package com.yql.demo.entry;

import com.yql.mvcframework.MyAutowired;
import com.yql.mvcframework.MyController;
import com.yql.mvcframework.MyRequestMapping;
import com.yql.mvcframework.MyRequestParm;
import com.yql.demo.service.IDemoService;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@MyController
@MyRequestMapping("/demo")
public class DemoController {
    @MyAutowired private IDemoService iDemoService;

    @MyRequestMapping("/eidt.json")
    public void edit(HttpServletRequest req, HttpServletResponse resp, @MyRequestParm("name") String name){
        String result = iDemoService.get(name);
        try {
            resp.getWriter().write(result);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
