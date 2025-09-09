package com.liboshuai.demo.prototype.demo01;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.Date;

@Slf4j
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Sheep implements Cloneable{

    private String name;
    private Date birthday;

    @Override
    protected Sheep clone() throws CloneNotSupportedException {
        return (Sheep) super.clone();
    }
}
