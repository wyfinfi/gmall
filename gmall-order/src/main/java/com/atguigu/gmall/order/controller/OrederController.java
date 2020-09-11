package com.atguigu.gmall.order.controller;

import com.atguigu.gmall.common.bean.ResponseVo;
import com.atguigu.gmall.order.service.OrderService;
import com.atguigu.gmall.order.vo.OrderConfirmVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @Author Administrator
 * @Date 2020/9/10 14:53
 * @Version 1.0
 */
@RestController
public class OrederController {
    @Autowired
    private OrderService orderService;
    @GetMapping("confirm")
    public ResponseVo<OrderConfirmVo> confirm(){
        OrderConfirmVo orderConfirmVo = this.orderService.confirm();
        return ResponseVo.ok(orderConfirmVo);
    }
}
