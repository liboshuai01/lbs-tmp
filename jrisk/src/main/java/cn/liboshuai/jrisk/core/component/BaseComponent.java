package cn.liboshuai.jrisk.core.component;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.atomic.AtomicBoolean;

/**
 * 基础组件抽象类
 * 实现了通用的生命周期状态管理
 */
public abstract class BaseComponent implements Lifecycle {

    protected final Logger log = LoggerFactory.getLogger(getClass());

    /**
     * JUC 知识点: volatile
     * 保证该变量对所有线程立即可见，用于控制 while 循环的退出
     */
    protected volatile boolean running = false;

    /**
     * JUC 知识点: AtomicBoolean
     * 保证 init/start/stop 操作的原子性，防止并发调用导致状态错乱
     */
    private final AtomicBoolean initialized = new AtomicBoolean(false);

    protected String name;

    public BaseComponent(String name) {
        this.name = name;
    }

    @Override
    public void init() {
        if (initialized.compareAndSet(false, true)) {
            log.info("[{}] Initializing...", name);
            doInit();
            log.info("[{}] Initialized.", name);
        } else {
            log.warn("[{}] Already initialized.", name);
        }
    }

    @Override
    public void start() {
        if (!initialized.get()) {
            throw new IllegalStateException("Component " + name + " not initialized yet!");
        }
        if (!running) {
            log.info("[{}] Starting...", name);
            running = true;
            doStart();
            log.info("[{}] Started.", name);
        }
    }

    @Override
    public void stop() {
        if (running) {
            log.info("[{}] Stopping...", name);
            running = false; // volatile 写，强制刷入主存，其他线程立马可见
            doStop();
            log.info("[{}] Stopped.", name);
        }
    }

    @Override
    public boolean isRunning() {
        return running;
    }

    // 子类实现具体的逻辑
    protected abstract void doInit();
    protected abstract void doStart();
    protected abstract void doStop();
}