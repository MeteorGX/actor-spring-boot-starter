package com.meteorcat.spring.boot.starter;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import org.springframework.context.ApplicationContext;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;


/**
 * Actor event container | Actor容器类
 * ------------------------------------
 * Actor Multithreaded Invocation Utilities and Related Class Containers
 * Actor 多线程调用工具和相关类容器
 */
public class ActorEventContainer extends HashMap<Integer, ActorConfigurer> {

    /**
     * event monitor| 事件线程管理器
     */
    private final ActorEventMonitor monitor;

    /**
     * actor instances | actor 相关句柄
     */
    private final List<ActorConfigurer> configurers;


    /**
     * Spring Application Context | Spring 上下文
     */
    private final ApplicationContext context;

    /**
     * Construct
     *
     * @param monitor event pool
     */
    public ActorEventContainer(ActorEventMonitor monitor, ApplicationContext context) {
        this.monitor = monitor;
        this.context = context;
        this.configurers = new ArrayList<>();
    }


    public ActorEventContainer(ActorEventMonitor monitor, ApplicationContext context, int capacity) {
        super(capacity);
        this.monitor = monitor;
        this.context = context;
        this.configurers = new ArrayList<>(capacity);
    }


    public ActorEventContainer(ActorEventMonitor monitor, ApplicationContext context, int capacity, int configurerCapacity) {
        super(capacity);
        this.monitor = monitor;
        this.context = context;
        this.configurers = new ArrayList<>(configurerCapacity);
    }


    @PostConstruct
    public void init() {
        if (context != null) {
            Map<String, ActorConfigurer> classes = context.getBeansOfType(ActorConfigurer.class);
            for (Map.Entry<String, ActorConfigurer> clazz : classes.entrySet()) {
                ActorConfigurer configurer = clazz.getValue();
                if (!configurers.contains(configurer)) {
                    configurer.setContainer(this);
                    configurer.setContext(context);
                    configurer.setMonitor(monitor);
                    configurer.construct();
                    configurers.add(configurer);
                }

                for (Integer value : configurer.values()) {
                    put(value, configurer);
                }
            }
        }
        run();
    }


    @PreDestroy
    public void destroy() {
        monitor.shutdown();
        configurers.forEach(ActorConfigurer::destruct);
    }


    public void run() {
        int coreThreads = monitor.getCorePoolSize();
        for (int i = 0; i < coreThreads; i++) {
            long idx = i + 1;
            monitor.scheduleAtFixedRate(() -> {
                if (monitor.isShutdown()) {
                    return;
                }
                forEach((op, configurer) -> {
                    if (!configurer.isEmptyEvent()) {
                        configurer.run();
                    }
                });
            }, 0, idx * 100 + 1000, TimeUnit.MILLISECONDS);
        }
    }


    public void execute(Runnable runnable) {
        monitor.execute(runnable);
    }


    public ScheduledFuture<?> schedule(Runnable runnable, long delay, TimeUnit unit) {
        return monitor.schedule(runnable, delay, unit);
    }


    public ScheduledFuture<?> scheduleAtFixedRate(Runnable runnable, long initialDelay, long period, TimeUnit unit) {
        return monitor.scheduleAtFixedRate(runnable, initialDelay, period, unit);
    }


    public ScheduledFuture<?> scheduleWithFixedDelay(Runnable runnable, long initialDelay, long delay, TimeUnit unit) {
        return monitor.scheduleAtFixedRate(runnable, initialDelay, delay, unit);
    }


    public int getCoreThreads() {
        return monitor.getCorePoolSize();
    }


    public ApplicationContext getContext() {
        return context;
    }
}
