package com.atguigu.gmall.auth.feign;

import com.atguigu.gmall.ums.api.GmallUmsApi;
import org.springframework.cloud.openfeign.FeignClient;

/**
 * @Author Administrator
 * @Date 2020/9/7 19:46
 * @Version 1.0
 */
@FeignClient("ums-service")
public interface GmallUmsClient extends GmallUmsApi {
}
