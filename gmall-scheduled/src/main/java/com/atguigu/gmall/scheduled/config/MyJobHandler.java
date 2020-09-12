package com.atguigu.gmall.scheduled.config;

import com.xxl.job.core.biz.model.ReturnT;
import com.xxl.job.core.handler.annotation.XxlJob;
import com.xxl.job.core.log.XxlJobLogger;
import org.springframework.stereotype.Component;

/**
 * @Author Administrator
 * @Date 2020/9/9 21:06
 * @Version 1.0
 */
@Component
public class MyJobHandler {
    @XxlJob("myJobHandler")
    public ReturnT<String> excute(String param){
        XxlJobLogger.log("使用XxlJobLogger打印执行日志，O(∩_∩)O");
        System.out.println("我的任务执行了：" + param + "，线程：" + Thread.currentThread().getName());
        return ReturnT.SUCCESS;
    }
}
