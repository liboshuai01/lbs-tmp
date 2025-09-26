package com.liboshuai.demo.rpc;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.Properties;

@Data
@AllArgsConstructor
public class Configuration {
    private Properties properties;
}
