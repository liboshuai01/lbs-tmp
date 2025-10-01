package com.liboshuai.demo.prototype.shallow_clone;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

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
