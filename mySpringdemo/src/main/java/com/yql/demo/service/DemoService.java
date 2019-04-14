package com.yql.demo.service;

import com.yql.mvcframework.MyService;

@MyService
public class DemoService implements IDemoService {

    @Override
    public String get(String name) {
        return "My name is "+name;
    }
}
