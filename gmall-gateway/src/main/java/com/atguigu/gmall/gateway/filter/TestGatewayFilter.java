package com.atguigu.gmall.gateway.filter;

import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

/**
 * @Author Administrator
 * @Date 2020/9/7 20:24
 * @Version 1.0
 */
@Component
public class TestGatewayFilter implements GlobalFilter , Ordered {
    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        //System.out.println("无需配置，拦截所有经过网关的请求");
        return chain.filter(exchange);
    }

    @Override
    public int getOrder() {
        return 10;
    }
}
