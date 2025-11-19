package com.liboshuai.demo.juc;


import java.util.List;

public interface TaskMailbox {
    /** 邮箱是否打开 */
    boolean hasMail();

    /** 尝试获取邮件 (非阻塞) */
    Runnable tryTake(int priority);

    /** 获取邮件 (阻塞) */
    Runnable take(int priority) throws InterruptedException;

    /** 投递邮件 */
    void put(Runnable mail);

    /** 关闭并返回剩余邮件（用于清理） */
    List<Runnable> drain();

    void quiesce(); // 停止接收新邮件
}
