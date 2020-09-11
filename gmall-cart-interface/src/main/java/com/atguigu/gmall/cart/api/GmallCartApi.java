package com.atguigu.gmall.cart.api;

import com.atguigu.gmall.cart.entity.Cart;
import com.atguigu.gmall.common.bean.ResponseVo;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.List;

/**
 * @Author Administrator
 * @Date 2020/9/10 14:48
 * @Version 1.0
 */
public interface GmallCartApi {
    @GetMapping("check/{userId}")
    public ResponseVo<List<Cart>> queryCheckedCarts(@PathVariable("userId") Long userId);
}
