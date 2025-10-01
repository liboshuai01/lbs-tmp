package com.liboshuai.demo.base.spring.dao;

import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Repository;

@Repository // 声明这是一个 Spring 管理的 Bean，通常用于 DAO 层
@Profile("mysql") // 关键！这个 Bean 只在 "mysql" 环境被激活时才会被创建
public class UserDaoMysqlImpl implements UserDao {
    @Override
    public void save(String username) {
        System.out.println(">>> [MySQL] 正在将用户 '" + username + "' 保存到 MySQL 数据库...");
    }
}
