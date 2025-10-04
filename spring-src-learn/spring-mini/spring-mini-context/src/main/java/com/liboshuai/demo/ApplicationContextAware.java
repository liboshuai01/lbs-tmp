package com.liboshuai.demo;

public interface ApplicationContextAware extends Aware{
    void setApplicationContext(ApplicationContext applicationContext);
}
