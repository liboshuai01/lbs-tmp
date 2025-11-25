package cn.liboshuai.jrisk.core.component;

/**
 * 组件生命周期接口
 * 模拟 Flink 算子的生命周期：初始化 -> 启动 -> 停止
 */
public interface Lifecycle {

    /**
     * 初始化资源 (分配内存、检查配置)
     */
    void init();

    /**
     * 启动组件 (提交任务到线程池、开始消费)
     */
    void start();

    /**
     * 停止组件 (释放资源、关闭连接)
     */
    void stop();

    /**
     * 组件是否正在运行
     */
    boolean isRunning();
}