package com.yql.mvcframework;

import java.util.HashMap;
import java.util.Map;

public class Model {
    private static Map<String,Object> attr=new HashMap<>();
    public void addAttr(String key,Object value){
        attr.put(key,value);
    }
    public Map<String,Object> getAttr(){
        return this.attr;
    }
}
