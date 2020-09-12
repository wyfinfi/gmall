package com.atguigu.gmall.auth.service;

import com.atguigu.gmall.auth.config.JwtProperties;
import com.atguigu.gmall.auth.feign.GmallUmsClient;
import com.atguigu.gmall.common.bean.ResponseVo;
import com.atguigu.gmall.common.utils.CookieUtils;
import com.atguigu.gmall.common.utils.IpUtil;
import com.atguigu.gmall.common.utils.JwtUtils;
import com.atguigu.gmall.ums.entity.UserEntity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.HashMap;

/**
 * @Author Administrator
 * @Date 2020/9/7 19:25
 * @Version 1.0
 */
@Service
@EnableConfigurationProperties(JwtProperties.class)
public class AuthService {
    @Autowired
    private GmallUmsClient umsClient;
    @Autowired
    private JwtProperties jwtProperties;
    public void accredit(String loginName, String password,
                         HttpServletRequest request, HttpServletResponse response) {
        try {
            //完成远程请求获取用户信息
            ResponseVo<UserEntity> userEntityResponseVo = this.umsClient.queryUSer(loginName, password);
            UserEntity userEntity = userEntityResponseVo.getData();
            //判断用户信息是否为空
            System.out.println("userEntity = " + userEntity);
            if(userEntity==null){
                return;
            }
            //把用户id放入载荷
            HashMap<String, Object> map = new HashMap<>();
            map.put("userId",userEntity.getId());
            map.put("username",userEntity.getUsername());
            //为了防止jwt被别人盗取，载荷中加入用户ip地址
            String ipAddress = IpUtil.getIpAddressAtService(request);
            map.put("ip",ipAddress);
            //制作token
            String token = JwtUtils.generateToken(map, jwtProperties.getPrivateKey(), jwtProperties.getExpire());
            System.out.println("token = " + token);
            //把jwt放入cookie中
            CookieUtils.setCookie(request,response,jwtProperties.getCookieName()
                    ,token,jwtProperties.getExpire()*60);
        } catch (Exception e) {
           throw new RuntimeException("用户名或者密码出错");
        }
    }
}
