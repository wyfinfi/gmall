package com.atguigu.gmall.cart.feign;

import com.atguigu.gmall.sms.intfc.feign.GmallSmsApi;
import org.springframework.cloud.openfeign.FeignClient;

/**
 * @Author Administrator
 * @Date 2020/9/3 15:10
 * @Version 1.0
 */
@FeignClient("sms-service")
public interface GmallSmsClient extends GmallSmsApi {
}
