package com.meteorcat.spring.boot.starter;


import org.springframework.stereotype.Component;

import java.lang.annotation.*;


/**
 * Make a class an Actor global singleton| 将类设置为 Actor 全局实例化
 */
@Component
@Inherited
@Documented
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface EnableActor {


    /**
     * The EnableActor object needs to be written | 需要把 EnableActor 对象写入
     *
     * @return ActorConfigurer
     */
    Class<? extends ActorConfigurer> owner();
}
