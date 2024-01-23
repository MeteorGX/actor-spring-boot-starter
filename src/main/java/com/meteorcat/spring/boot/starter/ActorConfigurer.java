package com.meteorcat.spring.boot.starter;

import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;

import java.lang.reflect.Method;
import java.util.*;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * Actor Services | Actor基础服务
 * -------------------------------
 * Multi-threaded actor service that handles and forwards messages via a message queue
 * 多线程驱动的 Actor 服务, 采用消息队列处理转发消息
 */
public abstract class ActorConfigurer {

    /**
     * Searching all @ActorMapping methods on inheritance subclasses of ActorConfigurer
     * 搜索继承 ActorConfigurer 子类对象全部的 @ActorMapping 方法
     */
    @Nullable
    private Map<Integer, ActorFuture> futures;

    /**
     * Search for all Mapping values whose inheritance actor configured subclasses
     * 搜索继承 ActorConfigurer 子类对象全部的 Mapping 对应值
     */
    @Nullable
    private List<Integer> values;

    /**
     * Listening Actor's Message Queue
     * 监听的 Actor 消息队列
     */
    @Nullable
    private Queue<ActorMessage> events;


    /**
     * Actor instances with read-write locks
     * Actor 所有的读写锁实例
     */
    private final ReadWriteLock lock = new ReentrantReadWriteLock();

    /**
     * Actor read lock
     * Actor 读取锁
     */
    private final Lock readLock = lock.readLock();

    /**
     * Actor write lock
     * Actor 写入锁
     */
    private final Lock writeLock = lock.writeLock();


    /**
     * Actor initialisation method to be called from @Bean
     * Actor 初始化方法, 用于 @Bean 调用
     *
     * @throws Exception Error
     */
    public void init() throws Exception {
        Class<? extends ActorConfigurer> configurer = this.getClass();
        EnableActor enableActor = configurer.getAnnotation(EnableActor.class);
        if (enableActor == null || !enableActor.owner().getName().equals(configurer.getName())) {
            throw new ClassNotFoundException(String.format("Not Implemented @EnableActor(own = %s)", configurer.getName()));
        }

        // search class methods
        Method[] methods = configurer.getMethods();
        if (values == null) {
            values = new ArrayList<>(methods.length);
        }


        if (futures == null) {
            futures = new HashMap<>(methods.length);
        }


        for (Method method : methods) {
            ActorMapping mapping = method.getAnnotation(ActorMapping.class);
            if (mapping != null) {
                Integer op = mapping.value();
                int[] status = mapping.state();
                List<Integer> state = new ArrayList<>(status.length);
                for (int v : status) {
                    state.add(v);
                }
                values.add(op);
                futures.put(op, new ActorFuture(op, this, method, state));
            }
        }
    }


    /**
     * Actor invocation method for @Bean calls
     * Actor 退出调用的方法, 用于 @Bean 调用
     *
     * @throws Exception Error
     */
    public void destroy() throws Exception {
    }


    /**
     * Data preprocessing | 数据预处理
     *
     * @param params data
     * @return Object[]
     */
    public Object[] filter(Object[] params) throws Exception {
        return params;
    }


    /**
     * Actor @ActorMapping values
     * Actor 内部 Mapping 对象方法的值
     *
     * @return List
     */
    public List<Integer> values() {
        if (values == null) {
            Class<? extends ActorConfigurer> configurer = this.getClass();
            EnableActor enableActor = configurer.getAnnotation(EnableActor.class);
            if (enableActor == null) {
                values = new ArrayList<>(0);
                return values;
            }

            Method[] methods = configurer.getMethods();
            values = new ArrayList<>(methods.length);
            for (Method method : methods) {
                ActorMapping mapping = method.getAnnotation(ActorMapping.class);
                if (mapping != null) {
                    values.add(mapping.value());
                }
            }
        }
        return values;
    }


    /**
     * Actor @ActorMapping methods
     * Actor 内部 Mapping 对象方法
     *
     * @return Map
     */
    public Map<Integer, ActorFuture> futures() {
        if (futures == null) {
            futures = new HashMap<>(0);
        }
        return futures;
    }


    /**
     * Remove @ActorMapping Method
     * 删除 @ActorMapping 注册方法
     *
     * @param key value
     */
    public void remove(Integer key) {
        if (futures != null) {
            futures.remove(key);
        }
        if (values != null) {
            values.remove(key);
        }
    }


    /**
     * Invoke the @ActorMapping Method inside Actor
     * 唤醒 Actor 内部的 Mapping 方法
     * note: This approach is not thread safe | 该方法不是线程安全
     *
     * @param value @ActorMapping.value
     * @param args  params
     * @throws Exception Error
     */
    public void execute(Integer value, Object... args) throws Exception {
        if (futures != null) {
            ActorFuture future = futures.get(value);
            if (future != null) {
                future.invoke(args);
            }
        }
    }


    /**
     * Invoke the @ActorMapping Method inside Actor
     * 唤醒 Actor 内部的 Mapping 方法
     *
     * @param value @ActorMapping.value
     * @param state @ActorMapping.state
     * @param args  params
     * @throws Exception Error
     */
    public void execute(@NonNull Integer value, @NonNull Integer state, Object... args) throws Exception {
        if (futures != null) {
            ActorFuture future = futures.get(value);
            if (future == null) {
                return;
            }

            List<Integer> status = future.getStatus();
            if (status.isEmpty() || status.contains(state)) {
                future.invoke(args);
            }
        }
    }


    /**
     * Push to actor’s message queue
     * 推送到 Actor 的消息队列
     *
     * @param value @ActorMapping.value
     * @param state @ActorMapping.state
     * @param args  params
     */
    public void invoke(@NonNull Integer value, @NonNull Integer state, Object... args) {
        if (futures == null) return;

        // lock
        writeLock.lock();

        // state exists?
        ActorFuture future = futures.get(value);
        if (future == null) {
            writeLock.unlock();
            return;
        }

        // state pass?
        List<Integer> status = future.getStatus();
        if (status.isEmpty() || status.contains(state)) {
            // push message
            if (events == null) {
                events = new LinkedList<>();
            }
            events.add(new ActorMessage(value, state, args));
        }
        writeLock.unlock();
    }


    /**
     * Multi-thread execution of message queue processing
     * 多线程执行的消息队列处理
     */
    public void run() throws RuntimeException {
        if (events == null || futures == null) return;
        readLock.lock();
        if (events.isEmpty()) {
            readLock.unlock();
            return;
        }

        ActorMessage event = events.poll();
        ActorFuture future = futures.get(event.getValue());
        if (future == null) {
            readLock.unlock();
            return;
        }

        List<Integer> status = future.getStatus();
        if (status.isEmpty() || status.contains(event.getState())) {
            try {
                future.invoke(filter(event.getArgs()));
            } catch (Exception exception) {
                throw new RuntimeException(exception);
            } finally {
                readLock.unlock();
            }
        } else {
            readLock.unlock();
        }
    }

}
