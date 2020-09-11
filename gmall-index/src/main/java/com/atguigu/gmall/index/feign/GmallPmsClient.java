package com.atguigu.gmall.index.feign;

import com.atguigu.gmall.pms.api.GmallPmsApi;
import org.springframework.cloud.openfeign.FeignClient;

/**
 * @Author Administrator
 * @Date 2020/9/1 20:46
 * @Version 1.0
 */
@FeignClient("pms-service")
public interface GmallPmsClient extends GmallPmsApi {
}
