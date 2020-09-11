package com.atguigu.gmall.item.service;

import com.atguigu.gmall.common.bean.ResponseVo;
import com.atguigu.gmall.item.feign.GmallPmsClient;
import com.atguigu.gmall.item.feign.GmallSmsClient;
import com.atguigu.gmall.item.feign.GmallWmsClient;
import com.atguigu.gmall.item.vo.ItemVo;
import com.atguigu.gmall.pms.entity.*;
import com.atguigu.gmall.pms.vo.ItemGroupVo;
import com.atguigu.gmall.pms.vo.SaleAttrValueVo;
import com.atguigu.gmall.sms.intfc.vo.ItemSaleVo;
import com.atguigu.gmall.wms.entity.WareSkuEntity;
import org.apache.commons.lang3.StringUtils;
import org.aspectj.weaver.ast.Var;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.stream.Collectors;

/**
 * @Author Administrator
 * @Date 2020/9/4 20:09
 * @Version 1.0
 */
@Service
public class ItemService {
    @Autowired
    private GmallPmsClient pmsClient;
    @Autowired
    private GmallWmsClient wmsClient;
    @Autowired
    private GmallSmsClient smsClient;
    @Autowired
    private ThreadPoolExecutor threadPoolExecutor;

    public ItemVo load(Long skuId) {
        ItemVo itemVo = new ItemVo();

        CompletableFuture<SkuEntity> skuCompletableFuture = CompletableFuture.supplyAsync(() -> {
            //根据skuId查询sku的信息1
            ResponseVo<SkuEntity> skuEntityResponseVo =
                    this.pmsClient.querySkuById(skuId);
            SkuEntity skuEntity = skuEntityResponseVo.getData();
            if (skuEntity == null) {
                return null;
            }
            itemVo.setSkuId(skuId);
            itemVo.setTitle(skuEntity.getTitle());
            itemVo.setSubTitle(skuEntity.getSubtitle());
            itemVo.setPrice(skuEntity.getPrice());
            itemVo.setWeight(skuEntity.getWeight());
            itemVo.setDefaultImage(skuEntity.getDefaultImage());
            return skuEntity;
        }, threadPoolExecutor);
        CompletableFuture<Void> categoryCompletableFuture =
                skuCompletableFuture.thenAcceptAsync(skuEntity -> {
                    //根据cid3查询分类信息2
                    ResponseVo<List<CategoryEntity>> listResponseVo =
                            this.pmsClient.queryCategoriesByCid3(skuEntity.getCatagoryId());
                    List<CategoryEntity> categoryEntities = listResponseVo.getData();
                    itemVo.setCategories(categoryEntities);
                }, threadPoolExecutor);
        CompletableFuture<Void> brandCompletableFuture = skuCompletableFuture.thenAcceptAsync(skuEntity -> {
            //根据品牌的id 查询品牌3
            ResponseVo<BrandEntity> brandEntityResponseVo =
                    this.pmsClient.queryBrandById(skuEntity.getBrandId());
            BrandEntity brandEntity = brandEntityResponseVo.getData();
            if (brandEntity == null) {
                itemVo.setBrandId(brandEntity.getId());
                itemVo.setBrandName(brandEntity.getName());
            }
        }, threadPoolExecutor);
        CompletableFuture<Void> spuCompletableFuture = skuCompletableFuture.thenAcceptAsync(skuEntity -> {
            //根据spuId查询spu4
            ResponseVo<SpuEntity> spuEntityResponseVo =
                    this.pmsClient.querySpuById(skuEntity.getSpuId());
            SpuEntity spuEntity = spuEntityResponseVo.getData();
            itemVo.setSpuId(spuEntity.getId());
            itemVo.setSpuName(spuEntity.getName());
        });
        CompletableFuture<Void> skuImageCompletableFuture = CompletableFuture.runAsync(() -> {
            //根据skuId查询图片5
            ResponseVo<List<SkuImagesEntity>> skuImageResponseVo =
                    this.pmsClient.queryImagesBySkuId(skuId);
            List<SkuImagesEntity> skuImagesEntityList = skuImageResponseVo.getData();
            itemVo.setImages(skuImagesEntityList);
        }, threadPoolExecutor);
        CompletableFuture<Void> salesCompletableFuture = CompletableFuture.runAsync(() -> {
            //根据skuId查询营销信息6
            ResponseVo<List<ItemSaleVo>> salesResponseVo =
                    this.smsClient.querySalesBySkuId(skuId);
            List<ItemSaleVo> saleVos = salesResponseVo.getData();
            itemVo.setSales(saleVos);
        }, threadPoolExecutor);
        CompletableFuture<Void> wareCompletableFuture = CompletableFuture.runAsync(() -> {
            //根据skuId查询sku的库存信息7
            ResponseVo<List<WareSkuEntity>> wareResponseVo =
                    this.wmsClient.queryWareSkuBySkuId(skuId);
            List<WareSkuEntity> wareSkuEntityList = wareResponseVo.getData();
            if (!CollectionUtils.isEmpty(wareSkuEntityList)) {
                itemVo.setStore(wareSkuEntityList.stream().anyMatch(
                        wareSkuEntity -> wareSkuEntity.getStock() - wareSkuEntity.getStockLocked() > 0));
            }
        }, threadPoolExecutor);
        CompletableFuture<Void> saleAttrsCompletableFuture = skuCompletableFuture.thenAcceptAsync(skuEntity -> {

            // 根据spuId查询spu下的所有sku的销售属性8
            ResponseVo<List<SaleAttrValueVo>> saleAttrlistResponseVo =
                    this.pmsClient.querySkuAttrValuesBySpuId(skuEntity.getSpuId());
            List<SaleAttrValueVo> saleAttrValues = saleAttrlistResponseVo.getData();
            itemVo.setSaleAttrs(saleAttrValues);
        }, threadPoolExecutor);
        CompletableFuture<Void> skuAttrCompletableFuture = CompletableFuture.runAsync(() -> {

            // 当前sku的销售属性9
            ResponseVo<List<SkuAttrValueEntity>> values = this.pmsClient.querySkuAttrValuesBySkuId(skuId);
            List<SkuAttrValueEntity> skuAttrValueEntityList = values.getData();
            Map<Long, String> map =
                    skuAttrValueEntityList.stream().collect(
                            Collectors.toMap(SkuAttrValueEntity::getAttrId, SkuAttrValueEntity::getAttrName));
            itemVo.setSaleAttr(map);
        }, threadPoolExecutor);
        CompletableFuture<Void> skusJaonCompletableFuture = skuCompletableFuture.thenAcceptAsync(skuEntity -> {

            // 根据spuId查询spu下的所有sku及销售属性的映射关系10
            ResponseVo<String> stringResponseVo =
                    this.pmsClient.querySkusJsonBySpuId(skuEntity.getSpuId());
            String skusJaon = stringResponseVo.getData();
            itemVo.setSkuJson(skusJaon);
        }, threadPoolExecutor);
        CompletableFuture<Void> spuDesCompletableFuture = skuCompletableFuture.thenAcceptAsync(skuEntity -> {

            // 根据spuId查询spu的海报信息11
            ResponseVo<SpuDescEntity> spuDescEntityResponseVo =
                    this.pmsClient.querySpuDescById(skuEntity.getSpuId());
            SpuDescEntity spuDescEntity = spuDescEntityResponseVo.getData();
            if (spuDescEntity != null && StringUtils.isNotBlank(spuDescEntity.getDecript())) {
                String[] images = StringUtils.split(spuDescEntity.getDecript(), ",");
                itemVo.setSpuImages(Arrays.asList(images));

            }
        }, threadPoolExecutor);
        CompletableFuture<Void> itemGroupCompletableFuture = skuCompletableFuture.thenAcceptAsync(skuEntity -> {
            // 根据cid3 spuId skuId查询组及组下的规格参数及值 12
            ResponseVo<List<ItemGroupVo>> itemGroupVos =
                    this.pmsClient.queryGroupsBySpuIdAndCid(skuEntity.getSpuId(), skuId, skuEntity.getCatagoryId());
            List<ItemGroupVo> itemGroupVoList = itemGroupVos.getData();
            itemVo.setGroups(itemGroupVoList);
        }, threadPoolExecutor);
        CompletableFuture.allOf(categoryCompletableFuture, brandCompletableFuture, wareCompletableFuture,
                spuCompletableFuture, skuImageCompletableFuture, salesCompletableFuture,
                saleAttrsCompletableFuture, skuAttrCompletableFuture, skusJaonCompletableFuture,
                spuDesCompletableFuture, itemGroupCompletableFuture).join();
        return itemVo;
    }
}
