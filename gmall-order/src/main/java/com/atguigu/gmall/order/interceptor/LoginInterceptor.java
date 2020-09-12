package com.atguigu.gmall.order.interceptor;

import com.atguigu.gmall.order.bean.UserInfo;

import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;


/**
 * @Author Administrator
 * @Date 2020/9/8 16:51
 * @Version 1.0
 */
@Component
public class LoginInterceptor implements HandlerInterceptor {
    //声明线程局部bial
    private static final ThreadLocal<UserInfo> THREAD_LOCAL=new ThreadLocal<>();
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        UserInfo userInfo = new UserInfo();
        // 获取请求头信息
        String userId1= request.getHeader("userId");
        Long userId  = Long.valueOf(userId1);
        userInfo.setUserId(userId);
        //把信息放入局部变量
        THREAD_LOCAL.set(userInfo);
        return true;
    }
    public static UserInfo getUserInfo(){
        return THREAD_LOCAL.get();
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {
        // 调用删除方法，是必须选项。因为使用的是tomcat线程池，请求结束后，线程不会结束。
        // 如果不手动删除线程变量，可能会导致内存泄漏
        THREAD_LOCAL.remove();
    }
}
