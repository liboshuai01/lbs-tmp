package com.liboshuai.demo.composite;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class CompanyComponentTest {

    private Department root;
    private Department researchDept;
    private Department salesDept;
    private Employee researcher1;
    private Employee researcher2;
    private Employee salesperson1;

    @BeforeEach
    void setUp() {
        // 创建根部门
        root = new Department("总公司");

        // 创建子部门
        researchDept = new Department("研发部");
        salesDept = new Department("销售部");

        // 创建员工
        researcher1 = new Employee("张三");
        researcher2 = new Employee("李四");
        salesperson1 = new Employee("王五");

        // 构建组织架构
        researchDept.add(researcher1);
        researchDept.add(researcher2);
        salesDept.add(salesperson1);

        root.add(researchDept);
        root.add(salesDept);
        root.add(new Employee("赵六-行政")); // 总公司直属员工
    }

    @Test
    @DisplayName("测试：叶子节点不支持添加操作")
    void testLeafCannotAddComponents() {
        Employee employee = new Employee("普通员工");
        assertThrows(UnsupportedOperationException.class, () -> {
            employee.add(new Employee("试图添加的下属"));
        }, "员工节点不应该能添加子节点");
    }

    @Test
    @DisplayName("测试：叶子节点不支持移除操作")
    void testLeafCannotRemoveComponents() {
        Employee employee = new Employee("普通员工");
        assertThrows(UnsupportedOperationException.class, () -> {
            employee.remove(new Employee("试图移除的下属"));
        }, "员工节点不应该能移除子节点");
    }

    @Test
    @DisplayName("测试：树枝节点可以成功添加子节点")
    void testCompositeCanAddComponents() {
        Department newDept = new Department("市场部");
        Employee marketer = new Employee("周七");
        newDept.add(marketer);

        assertEquals(1, newDept.getChildren().size(), "市场部应该有一个子节点");
        assertTrue(newDept.getChildren().contains(marketer), "市场部应该包含员工'周七'");
    }

    @Test
    @DisplayName("测试：树枝节点可以成功移除子节点")
    void testCompositeCanRemoveComponents() {
        // salesDept 初始有一个员工 salesperson1
        assertEquals(1, salesDept.getChildren().size());

        salesDept.remove(salesperson1);
        assertEquals(0, salesDept.getChildren().size(), "移除后，销售部应该没有子节点");
        assertFalse(salesDept.getChildren().contains(salesperson1), "销售部不应再包含员工'王五'");
    }

    @Test
    @DisplayName("测试：验证组合结构的完整性")
    void testCompositeStructureIntegrity() {
        // 总公司有3个子节点：研发部、销售部、赵六
        assertEquals(3, root.getChildren().size());
        assertTrue(root.getChildren().contains(researchDept));
        assertTrue(root.getChildren().contains(salesDept));

        // 研发部有2个子节点：张三、李四
        assertEquals(2, researchDept.getChildren().size());
        assertTrue(researchDept.getChildren().contains(researcher1));
        assertTrue(researchDept.getChildren().contains(researcher2));
    }

    @Test
    @DisplayName("测试：显示整个组织架构")
    void testDisplayHierarchy() {
        System.out.println("--- 组织架构图 ---");
        root.display(0);
        System.out.println("-----------------");
        // 这个测试主要是为了视觉验证，没有断言
        // 在实际项目中，可以重定向 System.out 并断言输出的字符串内容
    }
}