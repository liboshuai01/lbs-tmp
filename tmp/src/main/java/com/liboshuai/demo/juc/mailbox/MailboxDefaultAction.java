package com.liboshuai.demo.juc.mailbox;


public interface MailboxDefaultAction {
    /**
     * 执行默认动作 (例如: 从 InputGate 读取并处理一条数据)
     * @param controller 用于控制循环流转（例如暂停默认动作）
     */
    void runDefaultAction(Controller controller) throws Exception;

    interface Controller {
        void allFinished(); // 标记任务全部完成
    }
}
