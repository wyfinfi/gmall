package com.atguigu.gmall.index.config;

import net.bytebuddy.dynamic.scaffold.subclass.ConstructorStrategy;

import java.lang.annotation.*;

/**
 * @Author Administrator
 * @Date 2020/9/3 11:25
 * @Version 1.0
 */
@Target({ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface GmallCache {
    //缓存前缀
    String prefix() default "";
    //缓存过期时间
    int timeout() default 5;
    //防止雪崩设置的随即值范围
     int random() default 5;
     //防止击穿
    String lock() default "lock";

}
