package com.liboshuai.demo.service.impl;

import com.liboshuai.demo.Autowired;
import com.liboshuai.demo.Component;
import com.liboshuai.demo.Scope;
import com.liboshuai.demo.service.OrderService;
import com.liboshuai.demo.service.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Scope("prototype")
@Component
public class OrderServiceImpl implements OrderService {

    private static final Logger LOG = LoggerFactory.getLogger(OrderServiceImpl.class);

    @Autowired
    private UserService userService;

    @Override
    public void test() {
        LOG.debug(">>> OrderServiceImpl的test方法被调用了");
        userService.test();
    }
}
