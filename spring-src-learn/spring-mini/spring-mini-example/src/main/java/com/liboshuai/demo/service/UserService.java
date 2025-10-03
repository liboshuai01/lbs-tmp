package com.liboshuai.demo.service;

import com.liboshuai.demo.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Component
public class UserService {

    private static final Logger LOG = LoggerFactory.getLogger(UserService.class);

    public void test() {
        LOG.info(">>> 调用了test方法 <<<");
    }

}
