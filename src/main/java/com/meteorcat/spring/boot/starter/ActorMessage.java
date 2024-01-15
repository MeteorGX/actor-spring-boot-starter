package com.meteorcat.spring.boot.starter;

import org.springframework.lang.NonNull;

import java.io.Serializable;
import java.util.Arrays;

/**
 * Actor message object | Actor消息对象
 * -----------------------------------
 * Container class that pushes data to other threads
 * 其他线程的数据推送的容器类
 */
public class ActorMessage implements Serializable {

    /**
     * Actor @ActorMapping.value
     * Actor 指定的值
     */
    private final Integer value;


    /**
     * Actor @ActorMapping.state
     * Actor 指定的状态
     */
    private final Integer state;

    /**
     * push parameter
     * 推送参数
     */
    private final Object[] args;

    /**
     * construct method | 构造方法
     *
     * @param value @ActorMapping.value
     * @param state @ActorMapping.state
     * @param args  params
     */
    public ActorMessage(@NonNull Integer value, @NonNull Integer state, Object[] args) {
        this.value = value;
        this.state = state;
        this.args = args;
    }


    public Integer getValue() {
        return value;
    }

    public Integer getState() {
        return state;
    }

    public Object[] getArgs() {
        return args;
    }

    @Override
    public String toString() {
        return "ActorMessage{" +
                "value=" + value +
                ", state=" + state +
                ", args=" + Arrays.toString(args) +
                '}';
    }
}
