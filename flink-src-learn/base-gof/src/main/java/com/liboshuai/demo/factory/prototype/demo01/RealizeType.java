package com.liboshuai.demo.factory.prototype.demo01;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class RealizeType implements Cloneable{

    public RealizeType() {
        log.info("具体原型对象创建完成");
    }

    @Override
    protected RealizeType clone() throws CloneNotSupportedException {
        log.info("具体原型对象复制成功");
        return (RealizeType) super.clone();
    }
}
