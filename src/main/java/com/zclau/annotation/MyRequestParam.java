package com.zclau.annotation;

import java.lang.annotation.*;

/**
 * RequestParam注解,只能注解在参数上
 */

@Target(ElementType.PARAMETER)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface MyRequestParam {

    /**
     * 表示参数的别名，必填
     */
    String value();
}
