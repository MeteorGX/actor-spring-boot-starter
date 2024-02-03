package com.meteorcat.spring.boot.starter;

/**
 * Controlling whether an Actor is in the thread pool or not is thread-safe
 * 控制 Actor 是否在线程池中是线程安全的
 * todo: Must be implemented
 */
public enum ActorThreadState {
    ThreadSafe,
    NonThreadSafe,
}
