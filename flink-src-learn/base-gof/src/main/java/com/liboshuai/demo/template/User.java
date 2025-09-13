package com.liboshuai.demo.template;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class User {
    private final String username;
    private final int age;
}
