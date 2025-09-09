package com.liboshuai.demo.prototype.demo01;

import lombok.extern.slf4j.Slf4j;

import java.util.Date;

@Slf4j
public class Client {
    public static void main(String[] args) throws CloneNotSupportedException {
        // 创建原型羊“多利”
        Date birthday = new Date();
        Sheep dolly = new Sheep("多利", birthday);
        log.info("原型羊：{}", dolly);

        // 克隆一只新羊
        Sheep clonedSheep = dolly.clone();
        clonedSheep.setName("克隆多利");
        log.info("克隆羊：{}", clonedSheep);
        log.info("-------------------------------------");

        // 改变原型羊的生日
        log.info("修改原型羊的生日...");
        birthday.setTime(0L);

        log.info("原型羊修改后：{}", dolly);
        log.info("克隆羊的状态：{}", clonedSheep);
        log.info("问题出现了：克隆羊的生日也被改变了！");
    }
}
