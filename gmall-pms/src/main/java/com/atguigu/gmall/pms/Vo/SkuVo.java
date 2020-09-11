package com.atguigu.gmall.pms.Vo;

import com.atguigu.gmall.pms.entity.SkuAttrValueEntity;
import com.atguigu.gmall.pms.entity.SkuEntity;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

/**
 * @Author Administrator
 * @Date 2020/8/24 10:21
 * @Version 1.0
 */
@Data
public class SkuVo extends SkuEntity {
    private List<String> images;

    //积分活动
    private BigDecimal growBounds;
    private BigDecimal buyBounds;

    private List<Integer> work;

   //满减活动
    private BigDecimal fullPrice;
    private BigDecimal reducePrice;
    private Integer FulladdOther;

    private Integer fullCount;
    private BigDecimal discount;
    private Integer addOther;

    private List<SkuAttrValueEntity> saleAttrs;
}
