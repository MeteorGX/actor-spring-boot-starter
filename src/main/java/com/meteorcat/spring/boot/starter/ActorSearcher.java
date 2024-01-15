package com.meteorcat.spring.boot.starter;

import org.springframework.context.ApplicationContext;
import org.springframework.lang.NonNull;

import java.util.List;
import java.util.Map;

/**
 * Actor search build tools | Actor对象检索构建工具
 */
public final class ActorSearcher {


    public static ActorEventContainer build(@NonNull ApplicationContext ctx, int coreThreads) {
        return build(ctx, new ActorEventMonitor(coreThreads));
    }

    public static ActorEventContainer build(@NonNull ApplicationContext ctx, ActorEventMonitor monitor) {
        ActorEventContainer container = new ActorEventContainer(monitor);
        Map<String, ActorConfigurer> classes = ctx.getBeansOfType(ActorConfigurer.class);
        for (Map.Entry<String, ActorConfigurer> clazz : classes.entrySet()) {
            ActorConfigurer configurer = clazz.getValue();
            List<Integer> values = configurer.values();
            for (Integer value : values) {
                container.put(value, configurer);
            }
        }
        return container;
    }

}
