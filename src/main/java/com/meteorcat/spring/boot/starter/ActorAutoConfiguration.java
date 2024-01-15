package com.meteorcat.spring.boot.starter;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties(ActorEventContainer.class)
public class ActorAutoConfiguration {

    final ApplicationContext ctx;

    public ActorAutoConfiguration(ApplicationContext ctx) {
        this.ctx = ctx;
    }

    @Bean(initMethod = "init", destroyMethod = "destroy")
    public ActorEventContainer container(ActorEventContainer container) {
        return container == null ? ActorSearcher.build(ctx, 4) : container;
    }

}
