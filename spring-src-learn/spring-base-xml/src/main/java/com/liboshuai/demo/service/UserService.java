package com.liboshuai.demo.service;

import com.liboshuai.demo.dao.UserDao;

public class UserService {

    private UserDao userDao;

    // Spring容器将通过这个方法注入一个UserDao的实例
    public void setUserDao(UserDao userDao) {
        this.userDao = userDao;
    }

    public void addUser(String username) {
        System.out.println("业务层调用DAO层...");
        userDao.saveUser(username);
    }
}
