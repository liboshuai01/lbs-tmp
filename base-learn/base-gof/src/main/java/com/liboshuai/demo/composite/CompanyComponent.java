package com.liboshuai.demo.composite;

import lombok.Getter;

/**
 * 抽象构件 (Component)
 * 定义公司组织单元的通用接口，无论是部门还是员工。
 */
@Getter
public abstract class CompanyComponent {

    protected String name;

    public CompanyComponent(String name) {
        this.name = name;
    }

    /**
     * 增加子节点（部门或员工）
     * @param component 子节点
     */
    public void add(CompanyComponent component) {
        throw new UnsupportedOperationException("不支持此操作");
    }

    /**
     * 移除子节点
     * @param component 子节点
     */
    public void remove(CompanyComponent component) {
        throw new UnsupportedOperationException("不支持此操作");
    }

    /**
     * 显示组织架构（业务方法）
     * @param depth 层级深度，用于格式化输出
     */
    public abstract void display(int depth);
}