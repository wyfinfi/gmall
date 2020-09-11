package com.atguigu.gmall.cart.config;

import com.atguigu.gmall.common.utils.RsaUtils;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.properties.ConfigurationProperties;

import javax.annotation.PostConstruct;
import java.io.File;
import java.security.PrivateKey;
import java.security.PublicKey;

/**
 * @Author Administrator
 * @Date 2020/9/7 19:04
 * @Version 1.0
 */
@Data
@Slf4j
@ConfigurationProperties(prefix = "auth.jwt")
public class JwtProperties {
    private String pubkeyPath;
    private String cookieName;
    private String userKey;
    private Integer expire;
    private PublicKey publicKey;


    @PostConstruct
    public void init()  {
        try {
            File pubFile = new File(pubkeyPath);
            this.publicKey = RsaUtils.getPublicKey(pubkeyPath);
        } catch (Exception e) {
            log.error("获取公钥出错");
            e.printStackTrace();
        }
    }
}
