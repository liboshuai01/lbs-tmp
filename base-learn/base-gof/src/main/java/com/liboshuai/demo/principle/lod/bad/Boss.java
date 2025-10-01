package com.liboshuai.demo.principle.lod.bad;

import java.util.ArrayList;
import java.util.List;

// 一个简单的课程类
class Course {}

/**
 * 团队负责人
 * 这个类的设计暴露了其内部的数据结构（List）。
 */
class TeamLeader {
    private List<Course> courseList;

    public TeamLeader() {
        // 初始化课程列表
        courseList = new ArrayList<>();
        courseList.add(new Course());
        courseList.add(new Course());
    }

    // 问题所在：这个方法返回了内部的实现细节
    public List<Course> getCourses() {
        return courseList;
    }
}

/**
 * 老板
 * 这个类违反了迪米特法则。
 */
public class Boss {

    // Boss 和 TeamLeader 是直接朋友
    public void checkCourseCount(TeamLeader leader) {
        // leader.getCourses() 返回的 List 对 Boss 来说是“陌生人”
        // Boss 却直接调用了陌生人(List)的方法(size)
        List<Course> courses = leader.getCourses();
        System.out.println("课程的总数是: " + courses.size());
    }

    public static void main(String[] args) {
        Boss boss = new Boss();
        TeamLeader leader = new TeamLeader();
        boss.checkCourseCount(leader);
    }
}