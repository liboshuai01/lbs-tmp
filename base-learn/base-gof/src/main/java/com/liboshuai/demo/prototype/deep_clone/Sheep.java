package com.liboshuai.demo.prototype.deep_clone;

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
        Sheep clonedSheep = (Sheep) super.clone();
        clonedSheep.birthday = (Date) this.birthday.clone();
        return clonedSheep;
    }
}
