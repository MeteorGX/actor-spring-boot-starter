package com.meteorcat.spring.boot.starter;

import org.springframework.lang.NonNull;

import java.io.Serializable;
import java.lang.reflect.Method;
import java.util.List;

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
    private final List<Integer> status;

    public ActorFuture(@NonNull Integer value, Object instance, @NonNull Method method, @NonNull List<Integer> status) {
        this.value = value;
        this.instance = instance;
        this.method = method;
        this.status = status;
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

    public @NonNull List<Integer> getStatus() {
        return status;
    }

    @Override
    public String toString() {
        return "ActorFuture{" +
                "value=" + value +
                ", instance=" + instance +
                ", method=" + method +
                ", status=" + status +
                '}';
    }

    public void invoke(Object... args) throws Exception {
        method.invoke(instance, args);
    }
}
