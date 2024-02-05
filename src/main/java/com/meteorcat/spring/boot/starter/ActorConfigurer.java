package com.meteorcat.spring.boot.starter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.lang.NonNull;

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

    private final Logger logger = LoggerFactory.getLogger(ActorConfigurer.class);

    /**
     * Searching all @ActorMapping methods on inheritance subclasses of ActorConfigurer
     * 搜索继承 ActorConfigurer 子类对象全部的 @ActorMapping 方法
     */
    private Map<Integer, ActorFuture> futures;

    /**
     * Search for all Mapping values whose inheritance actor configured subclasses
     * 搜索继承 ActorConfigurer 子类对象全部的 Mapping 对应值
     */
    private List<Integer> values;

    /**
     * Listening Actor's Message Queue
     * 监听的 Actor 消息队列
     */
    private final Queue<ActorMessage> events = new LinkedList<>();


    /**
     * container context | 容器 上下文
     */
    private ActorEventContainer container;


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
     * Collection capacity
     * 容器默认数量
     */
    private int capacity = 6;


    /**
     * Spring Application Context
     * Spring 应用上下文
     */
    private ApplicationContext context;


    /**
     * Event Monitor
     * 事件管理器
     */
    private ActorEventMonitor monitor;


    /**
     * Actor initialisation method to be called from @Bean
     * Actor 初始化方法, 用于 @Bean 调用
     */
    public void construct() {
        Class<? extends ActorConfigurer> configurer = this.getClass();
        String configurerName = configurer.getName();
        EnableActor enableActor = configurer.getAnnotation(EnableActor.class);
        if (enableActor == null || !enableActor.owner().getName().equals(configurerName)) {
            logger.error("Not Implemented @EnableActor(own = {})", configurerName);
            System.exit(1);
        }
        capacity = enableActor.capacity();// default capacity
        values = new ArrayList<>(capacity);
        futures = new HashMap<>(capacity);

        // search class methods
        Method[] methods = configurer.getMethods();
        for (Method method : methods) {
            ActorMapping mapping = method.getAnnotation(ActorMapping.class);
            if (mapping != null) {
                // fetch variables
                Integer op = mapping.value();
                int[] status = mapping.state();

                // create
                ActorFuture future = new ActorFuture(op, this, method, status);
                logger.info("Load @ActorMapping({}) = {}", configurerName, future);
                values.add(op);
                futures.put(op, future);
            }
        }

        // initialize
        try {
            init();
        } catch (Exception exception) {
            logger.trace(exception.getMessage());
        }
    }


    /**
     * Actor invocation method for @Bean calls
     * Actor 退出调用的方法, 用于 @Bean 调用
     */
    public void destruct() {
        try {
            destroy();
        } catch (Exception exception) {
            logger.trace(exception.getMessage());
        }
    }


    /**
     * Initialization
     */
    public abstract void init() throws Exception;

    /**
     * Exit
     */
    public abstract void destroy() throws Exception;


    /**
     * Data preprocessing | 数据预处理
     *
     * @param params data
     * @return Object[]
     */
    public Object[] filter(Object[] params) {
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
            values = new ArrayList<>(0);
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
     */
    public void execute(Integer value, Object... args) {
        if (futures != null) {
            ActorFuture future = futures.get(value);
            if (future != null) {
                try {
                    future.invoke(args);
                } catch (Exception exception) {
                    logger.error(exception.getMessage());
                }
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
     */
    public void execute(@NonNull Integer value, @NonNull Integer state, Object... args) {
        if (futures != null) {
            ActorFuture future = futures.get(value);
            if (future == null) {
                return;
            }

            int[] status = future.getStatus();
            if (status.length == 0 || Arrays.binarySearch(status, state) >= 0) {
                try {
                    future.invoke(args);
                } catch (Exception exception) {
                    logger.error(exception.getMessage());
                }
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
        int[] status = future.getStatus();
        if (status.length == 0 || Arrays.binarySearch(status, state) >= 0) {
            // push message
            events.add(new ActorMessage(value, state, args));
        }
        writeLock.unlock();
    }

    /**
     * check message queue
     *
     * @return boolean
     */
    public boolean isEmptyEvent() {
        return events.isEmpty();
    }

    /**
     * Multi-thread execution of message queue processing
     * 多线程执行的消息队列处理
     */
    public void run() {
        if (futures == null) return;
        readLock.lock();
        if (events.isEmpty()) {
            return;
        }

        ActorMessage event = events.poll();
        ActorFuture future = futures.get(event.getValue());
        if (future == null) {
            readLock.unlock();
            return;
        }

        int[] status = future.getStatus();
        if (status.length == 0 || Arrays.binarySearch(status, event.getState()) >= 0) {
            try {
                future.invoke(filter(event.getArgs()));
            } catch (Exception exception) {
                logger.error(exception.getMessage());
            } finally {
                readLock.unlock();
            }
        } else {
            readLock.unlock();
        }
    }


    /**
     * Collection capacity
     *
     * @return int
     */
    public int getCapacity() {
        return capacity;
    }


    /**
     * Set container context
     *
     * @param container context
     */
    public void setContainer(ActorEventContainer container) {
        this.container = container;
    }

    /**
     * get container context
     *
     * @return ActorEventContainer
     */
    public ActorEventContainer getContainer() {
        return container;
    }


    /**
     * Set ApplicationContext
     *
     * @param context ApplicationContext
     */
    public void setContext(ApplicationContext context) {
        this.context = context;
    }

    /**
     * Set ApplicationContext
     *
     * @return ApplicationContext
     */
    public ApplicationContext getContext() {
        return context;
    }

    /**
     * Set ActorEventMonitor
     *
     * @param monitor ActorEventMonitor
     */
    public void setMonitor(ActorEventMonitor monitor) {
        this.monitor = monitor;
    }

    /**
     * Get ActorEventMonitor
     *
     * @return ActorEventMonitor
     */
    public ActorEventMonitor getMonitor() {
        return monitor;
    }
}
