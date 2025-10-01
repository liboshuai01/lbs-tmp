package com.liboshuai.demo.dao.impl;

import com.liboshuai.demo.dao.UserDao;
import org.springframework.stereotype.Repository;

@Repository // 将这个类标识为一个Spring Bean，ID默认为类名首字母小写(userDaoImpl)
public class UserDaoImpl implements UserDao {
    @Override
    public void saveUser(String username) {
        // 模拟数据库保存操作
        System.out.println("成功保存用户到数据库: " + username);
    }
}
