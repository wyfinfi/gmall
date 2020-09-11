package com.atguigu.gmall.order.feign;

import com.atguigu.gmall.cart.api.GmallCartApi;
import org.springframework.cloud.openfeign.FeignClient;

/**
 * @Author Administrator
 * @Date 2020/9/10 15:03
 * @Version 1.0
 */
@FeignClient("cart-service")
public interface GmallCartClient extends GmallCartApi {
}
