package com.atguigu.gmall.auth.config;

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
    private String priKeyPath;
    private String secret;
    private String cookieName;
    private Integer expire;
    private String unick;

    private PublicKey publicKey;
    private PrivateKey privateKey;

    @PostConstruct
    public void init()  {
        try {
            File pubFile = new File(pubkeyPath);
            File priFile = new File(priKeyPath);
            // 如果公钥或者私钥不存在，重新生成公钥和私钥
            if (!pubFile.exists() || !priFile.exists()) {
                System.out.println("pubFile = " + pubFile);
                System.out.println("priFile = " + priFile);
                RsaUtils.generateKey(pubkeyPath, priKeyPath, secret);
            }

            this.publicKey = RsaUtils.getPublicKey(pubkeyPath);
            this.privateKey = RsaUtils.getPrivateKey(priKeyPath);
        } catch (Exception e) {
            log.error("生成公钥和私钥出错");
            e.printStackTrace();
        }
    }
}
