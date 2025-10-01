package com.liboshuai.demo.composite;

/**
 * 叶子构件 (Leaf)
 * 代表员工，是组织结构中的叶子节点，没有下属。
 */
public class Employee extends CompanyComponent {

    public Employee(String name) {
        super(name);
    }

    @Override
    public void display(int depth) {
        // 通过 depth 控制缩进，打印层级关系
        for (int i = 0; i < depth; i++) {
            System.out.print("-");
        }
        System.out.println("员工: " + name);
    }
}