package com.atguigu.gmall.pms.feign;

import com.atguigu.gmall.sms.intfc.feign.GmallSmsApi;
import org.springframework.cloud.openfeign.FeignClient;

/**
 * @Author Administrator
 * @Date 2020/8/24 18:46
 * @Version 1.0
 */
@FeignClient("sms-service")
public interface GmallSmsClient extends GmallSmsApi {

}
