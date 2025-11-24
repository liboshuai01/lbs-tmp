package cn.liboshuai.demo;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

public class AutoSaver {

    private static final Logger log = LoggerFactory.getLogger(AutoSaver.class);

    private final AtomicBoolean isDirty = new AtomicBoolean(false);

    public void edit() {
        log.info("用户开始修改内容...");
        isDirty.set(true);
        try {
            TimeUnit.MILLISECONDS.sleep(100);
        } catch (InterruptedException e) {
            log.info("用户修改内容时被中断");
            Thread.currentThread().interrupt();
        }
        log.info("用户写入了新的内容");
    }

    public void save() {
        if (isDirty.compareAndSet(true, false)) {
            log.info("系统开始了自动保存...");
            saveToDisk();
            log.info("系统保存内容成功了...");
        }
    }

    public void saveToDisk() {
        try {
            TimeUnit.MILLISECONDS.sleep(10);
        } catch (InterruptedException e) {
            log.info("系统保存内容到磁盘时被中断");
            Thread.currentThread().interrupt();
        }
        log.info("保存内容到系统中...");
    }
}
