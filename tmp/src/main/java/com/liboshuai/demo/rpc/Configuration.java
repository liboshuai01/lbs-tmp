package com.liboshuai.demo.rpc;

import java.util.Properties;

public class Configuration {
    private final Properties properties;

    public Configuration() {
        this.properties = new Properties();
    }

    public void setProperties(String key, String value) {
        properties.setProperty(key,value);
    }

    public String getProperties(String key) {
        return properties.getProperty(key);
    }

}
