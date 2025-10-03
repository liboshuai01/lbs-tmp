package com.liboshuai.demo.dao.impl;

import com.liboshuai.demo.dao.UserDao;

public class UserDaoImpl implements UserDao {
    @Override
    public void saveUser(String username) {
        // 模拟数据库保存操作
        System.out.println("成功保存用户到数据库: " + username);
    }
}
