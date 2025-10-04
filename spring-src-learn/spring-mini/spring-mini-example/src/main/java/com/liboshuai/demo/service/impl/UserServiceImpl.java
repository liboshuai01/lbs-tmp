package com.liboshuai.demo.service.impl;

import com.liboshuai.demo.Component;
import com.liboshuai.demo.Lazy;
import com.liboshuai.demo.Scope;
import com.liboshuai.demo.service.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Lazy
@Scope("singleton")
@Component
public class UserServiceImpl implements UserService {
    private static final Logger LOG = LoggerFactory.getLogger(UserServiceImpl.class);

    public void test() {
        LOG.debug(">>> UserServiceImpl的test方法被调用了");
    }
}
