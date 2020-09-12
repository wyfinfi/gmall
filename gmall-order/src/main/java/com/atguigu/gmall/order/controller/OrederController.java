package com.atguigu.gmall.order.controller;

import com.atguigu.gmall.order.service.OrderService;
import com.atguigu.gmall.order.vo.OrderConfirmVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;


/**
 * @Author Administrator
 * @Date 2020/9/10 14:53
 * @Version 1.0
 */
@Controller
public class OrederController {
    @Autowired
    private OrderService orderService;
    @GetMapping("confirm")
    public String confirm(Model model){
        OrderConfirmVo orderConfirmVo = this.orderService.confirm();
        model.addAttribute("confirmVo", orderConfirmVo);
        return "trade";
    }
}
