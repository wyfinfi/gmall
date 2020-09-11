package com.atguigu.gmall.search.feign;

import com.atguigu.gmall.pms.api.GmallPmsApi;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.stereotype.Component;

/**
 * @Author Administrator
 * @Date 2020/8/26 20:28
 * @Version 1.0
 */
@FeignClient("pms-service")
public interface GmallPmsClient extends GmallPmsApi {


}
