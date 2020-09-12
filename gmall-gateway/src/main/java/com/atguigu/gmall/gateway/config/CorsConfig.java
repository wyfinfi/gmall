package com.atguigu.gmall.gateway.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.reactive.CorsWebFilter;
import org.springframework.web.cors.reactive.UrlBasedCorsConfigurationSource;

/**
 * @Author Administrator
 * @Date 2020/8/21 16:41
 * @Version 1.0
 */
@Configuration
public class CorsConfig {
     @Bean
    public CorsWebFilter corsWebFilter(){
         //初始化cors配置对象
         CorsConfiguration configuration = new CorsConfiguration();

         configuration.addAllowedOrigin("http://manager.gmall.com");
         configuration.addAllowedOrigin("http://www.gmall.com");
         configuration.addAllowedOrigin("http://gmall.com");
         configuration.addAllowedOrigin("http://sso.gmall.com");
         configuration.addAllowedOrigin("http://order.gmall.com");
         configuration.addAllowedOrigin("localhost");
         configuration.addAllowedOrigin("127.0.0.1");

         //允许的头信息
         configuration.addAllowedHeader("*");
         //允许的请求方式
         configuration.addAllowedMethod("*");

         configuration.setAllowCredentials(true);

         UrlBasedCorsConfigurationSource corsConfigurationSource = new UrlBasedCorsConfigurationSource();
         corsConfigurationSource.registerCorsConfiguration("/**",configuration);

         return new CorsWebFilter(corsConfigurationSource);
     }
}
