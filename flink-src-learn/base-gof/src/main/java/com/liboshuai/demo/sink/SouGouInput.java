package com.liboshuai.demo.sink;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SouGouInput {
    private AbstractSkin sink;

    public void display() {
        sink.display();
    }
}
