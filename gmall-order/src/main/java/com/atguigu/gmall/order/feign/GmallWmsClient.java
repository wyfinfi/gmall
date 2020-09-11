package com.atguigu.gmall.order.feign;

import com.atguigu.gmall.wms.api.GmallWmsApi;
import org.springframework.cloud.openfeign.FeignClient;

/**
 * @Author Administrator
 * @Date 2020/9/3 15:09
 * @Version 1.0
 */
@FeignClient("wms-service")
public interface GmallWmsClient extends GmallWmsApi {
}
