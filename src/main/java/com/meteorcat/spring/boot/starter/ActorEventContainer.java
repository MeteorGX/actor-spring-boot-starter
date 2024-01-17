package com.meteorcat.spring.boot.starter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
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
     * Idle threads
     */
    private int idleThreads = 0;

    public ActorEventContainer(ActorEventMonitor monitor) {
        this.monitor = monitor;
        this.configurers = new ArrayList<>();
    }

    public ActorEventContainer(ActorEventMonitor monitor, int configurerCapacity) {
        this.monitor = monitor;
        this.configurers = new ArrayList<>(configurerCapacity);
    }

    public ActorEventContainer(int capacity, ActorEventMonitor monitor) {
        super(capacity);
        this.monitor = monitor;
        this.configurers = new ArrayList<>();
    }


    public ActorEventContainer(int capacity, ActorEventMonitor monitor, int configurerCapacity) {
        super(capacity);
        this.monitor = monitor;
        this.configurers = new ArrayList<>(configurerCapacity);
    }


    public void init() throws Exception {
        forEach((op, configurer) -> {
            if (!configurers.contains(configurer)) {
                configurers.add(configurer);
            }
        });

        for (ActorConfigurer configurer : configurers) {
            configurer.init();
        }

        run();
    }


    public void destroy() throws Exception {
        try {
            monitor.shutdown();
            while (!monitor.isShutdown()){ /* wait monitor quit */}
        } finally {
            for (ActorConfigurer configurer : configurers) {
                configurer.destroy();
            }
        }
    }


    public void setIdleThreads(int nThreads) {
        idleThreads = nThreads;
    }

    public void run() {
        int coreThreads = monitor.getCorePoolSize();
        int activeThreads = idleThreads >= coreThreads ? coreThreads : coreThreads - idleThreads;
        for (int i = 0; i < activeThreads; i++) {
            long idx = i + 1;
            monitor.scheduleAtFixedRate(() -> {
                if (monitor.isShutdown()) {
                    return;
                }
                forEach((op, configurer) -> configurer.run());
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


    public int getIdleThreads() {
        return idleThreads;
    }

    public int getCoreThreads() {
        return monitor.getCorePoolSize();
    }
}
