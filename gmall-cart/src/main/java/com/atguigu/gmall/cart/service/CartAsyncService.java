package com.atguigu.gmall.cart.service;

import com.atguigu.gmall.cart.mapper.CartMapper;
import com.atguigu.gmall.cart.entity.Cart;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

/**
 * @Author Administrator
 * @Date 2020/9/8 20:30
 * @Version 1.0
 */
@Service
public class CartAsyncService {
    @Autowired
    private CartMapper cartMapper;

    @Async
    public void updateCartByUserIdAndSkuId(String userId, Cart cart){
        cartMapper.update(cart,new UpdateWrapper<Cart>()
                .eq("user_id",userId)
                .eq("sku_id",cart.getSkuId()));
    }
    @Async
    public void saveCart(String userId,Cart cart){
        this.cartMapper.insert(cart);
    }
    @Async
    public void deleteCartByUserId(String userKey) {
        this.cartMapper.delete(new QueryWrapper<Cart>().eq("user_id",userKey));
    }

    public void deleteCartByUserIdAndskuId(String userId, Long skuId) {
        this.cartMapper.delete(new QueryWrapper<Cart>()
                .eq("user_id",userId)
                .eq("sku_id",skuId));
    }
}
