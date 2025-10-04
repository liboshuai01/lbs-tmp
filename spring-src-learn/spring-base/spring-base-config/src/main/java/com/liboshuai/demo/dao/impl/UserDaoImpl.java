package com.liboshuai.demo.dao.impl;

import com.liboshuai.demo.dao.UserDao;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.context.annotation.Lazy;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Repository;

import javax.annotation.PostConstruct;

//@Lazy
@Scope("singleton")
@Repository // 将这个类标识为一个Spring Bean，ID默认为类名首字母小写(userDaoImpl)
public class UserDaoImpl implements UserDao {

    @PostConstruct
    @Override
    public void saveUser(String username) {
        // 模拟数据库保存操作
        System.out.println("成功保存用户到数据库: " + username);
    }
}
