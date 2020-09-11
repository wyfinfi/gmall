package com.atguigu.gmall.item.feign;

import com.atguigu.gmall.pms.api.GmallPmsApi;
import org.springframework.cloud.openfeign.FeignClient;

/**
 * @Author Administrator
 * @Date 2020/9/3 15:08
 * @Version 1.0
 */
@FeignClient("pms-service")
public interface GmallPmsClient extends GmallPmsApi {
}
