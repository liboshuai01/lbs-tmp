package cn.liboshuai.demo.core.lifecycle;

/**
 * 标准生命周期接口，所有核心组件都应实现
 */
public interface LifeCycle {
    void init();
    void start();
    void shutdown();
}