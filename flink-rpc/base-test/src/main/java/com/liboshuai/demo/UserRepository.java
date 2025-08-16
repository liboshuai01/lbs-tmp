package com.liboshuai.demo;

// 这是一个外部依赖，可能实现很复杂（比如访问数据库）
public interface UserRepository {
    String findUsernameById(String userId);
}