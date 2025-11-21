package com.liboshuai.slr.engine.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Rule {
    private Long id;
    private String ruleName;
    private String script; // Aviator 表达式: count > 10
    private int threshold;
}