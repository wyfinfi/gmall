package com.atguigu.gmall.cart.service;

import com.alibaba.fastjson.JSON;
import com.atguigu.gmall.cart.bean.UserInfo;
import com.atguigu.gmall.cart.feign.GmallPmsClient;
import com.atguigu.gmall.cart.feign.GmallSmsClient;
import com.atguigu.gmall.cart.feign.GmallWmsClient;
import com.atguigu.gmall.cart.interceptor.LoginInterceptor;
import com.atguigu.gmall.cart.entity.Cart;
import com.atguigu.gmall.common.bean.ResponseVo;
import com.atguigu.gmall.pms.entity.SkuAttrValueEntity;
import com.atguigu.gmall.pms.entity.SkuEntity;
import com.atguigu.gmall.sms.intfc.vo.ItemSaleVo;
import com.atguigu.gmall.wms.entity.WareSkuEntity;
import io.swagger.annotations.Api;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.BoundHashOperations;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @Author Administrator
 * @Date 2020/9/8 18:39
 * @Version 1.0
 */
@Service
@Api(tags = "购物车")
public class CartService {
    @Autowired
    private GmallPmsClient pmsClient;
    @Autowired
    private GmallSmsClient smsClient;
    @Autowired
    private GmallWmsClient wmsClient;
    @Autowired
    private StringRedisTemplate redisTemplate;
    @Autowired
    private CartAsyncService cartAsyncService;

    private static final String KEY_PREFIX = "cart:info:";
    private static final String PRICE_PREFIX = "cart:price:";

    public void addCart(Cart cart) {
        //获取登录信息
        String userId = getUserId();
        String key = KEY_PREFIX + userId;
        //获取redis该用户的购物车
        BoundHashOperations<String, Object, Object> hashOps = this.redisTemplate.boundHashOps(key);
        //判断该用户的购物车信息是否包含了该商品
        String skuId = cart.getSkuId().toString();
        BigDecimal count = cart.getCount();//用户添加购物的商品
        if (hashOps.hasKey(skuId)) {
            //包含就更新数量
            String cartJson = hashOps.get(skuId).toString();
            cart = JSON.parseObject(cartJson, Cart.class);

            cart.setCount(cart.getCount().add(count));
            this.cartAsyncService.updateCartByUserIdAndSkuId(userId, cart);
        } else {
            //不包含，给用户新增购物车
            cart.setUserId(userId);
            //根据skuId查询sku
            ResponseVo<SkuEntity> skuEntityResponseVo =
                    this.pmsClient.querySkuById(cart.getSkuId());
            SkuEntity skuEntity = skuEntityResponseVo.getData();
            if (skuEntity != null) {
                cart.setTitle(skuEntity.getTitle());
                cart.setPrice(skuEntity.getPrice());
                cart.setDefaultImage(skuEntity.getDefaultImage());
            }
            // 根据skuId查询销售属性
            ResponseVo<List<SkuAttrValueEntity>> skuattrValueResponseVo = this.pmsClient.querySkuAttrValuesBySkuId(cart.getSkuId());
            List<SkuAttrValueEntity> skuAttrValueEntities = skuattrValueResponseVo.getData();
            cart.setSaleAttrs(JSON.toJSONString(skuAttrValueEntities));
            //根据skuId查询营销信息
            ResponseVo<List<ItemSaleVo>> itemResponseVo =
                    this.smsClient.querySalesBySkuId(cart.getSkuId());
            List<ItemSaleVo> itemSaleVos = itemResponseVo.getData();
            cart.setSales(JSON.toJSONString(itemSaleVos));
            //根据skuId查询库存信息
            ResponseVo<List<WareSkuEntity>> wareResponseVo =
                    this.wmsClient.queryWareSkuBySkuId(cart.getSkuId());
            List<WareSkuEntity> wareSkuEntities = wareResponseVo.getData();
            if (!CollectionUtils.isEmpty(wareSkuEntities)) {
                cart.setStore(wareSkuEntities.stream().anyMatch(wareSkuEntity ->
                        wareSkuEntity.getStock() - wareSkuEntity.getStockLocked() > 0));
            }
            //商品加入购物车时默认为选中状态
            cart.setCheck(true);
            //this.cartMapper.insert(cart);
            this.cartAsyncService.saveCart(userId, cart);
            //缓存实时价格
            this.redisTemplate.opsForValue().set(PRICE_PREFIX+skuId,skuEntity.getPrice().toString());
        }
        hashOps.put(skuId, JSON.toJSONString(cart));
    }

    public Cart queryCartBySkuId(Long skuId) {
        //获取登录信息
        String userId = getUserId();
        String key = KEY_PREFIX + userId;

        // 获取redis中该用户的购物车
        BoundHashOperations<String, Object, Object> hashOps = this.redisTemplate.boundHashOps(key);
        if (hashOps.hasKey(skuId.toString())) {
            String cartJson = hashOps.get(skuId.toString()).toString();
            return JSON.parseObject(cartJson, Cart.class);
        }
        throw new RuntimeException("您的购物车中没有该商品记录！");
    }

