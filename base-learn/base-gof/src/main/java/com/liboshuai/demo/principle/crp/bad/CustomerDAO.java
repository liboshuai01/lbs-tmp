package com.liboshuai.demo.principle.crp.bad;

/**
 * 一个数据库工具类，提供了连接功能。
 */
class DBUtil {
    public void getConnection() {
        System.out.println("获取MySQL数据库连接...");
    }
}

/**
 * CustomerDAO 通过继承来复用 DBUtil 的功能。
 * 这是一个 "白箱" 复用，因为子类对父类的实现细节是可见的。
 * 这种设计耦合度高，不灵活。
 */
public class CustomerDAO extends DBUtil {

    public void addCustomer() {
        // 直接调用继承来的方法
        super.getConnection();
        System.out.println("正在添加客户信息...");
    }

    public static void main(String[] args) {
        CustomerDAO dao = new CustomerDAO();
        dao.addCustomer();
    }
}