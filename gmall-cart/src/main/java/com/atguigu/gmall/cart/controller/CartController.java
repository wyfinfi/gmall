package com.atguigu.gmall.cart.controller;

import com.atguigu.gmall.cart.bean.UserInfo;
import com.atguigu.gmall.cart.interceptor.LoginInterceptor;
import com.atguigu.gmall.cart.entity.Cart;
import com.atguigu.gmall.cart.service.CartService;
import com.atguigu.gmall.common.bean.ResponseVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * @Author Administrator
 * @Date 2020/9/8 16:56
 * @Version 1.0
 */
@Controller
public class CartController {
    @Autowired
    private CartService cartService;


    @GetMapping("check/{userId}")
    @ResponseBody
    public ResponseVo<List<Cart>> queryCheckedCarts(@PathVariable("userId") Long userId){
        List<Cart> carts =this.cartService.queryCheckedCarts(userId);
        return ResponseVo.ok(carts);
    }
    @PostMapping("updateNum")
    @ResponseBody
    public ResponseVo<Object> updateNum(@RequestBody Cart cart){
        this.cartService.updateNUm(cart);
        return ResponseVo.ok();
    }
    @PostMapping("deleteCart")
    @ResponseBody
    public ResponseVo<Object>deleteCartByskuId(@RequestParam("skuId")Long skuId){
        this.cartService.deleteCartByskuId(skuId);
        return ResponseVo.ok();
    }
    @GetMapping("cart.html")
    //@ResponseBody
    public String queryCarts(Model model) {
        List<Cart> carts = this.cartService.queryCarts();
        model.addAttribute("carts", carts);
        return "cart";
    }

    @GetMapping
    public String addCart(Cart cart) {
        if (cart == null || cart.getSkuId() == null) {
            throw new RuntimeException("没有选择添加到购物车的商品");
        }
        this.cartService.addCart(cart);
        return "redirect:http://cart.gmall.com/addCart.html?skuId=" + cart.getSkuId();
    }

    //跳转到添加成功页
    @GetMapping("addCart.html")
    public String addCart(@RequestParam("skuId") Long skuId, Model model) {
        Cart cart = this.cartService.queryCartBySkuId(skuId);
        model.addAttribute("cart", cart);
        return "addCart";
    }

    @GetMapping("test")
    @ResponseBody
    public String test() {
        UserInfo userInfo = LoginInterceptor.getUserInfo();
        System.out.println("userInfo = " + userInfo);
        return "hello cart!";
    }
}
