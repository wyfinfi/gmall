package com.atguigu.gmall.item.vo;

import com.atguigu.gmall.pms.entity.CategoryEntity;
import com.atguigu.gmall.pms.entity.SkuImagesEntity;
import com.atguigu.gmall.pms.vo.ItemGroupVo;
import com.atguigu.gmall.pms.vo.SaleAttrValueVo;
import com.atguigu.gmall.sms.intfc.vo.ItemSaleVo;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

/**
 * @Author Administrator
 * @Date 2020/9/3 14:45
 * @Version 1.0
 */
@Data
public class ItemVo {
    //三级分类
    private List<CategoryEntity> categories;
    //品牌
    private Long brandId;
    private String brandName;
    //spu
    private Long spuId;
    private String spuName;
    //sku
    private Long skuId;
    private String title;
    private String subTitle;
    private BigDecimal price;
    private Integer weight;
    private String defaultImage;
    //sku图片
    private List<SkuImagesEntity> images;
    //营销信息
    private List<ItemSaleVo> sales;
    //是否有货
    private Boolean store =false;

    private List<SaleAttrValueVo> saleAttrs;

    //当前sku的销售属性
    private Map<Long,String> saleAttr;
    //sku列表
    private String skuJson;
    //spu海报信息
    private List<String> spuImages;
    //规格参数组及组下的规格参数
    private List<ItemGroupVo> groups;


}
