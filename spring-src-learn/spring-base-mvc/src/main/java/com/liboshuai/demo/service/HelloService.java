package com.liboshuai.demo.service;

/**
 * Service层接口，定义核心业务逻辑。
 */
public interface HelloService {
    /**
     * 生成问候消息。
     * @param name 名称
     * @return 包含问候语的字符串
     */
    String getHelloMessage(String name);
}
