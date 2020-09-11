package com.atguigu.gmall.search.repository;

import com.atguigu.gmall.search.bean.Goods;
import org.springframework.data.elasticsearch.repository.ElasticsearchCrudRepository;

/**
 * @Author Administrator
 * @Date 2020/8/26 20:32
 * @Version 1.0
 */
public interface GoodsRepository extends ElasticsearchCrudRepository<Goods,Long> {
}
