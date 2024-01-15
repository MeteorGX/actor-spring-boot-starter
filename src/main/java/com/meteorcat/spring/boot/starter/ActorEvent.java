package com.meteorcat.spring.boot.starter;

/**
 * Actor event| Actor 事件
 */
public interface ActorEvent extends Runnable {

    /**
     * Get Event Id
     * 获取任务ID
     *
     * @return long
     */
    long getEventId();


    /**
     * Set Event Id
     * 设置任务ID
     *
     * @param id long
     */
    void setTaskId(long id);
}
