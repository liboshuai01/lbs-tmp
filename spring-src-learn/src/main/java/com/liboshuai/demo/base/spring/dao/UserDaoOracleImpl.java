package com.liboshuai.demo.base.spring.dao;

import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Repository;

@Repository
@Profile("oracle") // 关键！这个 Bean 只在 "oracle" 环境被激活时才会被创建
public class UserDaoOracleImpl implements UserDao {
    @Override
    public void save(String username) {
        System.out.println(">>> [Oracle] 正在将用户 '" + username + "' 保存到 Oracle 数据库...");
    }
}
