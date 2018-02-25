package com.zclau.servlet;

import com.zclau.annotation.MyController;
import com.zclau.annotation.MyRequestMapping;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Method;
import java.net.URL;
import java.util.*;

/**
 *
 */
public class MyDispatcherServlet extends HttpServlet {

    private Properties properties = new Properties();

    private List<String> classNames = new ArrayList<>();

    private Map<String, Object> ioc = new HashMap<>();

    private Map<String, Method> handlerMapping = new HashMap<>();

    private Map<String, Object> controllerMap = new HashMap<>();



    @Override
    public void init(ServletConfig config) throws ServletException {
        // 1.load configuration
        doLoadConfig(config.getInitParameter("contextConfigLocation"));

        // 2.scan all the classes in given package
        doScan(properties.getProperty("scanPackage"));

        // 3.instantiate the classes by reflect, and put into ioc container
        doInstantiate();

        // 4.initiate HandlerMapping (make url correspond with method)
        initHandlerMapping();
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        doPost(req, resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        try {
            // 处理请求
            doDispatch(req, resp);
        } catch (Exception e) {
            resp.getWriter().write("500!! Server Exception");
        }
    }

    private void doDispatch(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        if (handlerMapping.isEmpty()) {
            return;
        }

        String url = req.getRequestURI();
        String contextPath = req.getContextPath();

        url = url.replace(contextPath, "").replaceAll("/+", "/");

        if (!this.handlerMapping.containsKey(url)) {
            resp.getWriter().write("404 NOT FOUND!");
            return;
        }

        Method method = this.handlerMapping.get(url);

        // 获取方法的参数类型
        Class<?>[] parameterTypes = method.getParameterTypes();

        // 获取请求参数
        Map<String, String[]> parameterMap = req.getParameterMap();
        List<String> paramList = new ArrayList<>(parameterMap.size());
        parameterMap.forEach((key, value) -> paramList.add(value[0]));

        Object[] paramValues = new Object[parameterTypes.length];

        int paramIndex = 0;
        for (int i = 0; i < parameterTypes.length; i++) {
            String type = parameterTypes[i].getSimpleName();

            if ("HttpServletRequest".equals(type)) {
                paramValues[i] = req;
                continue;
            }

            if ("HttpServletResponse".equals(type)) {
                paramValues[i] = resp;
                continue;
            }

            if ("String".equals(type)) {
                paramValues[i] = paramList.get(paramIndex++);
            }
        }

        // obj是method所对应的实例 在ioc容器中
        try {
            method.invoke(this.controllerMap.get(url), paramValues);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    /**
     * 把web.xml中的contextConfigLocation对应value值的文件加载到流里面
     */
    private void doLoadConfig(String location) {

        try(InputStream resourceAsStream = this.getClass().getClassLoader().getResourceAsStream(location)) {
            properties.load(resourceAsStream);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    private void doScan(String packageName) {
        URL url = this.getClass().getClassLoader().getResource("/" + packageName.replaceAll("\\.", "/"));
        File dir = new File(url.getFile());
        for (File file : dir.listFiles()) {
            if (file.isDirectory()) {
                // 递归读取包
                doScan(packageName + "." + file.getName());
            } else {
                String className = (packageName + "." + file.getName()).replace(".class", "");
                classNames.add(className);
            }
        }
    }

    private void doInstantiate() {
        if (classNames.isEmpty()) {
            return;
        }

        for (String className : classNames) {
            try {
                // 把类搞出来,反射来实例化(只有加@MyController需要实例化)
                Class clazz = Class.forName(className);
                if (clazz.isAnnotationPresent(MyController.class)) {
                    ioc.put(toLowerFirstWorld(clazz.getSimpleName()), clazz.newInstance());
                } else {
                    continue;
                }
            } catch (Exception e) {
                e.printStackTrace();
                continue;
            }

        }
    }

    private void initHandlerMapping() {
        if (ioc.isEmpty()) {
            return;
        }

        try {
            ioc.forEach((className, instance) -> {
                Class clazz = instance.getClass();
                if (!clazz.isAnnotationPresent(MyController.class)) {
                    return;
                }

                String baseUrl = "";
                if (clazz.isAnnotationPresent(MyRequestMapping.class)) {
                    MyRequestMapping annotation = (MyRequestMapping) clazz.getAnnotation(MyRequestMapping.class);
                    baseUrl = annotation.value();
                }

                Method[] methods = clazz.getMethods();
                for (Method method : methods) {
                    if (!method.isAnnotationPresent(MyRequestMapping.class)) {
                        continue;
                    }

                    MyRequestMapping annotation = method.getAnnotation(MyRequestMapping.class);
                    String url = annotation.value();

                    url = (baseUrl + "/" + url).replaceAll("/+", "/");
                    handlerMapping.put(url, method);
                    controllerMap.put(url, instance);
                    System.out.println(url + "," + method);
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private String toLowerFirstWorld(String name) {
        char[] charArray = name.toCharArray();
        charArray[0] += 32;
        return String.valueOf(charArray);
    }
}
