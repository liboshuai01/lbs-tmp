package com.liboshuai.demo.demo01;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SouGouInput {
    private AbstractSkin sink;

    public String display() {
        int display = sink.display();
        if (display == 0) {
            return "默认皮肤";
        } else if (display == 1) {
            return "黑马皮肤";
        }
        return null;
    }
}
