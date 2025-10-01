package com.liboshuai.demo.composite;


import java.util.ArrayList;
import java.util.List;

/**
 * 树枝构件 (Composite)
 * 代表部门，可以包含子部门和员工。
 */
public class Department extends CompanyComponent {

    // 存储子节点的容器
    private final List<CompanyComponent> children = new ArrayList<>();

    public Department(String name) {
        super(name);
    }

    @Override
    public void add(CompanyComponent component) {
        children.add(component);
    }

    @Override
    public void remove(CompanyComponent component) {
        children.remove(component);
    }

    // 增加一个 getter 方便测试
    public List<CompanyComponent> getChildren() {
        return children;
    }

    @Override
    public void display(int depth) {
        // 打印部门名称
        for (int i = 0; i < depth; i++) {
            System.out.print("-");
        }
        System.out.println("部门: " + name);

        // 递归显示子节点
        for (CompanyComponent component : children) {
            component.display(depth + 2);
        }
    }
}