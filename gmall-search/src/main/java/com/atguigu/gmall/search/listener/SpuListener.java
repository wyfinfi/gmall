package com.atguigu.gmall.search.listener;


import com.atguigu.gmall.search.search.SearchService;
import org.springframework.amqp.core.ExchangeTypes;
import org.springframework.amqp.rabbit.annotation.Exchange;
import org.springframework.amqp.rabbit.annotation.Queue;
import org.springframework.amqp.rabbit.annotation.QueueBinding;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * @Author Administrator
 * @Date 2020/9/1 18:18
 * @Version 1.0
 */
@Component
public class SpuListener {
    @Autowired
    private SearchService searchService;
    @RabbitListener(bindings =@QueueBinding(
            value = @Queue(value = "item_spu_queue",durable = "true"),
            exchange = @Exchange(
                    value = "item_exchange",
                    ignoreDeclarationExceptions = "true",
                    type = ExchangeTypes.TOPIC
            ),
            key = {"item.insert"}
    ))
    public void listenCreate(Long spuId){
        if(spuId==null){
            return;
        }
        //创建索引
        this.searchService.createIndex(spuId);
    }
}
