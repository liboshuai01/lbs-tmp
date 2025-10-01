package com.liboshuai.demo.base.spring.service;

import com.liboshuai.demo.base.spring.dao.UserDao;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service // 声明这是一个 Spring 管理的业务层 Bean
public class UserService {
    // 1. 依赖的是 UserDao 接口，而不是任何具体的实现类
    private final UserDao userDao;

    // 2. 使用构造函数进行依赖注入，这是 Spring 推荐的方式
    @Autowired
    public UserService(UserDao userDao) {
        this.userDao = userDao;
        System.out.println("UserService 已创建，注入的 DAO 实现是: " + userDao.getClass().getName());
    }

    public void registerUser(String username) {
        System.out.println("开始处理用户 '" + username + "' 的注册业务...");
        // 3. 直接调用接口方法，完全不关心底层是 MySQL 还是 Oracle
        this.userDao.save(username);
        System.out.println("用户 '" + username + "' 注册业务处理完成。");
    }
}
