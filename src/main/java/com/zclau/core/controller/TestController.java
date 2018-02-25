package com.zclau.core.controller;

import com.zclau.annotation.MyController;
import com.zclau.annotation.MyRequestMapping;
import com.zclau.annotation.MyRequestParam;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 *
 */
@MyController
@MyRequestMapping("/test")
public class TestController {

    @MyRequestMapping("/doTest")
    public void test1(HttpServletRequest request, HttpServletResponse response,
            @MyRequestParam("param") String param) {
        System.out.println(param);
        try {
            response.getWriter().write("doTest method success! param:" + param);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @MyRequestMapping("/doTest2")
    public void test2(HttpServletRequest request, HttpServletResponse response){
        try {
            response.getWriter().println("doTest2 method success!");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @MyRequestMapping("/doTest3")
    public void test3(HttpServletRequest request, HttpServletResponse response,
            @MyRequestParam("name") String name, @MyRequestParam("age") String age, @MyRequestParam("sex") String sex) {
        try {
            response.getWriter().write("doTest3 method success! name:" + name + ", age: " + age + ", sex: " + sex);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
