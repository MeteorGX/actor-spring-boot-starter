package com.meteorcat.spring.boot.starter;


import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.ApplicationContext;

@ConfigurationProperties(prefix = "com.meteorcat.spring.boot.starter")
public class ActorConfigProperty {

    private ApplicationContext ctx;

    private Integer monitorCore = 4;

    private Integer monitorIdleCore = 0;


    private Integer containerCapacity = 16;

    private Integer configurerCapacity = 16;


    public void setApplicationContext(ApplicationContext ctx) {
        this.ctx = ctx;
    }

    public ApplicationContext getApplicationContext() {
        return ctx;
    }

    public void setMonitorCore(Integer monitorCore) {
        this.monitorCore = monitorCore;
    }

    public Integer getMonitorCore() {
        return monitorCore;
    }


    public void setMonitorIdleCore(Integer monitorIdleCore) {
        this.monitorIdleCore = monitorIdleCore;
    }

    public Integer getMonitorIdleCore() {
        return monitorIdleCore;
    }

    public void setContainerCapacity(Integer containerCapacity) {
        this.containerCapacity = containerCapacity;
    }

    public Integer getContainerCapacity() {
        return containerCapacity;
    }

    public void setConfigurerCapacity(Integer configurerCapacity) {
        this.configurerCapacity = configurerCapacity;
    }

    public Integer getConfigurerCapacity() {
        return configurerCapacity;
    }
}
