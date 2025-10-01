package com.liboshuai.demo.principle.crp.good;

/**
 * 数据库连接类保持独立。
 * （更进一步的设计，可以将其抽象为接口，以符合依赖倒转原则）
 */
class DBUtil {
    public void getConnection() {
        System.out.println("获取MySQL数据库连接...");
    }
}

/**
 * CustomerDAO 通过组合来复用 DBUtil 的功能。
 * 这是一个 "黑箱" 复用，耦合度低，非常灵活。
 */
public class CustomerDAO {
    // 步骤 1: "has-a" 关系，将 DBUtil 作为成员变量
    private DBUtil dbUtil;

    // 步骤 2: 通过构造函数注入依赖
    public CustomerDAO(DBUtil dbUtil) {
        this.dbUtil = dbUtil;
    }

    public void addCustomer() {
        // 步骤 3: 通过委托来使用其功能
        this.dbUtil.getConnection();
        System.out.println("正在添加客户信息...");
    }

    public static void main(String[] args) {
        // 在使用时，将依赖的对象组合进来
        DBUtil util = new DBUtil();
        CustomerDAO dao = new CustomerDAO(util);
        dao.addCustomer();
    }
}