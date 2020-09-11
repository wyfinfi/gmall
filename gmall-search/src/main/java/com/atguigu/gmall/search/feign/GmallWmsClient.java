package com.atguigu.gmall.search.feign;

import com.atguigu.gmall.wms.api.GmallWmsApi;
import org.springframework.cloud.openfeign.FeignClient;

/**
 * @Author Administrator
 * @Date 2020/8/26 20:30
 * @Version 1.0
 */
@FeignClient("wms-service")
public interface GmallWmsClient extends GmallWmsApi {
}
