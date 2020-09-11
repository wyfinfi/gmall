package com.atguigu.gmall.pms.Vo;

import com.atguigu.gmall.pms.entity.SpuEntity;
import lombok.Data;

import java.util.List;

/**
 * @Author Administrator
 * @Date 2020/8/23 16:56
 * @Version 1.0
 */
@Data
public class SpuVo extends SpuEntity {

    // 图片信息
    private List<String> spuImages;

    // 基本属性信息
    private List<SpuAttrValueVo> baseAttrs;

    // sku信息
    private List<SkuVo> skus;
}