package com.liboshuai.demo.principle.lod.good;

import java.util.ArrayList;
import java.util.List;

// 一个简单的课程类
class Course {}

/**
 * 团队负责人
 * 这个类封装了其内部实现。
 */
class TeamLeader {
    private List<Course> courseList;

    public TeamLeader() {
        courseList = new ArrayList<>();
        courseList.add(new Course());
        courseList.add(new Course());
    }

    /**
     * 关键点：TeamLeader 自己负责统计，只把结果返回给调用者。
     * 它没有暴露任何内部实现细节。
     */
    public int getCourseCount() {
        return this.courseList.size();
    }
}

/**
 * 老板
 * 这个类的设计遵循了迪米特法则。
 */
public class Boss {

    // Boss 只和它的直接朋友 TeamLeader 通信。
    public void checkCourseCount(TeamLeader leader) {
        // 直接调用朋友的方法获取结果，不多问。
        System.out.println("课程的总数是: " + leader.getCourseCount());
    }

    public static void main(String[] args) {
        Boss boss = new Boss();
        TeamLeader leader = new TeamLeader();
        boss.checkCourseCount(leader);
    }
}