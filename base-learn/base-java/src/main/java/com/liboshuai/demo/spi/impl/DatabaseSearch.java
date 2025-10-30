package com.liboshuai.demo.spi.impl;

import com.liboshuai.demo.spi.Search;

import java.util.Collections;
import java.util.List;

/**
 * 服务实现 A (Service Provider A)
 */
public class DatabaseSearch implements Search {
    @Override
    public List<String> search(String keyword) {
        System.out.println("正在从 [数据库] 中搜索: " + keyword);
        // 模拟返回结果
        return Collections.singletonList("数据库搜索结果");
    }
}