package com.meteorcat.spring.boot.starter;

import org.springframework.lang.NonNull;

import java.util.List;
import java.util.concurrent.*;

/**
 * Actor event monitor|事件调度管理器
 */
public class ActorEventMonitor extends ScheduledThreadPoolExecutor {



    public ActorEventMonitor(int corePoolSize) {
        super(corePoolSize);
    }

    public ActorEventMonitor(int corePoolSize, ThreadFactory threadFactory) {
        super(corePoolSize, threadFactory);
    }

    public ActorEventMonitor(int corePoolSize, RejectedExecutionHandler handler) {
        super(corePoolSize, handler);
    }

    public ActorEventMonitor(int corePoolSize, ThreadFactory threadFactory, RejectedExecutionHandler handler) {
        super(corePoolSize, threadFactory, handler);
    }


    /**
     * Thread execute method|线程调用
     *
     * @param event Runnable
     */
    public void execute(ActorEvent event) {
        super.execute(event);
    }

    /**
     * Thread execute method|线程调用
     *
     * @param event Runnable
     * @return Future
     */
    public Future<?> submit(ActorEvent event) {
        return super.submit(event);
    }

    /**
     * Thread execute method|线程调用
     *
     * @param event  Runnable
     * @param result callable
     * @param <T>    result class
     * @return T
     */
    public <T> Future<T> submit(ActorEvent event, T result) {
        return super.submit(event, result);
    }


    public ScheduledFuture<?> schedule(ActorEvent event, long delay, TimeUnit unit) {
        return super.schedule(event, delay, unit);
    }


    public ScheduledFuture<?> scheduleAtFixedRate(ActorEvent event, long initialDelay, long period, TimeUnit unit) {
        return super.scheduleAtFixedRate(event, initialDelay, period, unit);
    }
    public ScheduledFuture<?> scheduleWithFixedDelay(ActorEvent event, long initialDelay, long delay, TimeUnit unit) {
        return super.scheduleWithFixedDelay(event, initialDelay, delay, unit);
    }


    @Override
    public void shutdown() {
        super.shutdown();
    }


    @Override
    public boolean isShutdown() {
        return super.isShutdown();
    }


    @Override
    public @NonNull List<Runnable> shutdownNow() {
        return super.shutdownNow();
    }

    @Override
    public boolean isTerminated() {
        return super.isTerminated();
    }

    @Override
    public int getPoolSize() {
        return super.getPoolSize();
    }

    @Override
    public int getCorePoolSize() {
        return super.getCorePoolSize();
    }

    @Override
    public boolean isTerminating() {
        return super.isTerminating();
    }

    @Override
    public boolean remove(Runnable task) {
        return super.remove(task);
    }

    @Override
    public boolean awaitTermination(long timeout, TimeUnit unit) throws InterruptedException {
        return super.awaitTermination(timeout, unit);
    }

}
