package com.atguigu.gmall.order.mqlistener;

import com.atguigu.gmall.common.bean.ResponseVo;
import com.atguigu.gmall.order.feign.GmallPmsClient;
import com.atguigu.gmall.pms.entity.SkuEntity;
import com.rabbitmq.client.Channel;
import org.springframework.amqp.core.ExchangeTypes;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.Exchange;
import org.springframework.amqp.rabbit.annotation.Queue;
import org.springframework.amqp.rabbit.annotation.QueueBinding;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.util.CollectionUtils;

import java.io.IOException;
import java.util.List;

/**
 * @Author Administrator
 * @Date 2020/9/9 19:34
 * @Version 1.0
 */
@Configuration
public class OrderListener {
    private static final  String key="cart:price:";
    @Autowired
    private GmallPmsClient pmsClient;
    @Autowired
    private StringRedisTemplate redisTemplate;
    @RabbitListener(bindings = @QueueBinding(
            value = @Queue(value = "CART_ITEM_QUEUE",durable = "true"),
            exchange = @Exchange(value = "SPU_ITEM_EXCHANGE",ignoreDeclarationExceptions = "true",type = ExchangeTypes.TOPIC),
           key = {"item.update"}))
    public void listener(Long spuId, Channel channel, Message message) throws IOException {
        ResponseVo<List<SkuEntity>> listResponseVo = this.pmsClient.querySkusBySpuId(spuId);
        List<SkuEntity> skuEntities = listResponseVo.getData();
        if(!CollectionUtils.isEmpty(skuEntities)){
            skuEntities.forEach(skuEntity -> {
                this.redisTemplate.opsForValue().setIfPresent(key+skuEntity.getId().toString(),
                        skuEntity.getPrice().toString());
            });
            channel.basicAck(message.getMessageProperties().getDeliveryTag(),false);
        }
    }
}
