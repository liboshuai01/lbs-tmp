package com.liboshuai.demo.component;

import com.liboshuai.demo.event.UserRegisteredEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Component
public class Component3 {

    private static final Logger log = LoggerFactory.getLogger(Component3.class);

    @EventListener
    public void aaa(UserRegisteredEvent event) {
        log.debug("发送邮件");
    }
}
