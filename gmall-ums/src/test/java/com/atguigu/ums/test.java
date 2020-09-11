package com.atguigu.ums;

import com.atguigu.gmall.common.utils.JwtUtils;
import com.atguigu.gmall.common.utils.RsaUtils;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.aspectj.lang.annotation.Before;
import org.bouncycastle.jcajce.provider.asymmetric.RSA;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import java.security.PrivateKey;
import java.security.PublicKey;
import java.util.HashMap;
import java.util.Map;

/**
 * @Author Administrator
 * @Date 2020/9/7 18:38
 * @Version 1.0
 */
public class test {
    private static final String pubkeyPath="D:\\rsa\\rsa.pub";
    private static final String prikeyPath="D:\\rsa\\rsa.pri";
    private PublicKey publicKey;
    private PrivateKey privateKey;
    @Test
    void testRsa() throws Exception {
        RsaUtils.generateKey(pubkeyPath,prikeyPath, "123456");
    }
    @BeforeEach
    public void testGetRsa() throws Exception {
        this.publicKey = RsaUtils.getPublicKey(pubkeyPath);
        this.privateKey = RsaUtils.getPrivateKey(prikeyPath);
    }
    @Test
    public void  testGenerateToken() throws Exception {
        HashMap<String, Object> map = new HashMap<>();
        map.put("id",11);
        map.put("username","liuyan");
        String token = JwtUtils.generateToken(map, privateKey, 5);
        System.out.println("token = " + token);
    }
    @Test
    public void testParseToken() throws Exception {
        String token="eyJhbGciOiJSUzI1NiJ9.eyJpZCI6MTEsInVzZXJuYW1lIjoibGl1eWFuIiwiZXhwIjoxNTk5NDc2NzEwfQ.S5cyLo6uvcO6Q1bezl4BFse9GuptRJcuJXYDZOkU9wmfeQMuvcVYv_Fz_3XWS8qFc4FnFhMoE7S_DiFedU8AGJY5K7OROKFUcDpGvCPWDep8sc_bv8EB1Ymr6g8Ry8sBbm-aML3XRvdAW1eE8W0wk990dG4TeeHn16fqaGyxAO9oARmhcyvBcvfRKOoEuf-ncJkRzIZNV5J9rQjxz-v2hpiCNaBf4kxCoEUHzIQhuJqFLnwmvZZJ__LaoEmsOja-kMF-5qrxo24yzB946UwHmm2cdj9NhQxDK3_bhLI1wKGtgY2pS5tzQP-w18viefYjPVywJaaDk9ErvTES9bqawQ";
        Map<String, Object> map = JwtUtils.getInfoFromToken(token, publicKey);
        System.out.println(" id= " + map.get("id"));
        System.out.println("username = " + map.get("username"));

    }

}
