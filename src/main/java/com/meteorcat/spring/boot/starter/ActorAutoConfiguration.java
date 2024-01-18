package com.meteorcat.spring.boot.starter;

import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

// todo: auto configuration actor
@Order
@Service
@Configuration
@EnableConfigurationProperties(ActorConfigProperty.class)
public class ActorAutoConfiguration {

    ApplicationContext ctx;

    public ActorAutoConfiguration(ApplicationContext ctx) {
        this.ctx = ctx;
    }

    @ConditionalOnMissingBean
    @Bean(initMethod = "init", destroyMethod = "destroy")
    public ActorEventContainer container(ActorConfigProperty property) {
        if (property == null) {
            return null;
        }

        ctx = property.getApplicationContext() == null ? ctx : property.getApplicationContext();
        if (ctx == null) {
            return null;
        }
        property.setApplicationContext(ctx);


        ActorEventMonitor monitor = new ActorEventMonitor(property.getMonitorCore());
        ActorEventContainer container = new ActorEventContainer(
                monitor,
                property.getContainerCapacity()
        );
        container.setIdleThreads(property.getMonitorIdleCore());

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
