package com.meteorcat.spring.boot.starter;

import java.lang.annotation.*;


/**
 * Declare Actor method entry | 声明 Actor 方法入口
 */
@Inherited
@Documented
@Target({ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
public @interface ActorMapping {

    /**
     * Actor mapping value, globally unique
     *
     * @return int
     */
    int value() default -1;

    /**
     * Actor mapping state, default all = {}
     *
     * @return int[]
     */
    int[] state() default {};

}
