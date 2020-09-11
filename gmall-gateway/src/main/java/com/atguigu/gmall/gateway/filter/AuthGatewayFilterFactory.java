package com.atguigu.gmall.gateway.filter;

import com.alibaba.nacos.client.utils.IPUtil;
import com.atguigu.gmall.common.utils.IpUtil;
import com.atguigu.gmall.common.utils.JwtUtils;
import com.atguigu.gmall.gateway.config.JwtProperties;
import lombok.Data;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.http.HttpCookie;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import org.springframework.util.MultiValueMap;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * @Author Administrator
 * @Date 2020/9/7 20:31
 * @Version 1.0
 */
@Component
@EnableConfigurationProperties(JwtProperties.class)
public class AuthGatewayFilterFactory extends AbstractGatewayFilterFactory<AuthGatewayFilterFactory.PathConfig> {
   @Autowired
   private JwtProperties jwtProperties;

    public AuthGatewayFilterFactory() {
        super(PathConfig.class);
    }

    @Override
    public ShortcutType shortcutType() {
        return ShortcutType.GATHER_LIST;
    }

    @Override
    public List<String> shortcutFieldOrder() {
        return Arrays.asList("paths");
    }

    @Data
    public static class PathConfig{
        private List<String> paths;
    }
    @Override
    public GatewayFilter apply(PathConfig config) {
        return new GatewayFilter() {
            @Override
            public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
                System.out.println("自定义过滤器");
                ServerHttpRequest request = exchange.getRequest();
                ServerHttpResponse response = exchange.getResponse();
                //判断当前请求路径在不在拦截路径中，不在放行
                List<String> paths = config.paths;
                String requestpath = request.getURI().getPath();
                if(CollectionUtils.isEmpty(paths)||!paths.stream().anyMatch(path->path.startsWith(requestpath))){
                    return chain.filter(exchange);
                }
                //获取token信息异步请求头中获取，同步cookie中获取
                String token = request.getHeaders().getFirst("token");
                if(StringUtils.isEmpty(token)){
                    MultiValueMap<String, HttpCookie> cookies = request.getCookies();
                    if(!CollectionUtils.isEmpty(cookies)&&cookies.containsKey(jwtProperties.getCookieName())){
                        token=cookies.getFirst(jwtProperties.getCookieName()).getValue();
                    }
                }
                //判断token是否为空，为空直接拦截
                if(StringUtils.isEmpty(token)){
                    response.setStatusCode(HttpStatus.SEE_OTHER);
                    response.getHeaders().set(HttpHeaders.LOCATION,
                            "http://sso.gmall.com/toLogin.html?returnUrl=" + request.getURI());
                    response.setComplete();
                }
                try {
                    //解析jwt有异常直接拦截
                    Map<String, Object> map = JwtUtils.getInfoFromToken(token, jwtProperties.getPublicKey());
                    //判断ip
                    String ip = map.get("ip").toString();
                    String curIp = IpUtil.getIpAddressAtGateway(request);
                    if(!StringUtils.equals(ip,curIp)){
                        response.setStatusCode(HttpStatus.SEE_OTHER);
                        response.getHeaders().set(HttpHeaders.LOCATION,
                                "http://sso.gmall.com/toLogin.html?returnUrl=" + request.getURI());
                        response.setComplete();
                    }
                    //传递登录信息给后续的服务，不需要再次解析jwt
                    request.mutate().header("userId", map.get("userId").toString()).build();
                    //将新的request对象转换成exchange对象
                    exchange.mutate().request(request).build();
                } catch (Exception e) {
                    e.printStackTrace();
                    response.setStatusCode(HttpStatus.SEE_OTHER);
                    response.getHeaders().set(HttpHeaders.LOCATION,
                            "http://sso.gmall.com/toLogin.html?returnUrl=" + request.getURI());
                    response.setComplete();
                }
                return chain.filter(exchange);
            }
        };
    }
}
