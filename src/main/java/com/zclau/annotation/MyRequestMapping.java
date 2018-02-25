package com.zclau.annotation;

import java.lang.annotation.*;

/**
 * RequestMapping注解，可以在类和方法上
 */
@Target({ElementType.TYPE,ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface MyRequestMapping {

    /**
     * 表示访问该方法的url
     */
    String value() default "";
}
