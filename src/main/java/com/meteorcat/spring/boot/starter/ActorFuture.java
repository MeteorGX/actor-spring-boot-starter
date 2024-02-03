package com.meteorcat.spring.boot.starter;

import org.springframework.lang.NonNull;

import java.io.Serializable;
import java.lang.reflect.Method;
import java.util.Arrays;

/**
 * EnableActor @ActorMapping methods | Actor 启用的 @ActorMapping 方法
 */
public class ActorFuture implements Serializable {

    /**
     * ActorMapping.value
     * ActorMapping映射值: value
     */
    private final Integer value;

    /**
     * EnableActor instance
     * EnableActor实例
     */
    private final Object instance;

    /**
     * ActorMapping method
     * ActorMapping的方法
     */
    private final Method method;

    /**
     * ActorMapping.state
     * ActorMapping映射数组: state
     */
    private final int[] status;

    /**
     * ActorMapping.threadState
     * ActorMapping是否设定线程安全
     */
    private final ActorThreadState threadState;


    public ActorFuture(@NonNull Integer value, Object instance, @NonNull Method method, @NonNull int[] status, @NonNull ActorThreadState threadState) {
        this.value = value;
        this.instance = instance;
        this.method = method;
        this.status = status;
        this.threadState = threadState;
    }


    public @NonNull Integer getValue() {
        return value;
    }

    public Object getInstance() {
        return instance;
    }

    public @NonNull Method getMethod() {
        return method;
    }

    public @NonNull int[] getStatus() {
        return status;
    }

    public @NonNull ActorThreadState getThreadState() {
        return threadState;
    }

    @Override
    public String toString() {
        return "ActorFuture{" +
                "value=" + value +
                ", instance=" + instance +
                ", method=" + method +
                ", status=" + Arrays.toString(status) +
                ", threadState=" + threadState +
                '}';
    }

    public void invoke(Object... args) throws Exception {
        method.invoke(instance, args);
    }
}