    private String getUserId() {
        UserInfo userInfo = LoginInterceptor.getUserInfo();
        if (userInfo.getUserId() != null) {
            return userInfo.getUserId().toString();
        }
        //用户为登录以userKey作为key
        return userInfo.getUserKey();
    }

    public List<Cart> queryCarts() {
        UserInfo userInfo = LoginInterceptor.getUserInfo();
        String userKey = userInfo.getUserKey();
        //查询为未登陆的购物车
        String unloginKey = KEY_PREFIX + userKey;
        BoundHashOperations<String, Object, Object> hashOps =
                this.redisTemplate.boundHashOps(unloginKey);
        //获取未登录的购物车集合
        List<Object> cartJsons = hashOps.values();
        List<Cart> unlonginCarts  = null;
        if(!CollectionUtils.isEmpty(cartJsons)){
           unlonginCarts= cartJsons.stream().map(cartJson->{
               Cart cart = JSON.parseObject(cartJson.toString(), Cart.class);
               String currentPrice = this.redisTemplate.opsForValue().get(PRICE_PREFIX + cart.getSkuId());
               cart.setCurrentPrice(new BigDecimal(currentPrice));
                return cart;
            }).collect(Collectors.toList());
        }
        //判断是否登录，未登录直接返回
        Long userId = userInfo.getUserId();
        if(userId == null){
            return unlonginCarts;
        }
        //合并购物车
        String loginKey = KEY_PREFIX +userId;
        //获取登录状态的购物车
        BoundHashOperations<String, Object, Object> loginHashOps =
                this.redisTemplate.boundHashOps(loginKey);
        // 判断是否存在未登录的购物车，有则遍历未登录的购物车合并到已登录的购物车中去
        if(!CollectionUtils.isEmpty(unlonginCarts)){
            unlonginCarts.forEach(cart -> {
                //登录状态购物车已存在当前商品，更新数量
                if(loginHashOps.hasKey(cart.getSkuId())){
                    BigDecimal count = cart.getCount();//未登录状态的数量
                    //获取登录状态的购物车并反序列化
                    String cartJson = loginHashOps.get(cart.getSkuId().toString()).toString();
                    cart= JSON.parseObject(cartJson, Cart.class);
                    cart.setCount(cart.getCount().add(count));
                    this.cartAsyncService.updateCartByUserIdAndSkuId(userId.toString(),cart);
                }else {
                    //登录状态购物车不包含该记录，新增
                    cart.setUserId(userId.toString());
                    this.cartAsyncService.saveCart(userId.toString(),cart);
                }
                loginHashOps.put(cart.getSkuId().toString(),JSON.toJSONString(cart));
            });
            //合并完未登录的购物车，要删除未登录的购物车
            this.cartAsyncService.deleteCartByUserId(userKey);
            this.redisTemplate.delete(unloginKey);
        }
            //查询登录状态所有购物车信息，反序列化返回
        List<Object> loginCartJsons = loginHashOps.values();
        if(!CollectionUtils.isEmpty(loginCartJsons)){
            return loginCartJsons.stream().map(loginCartJson->{
                Cart cart = JSON.parseObject(loginCartJson.toString(), Cart.class);
                String currentPrice = this.redisTemplate.opsForValue().get(PRICE_PREFIX + cart.getSkuId());
                cart.setCurrentPrice(new BigDecimal(currentPrice));
                return cart;
            }).collect(Collectors.toList());
        }
        return null;
    }

    public void updateNUm(Cart cart) {
        String userId = this.getUserId();
        String key = KEY_PREFIX +userId;
        //获取该用户的所有购物车
        BoundHashOperations<String, Object, Object> hashOps =
                this.redisTemplate.boundHashOps(key);
        //判断该用户的购物车是否包含该条信息
        if (hashOps.hasKey(cart.getSkuId().toString())){
            //页面传递要更新的数量
            BigDecimal count = cart.getCount();
            String cartJson = hashOps.get(cart.getSkuId().toString()).toString();
            cart = JSON.parseObject(cartJson, Cart.class);
            cart.setCount(count);
            //更新到mysql及redis
            this.cartAsyncService.updateCartByUserIdAndSkuId(userId,cart);
            hashOps.put(cart.getSkuId().toString(),JSON.toJSONString(cart));
        }
    }

    public void deleteCartByskuId(Long skuId) {
        String userId = this.getUserId();
        String key =KEY_PREFIX + userId;
        //获取该用户的所有购物车
        BoundHashOperations<String, Object, Object> hashOps =
                this.redisTemplate.boundHashOps(key);
        //判断该用户的购物车中是否包含该信息
        if (hashOps.hasKey(skuId.toString())){
            this.cartAsyncService.deleteCartByUserIdAndskuId(userId,skuId);
        }
    }

    public List<Cart> queryCheckedCarts(Long userId) {
        String key= KEY_PREFIX +userId;
        BoundHashOperations<String, Object, Object> hashOps =
                this.redisTemplate.boundHashOps(key);
        List<Object> cartJsons = hashOps.values();
        if(CollectionUtils.isEmpty(cartJsons)){
            return null;
        }
        return cartJsons.stream().map(cartJson->JSON.parseObject(cartJson.toString(),Cart.class))
                .filter(cart -> cart.getCheck()).collect(Collectors.toList());
    }
}


