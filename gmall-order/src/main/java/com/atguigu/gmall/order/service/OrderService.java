package com.atguigu.gmall.order.service;

import com.atguigu.gmall.cart.entity.Cart;
import com.atguigu.gmall.common.bean.ResponseVo;
import com.atguigu.gmall.order.bean.UserInfo;
import com.atguigu.gmall.order.feign.*;
import com.atguigu.gmall.order.interceptor.LoginInterceptor;
import com.atguigu.gmall.order.vo.OrderConfirmVo;
import com.atguigu.gmall.order.vo.OrderItemVo;
import com.atguigu.gmall.pms.entity.SkuAttrValueEntity;
import com.atguigu.gmall.pms.entity.SkuEntity;
import com.atguigu.gmall.sms.intfc.vo.ItemSaleVo;
import org.aspectj.weaver.ast.Var;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.math.BigDecimal;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.stream.Collectors;

/**
 * @Author Administrator
 * @Date 2020/9/10 14:54
 * @Version 1.0
 */
@Service
public class OrderService {
    @Autowired
    private GmallPmsClient pmsClient;
    @Autowired
    private GmallSmsClient smsClient;
    @Autowired
    private  GmallWmsClient wmsClient;
    @Autowired
    private GmallUmsClient umsClient;
    @Autowired
    private GmallCartClient cartClient;
    @Autowired
    private StringRedisTemplate redisTemplate;
    @Autowired
    private ThreadPoolExecutor threadPoolExecutor;

    private static final String KEY_PREFIX = "order:token:";

    //使用异步bianp
    public OrderConfirmVo confirm() {
        OrderConfirmVo orderConfirmVo = new OrderConfirmVo();
        UserInfo userInfo = LoginInterceptor.getUserInfo();
        Long userId = userInfo.getUserId();
        CompletableFuture<List<Cart>> cartCompletableFuture = CompletableFuture.supplyAsync(() -> {
            ResponseVo<List<Cart>> listResponseVo = this.cartClient.queryCheckedCarts(userId);
            List<Cart> carts = listResponseVo.getData();
            if(CollectionUtils.isEmpty(carts)){
                throw new RuntimeException("没有选中的购物车信息");
            }
            return carts;
        }, threadPoolExecutor);
        CompletableFuture<Void> itemCompletableFuture = cartCompletableFuture.thenAcceptAsync(carts -> {
           List<OrderItemVo> items = carts.stream().map(cart -> {
                OrderItemVo orderItemVo = new OrderItemVo();
                orderItemVo.setSkuId(cart.getSkuId());
                orderItemVo.setCount(cart.getCount().intValue());
                //根据skuId查询sku
               CompletableFuture<Void> skuCompletableFuture = CompletableFuture.runAsync(() -> {
                   ResponseVo<SkuEntity> skuEntityResponseVo = this.pmsClient.querySkuById(cart.getSkuId());
                   SkuEntity skuEntity = skuEntityResponseVo.getData();
                   orderItemVo.setTitle(skuEntity.getTitle());
                   orderItemVo.setPrice(skuEntity.getPrice());
                   orderItemVo.setDefaultImage(skuEntity.getDefaultImage());
                   orderItemVo.setWeight(new BigDecimal(skuEntity.getWeight()));
               }, threadPoolExecutor);
               //查询销售属性
               CompletableFuture<Void> skuAttrValueCompletableFuture = CompletableFuture.runAsync(() -> {
                   ResponseVo<List<SkuAttrValueEntity>> skuAttrValueResponseVo =
                           this.pmsClient.querySkuAttrValuesBySkuId(cart.getSkuId());
                   List<SkuAttrValueEntity> skuAttrValueEntities =
                           skuAttrValueResponseVo.getData();
                   orderItemVo.setSaleAttrs(skuAttrValueEntities);
               },threadPoolExecutor);
               //根据skuId查询营销信息


               CompletableFuture<Void> saleCompletableFuture = CompletableFuture.runAsync(() -> {
                   ResponseVo<List<ItemSaleVo>> ItemSaleVos =
                           this.smsClient.querySalesBySkuId(cart.getSkuId());
                   List<ItemSaleVo> itemSaleVoList = ItemSaleVos.getData();
                   orderItemVo.setSales(itemSaleVoList);
               }, threadPoolExecutor);
                return orderItemVo;
           }).collect(Collectors.toList());
        }, threadPoolExecutor);
        return null;
    }
}
