package com.liboshuai.demo.spi.impl;

import com.liboshuai.demo.spi.Search;

import java.util.Collections;
import java.util.List;

/**
 * 服务实现 B (Service Provider B)
 */
public class FileSearch implements Search {
    @Override
    public List<String> search(String keyword) {
        System.out.println("正在从 [文件系统] 中搜索: " + keyword);
        // 模拟返回结果
        return Collections.singletonList("文件搜索结果");
    }
}
