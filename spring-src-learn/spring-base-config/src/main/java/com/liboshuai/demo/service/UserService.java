package com.liboshuai.demo.service;

import com.liboshuai.demo.dao.UserDao;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service // 将这个类标识为一个Spring Bean
public class UserService {

    @Autowired // 告诉 Spring 自动注入一个 UserDao 类型得 Bean
    private UserDao userDao;

    // 注意：使用 @Autowired 字段注入后，我们不再需要 setter 方法了！

    public void addUser(String username) {
        System.out.println("业务层调用DAO层...");
        userDao.saveUser(username);
    }
}
