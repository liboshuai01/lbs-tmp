package com.liboshuai.demo.service;

import com.liboshuai.demo.Component;
import com.liboshuai.demo.Lazy;
import com.liboshuai.demo.Scope;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Lazy
@Scope("prototype")
@Component
public class UserService {

    private static final Logger LOG = LoggerFactory.getLogger(UserService.class);

    public void test() {
        LOG.info(">>> 调用了test方法 <<<");
    }

}
