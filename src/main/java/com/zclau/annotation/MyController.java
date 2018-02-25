package com.zclau.annotation;

import java.lang.annotation.*;

/**
 * Controller注解，它只能标注在类上面
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface MyController {

    /**
     * 表示给controller注册别名
     */
    String value() default "";
}
