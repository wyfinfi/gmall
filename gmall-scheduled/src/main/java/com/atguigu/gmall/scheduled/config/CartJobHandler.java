package com.atguigu.gmall.scheduled.config;

import com.alibaba.fastjson.JSON;
import com.atguigu.gmall.scheduled.bean.Cart;
import com.atguigu.gmall.scheduled.mapper.CartMapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.xxl.job.core.biz.model.ReturnT;
import com.xxl.job.core.handler.annotation.XxlJob;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.BoundHashOperations;
import org.springframework.data.redis.core.BoundListOperations;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.util.List;

/**
 * @Author Administrator
 * @Date 2020/9/11 18:58
 * @Version 1.0
 */
@Component
public class CartJobHandler {
    @Autowired
    private StringRedisTemplate redisTemplate;
    @Autowired
    private CartMapper cartMapper;
    private static final String KEY ="cart:async:exception";
    private static final String KEY_PREFIX ="cart:info:";
    @XxlJob("cartJobHandler")
    public ReturnT<String> executor(String param){
        BoundListOperations<String, String> listOps =
                this.redisTemplate.boundListOps(KEY);
        //如果redis中出现异常的用户为空，则直接返回
        if(listOps.size()==0){
            return ReturnT.SUCCESS;
        }
        //获取第一个失败的用户
        String userId = listOps.rightPop();
        while ((StringUtils.isNotBlank(userId))){
            //先删除
            this.cartMapper.delete(new UpdateWrapper<Cart>().eq("user_id",userId));
            //再查询该用户redis中的购物车
            BoundHashOperations<String, Object, Object> hashOps =
                    this.redisTemplate.boundHashOps(KEY_PREFIX + userId);
            List<Object> cartJsons = hashOps.values();
            //如果该用户购物车数据为空，则直接进入下一次循环
            if(CollectionUtils.isEmpty(cartJsons)){
                userId = listOps.rightPop();
                continue;
            }
            //如果不为空，同步到数据库
            cartJsons.forEach(cartJson->{
                this.cartMapper.insert(JSON.parseObject(cartJson.toString(),Cart.class));
            });
            //查询下一个用户
            userId = listOps.rightPop();
        }
        return ReturnT.SUCCESS;
    }
}
