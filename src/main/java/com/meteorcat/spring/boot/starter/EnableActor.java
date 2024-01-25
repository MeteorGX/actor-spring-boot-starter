package com.meteorcat.spring.boot.starter;


import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.lang.annotation.*;


/**
 * Make a class an Actor global singleton| 将类设置为 Actor 全局实例化
 */
@Component
@Inherited
@Documented
@Order(value = 0)
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface EnableActor {


    /**
     * The EnableActor object needs to be written | 需要把 EnableActor 对象写入
     *
     * @return ActorConfigurer
     */
    Class<? extends ActorConfigurer> owner();


    /**
     * Collection Capacity| 容器默认的数量
     *
     * @return int
     */
    int capacity() default 16;
}
