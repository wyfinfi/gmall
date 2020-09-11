package com.atguigu.gmall.ums.api;

import com.atguigu.gmall.common.bean.ResponseVo;
import com.atguigu.gmall.ums.entity.UserAddressEntity;
import com.atguigu.gmall.ums.entity.UserEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

/**
 * @Author Administrator
 * @Date 2020/9/7 18:03
 * @Version 1.0
 */

public interface GmallUmsApi  {

    @GetMapping("ums/user/{id}")
    public ResponseVo<UserEntity> queryUserById(@PathVariable("id") Long id);

    @GetMapping("ums/useraddress/user/{userId}")
    public ResponseVo<List<UserAddressEntity>> queryAddressByUserId(@RequestParam("userId") Long userId);

    @GetMapping("ums/user/query")
    public ResponseVo<UserEntity> queryUSer(
            @RequestParam("loginName") String loginName,
            @RequestParam("password") String password
    );
}
