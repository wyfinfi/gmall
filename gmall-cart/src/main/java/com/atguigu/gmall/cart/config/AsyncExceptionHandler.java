package com.atguigu.gmall.cart.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.aop.interceptor.AsyncUncaughtExceptionHandler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.BoundListOperations;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;

/**
 * @Author Administrator
 * @Date 2020/9/8 20:20
 * @Version 1.0
 */
@Component
@Slf4j
public class AsyncExceptionHandler implements AsyncUncaughtExceptionHandler {
    @Autowired
    private StringRedisTemplate redisTemplate;
    private final static String KEY="cart:async:exception";
    @Override
    public void handleUncaughtException(Throwable throwable, Method method, Object... objects) {
        log.error("异步调用发生异常，方法：{}，参数：{},异常信息：{}",method,objects,throwable.getMessage());
        String userId = objects[0].toString();
        BoundListOperations<String, String> listOps = this.redisTemplate.boundListOps(KEY);
        listOps.leftPush(userId);
    }
}
