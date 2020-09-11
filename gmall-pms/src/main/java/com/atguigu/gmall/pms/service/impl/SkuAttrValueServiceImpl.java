package com.atguigu.gmall.pms.service.impl;

import com.alibaba.fastjson.JSON;
import com.atguigu.gmall.pms.vo.AttrValueVo;
import com.atguigu.gmall.pms.vo.SaleAttrValueVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.atguigu.gmall.common.bean.PageResultVo;
import com.atguigu.gmall.common.bean.PageParamVo;

import com.atguigu.gmall.pms.mapper.SkuAttrValueMapper;
import com.atguigu.gmall.pms.entity.SkuAttrValueEntity;
import com.atguigu.gmall.pms.service.SkuAttrValueService;


@Service("skuAttrValueService")
public class SkuAttrValueServiceImpl extends ServiceImpl<SkuAttrValueMapper, SkuAttrValueEntity> implements SkuAttrValueService {
   @Autowired
   private SkuAttrValueMapper skuAttrValueMapper;
    @Override
    public List<SkuAttrValueEntity> querySearchAttrValueBySkuId(Long skuId) {
        return this.skuAttrValueMapper.querySearchAttrValueBySkuId(skuId);
    }

    @Override
    public List<SaleAttrValueVo> querySkuAttrValuesBySpuId(Long spuId) {
        List<AttrValueVo> attrValueVos=
                skuAttrValueMapper.querySkuAttrValuesBySpuId(spuId);
        //对attrId进行分组

        Map<Long, List<AttrValueVo>> map=attrValueVos.stream()
                .collect(Collectors.groupingBy(AttrValueVo::getAttrId));
        ArrayList<SaleAttrValueVo> saleAttrValueVos = new ArrayList<>();
        map.forEach((attrId,attrs)->{
            SaleAttrValueVo saleAttrValueVo = new SaleAttrValueVo();
            //attrId
            saleAttrValueVo.setAttrId(attrId);
            //attrName
            saleAttrValueVo.setAttrName(attrs.get(0).getAttrName());
            //attrValues
            Set<String> attrValues = attrs.stream().map(AttrValueVo::getAttrValue).collect(Collectors.toSet());
            saleAttrValueVo.setAttrValues(attrValues);
            saleAttrValueVos.add(saleAttrValueVo);
        });
        return saleAttrValueVos;
    }

    @Override
    public String querySkusJsonBySpuId(Long spuId) {
        // [{"sku_id": 3, "attr_values": "暗夜黑,12G,512G"}, {"sku_id": 4, "attr_values": "白天白,12G,512G"}]
       //List<Map<String, Object>>因数据库每一条数据都是一个集合
        List<Map<String, Object>>  skusMap = this.skuAttrValueMapper.querySkusJsonBySpuId(spuId);
        Map<String, Long>map=skusMap.stream().collect(Collectors
                    .toMap(sku->sku.get("attr_values").toString(),sku->(Long)sku.get("sku_id")));
        return JSON.toJSONString(map);
    }

    @Override
    public PageResultVo queryPage(PageParamVo paramVo) {
        IPage<SkuAttrValueEntity> page = this.page(
                paramVo.getPage(),
                new QueryWrapper<SkuAttrValueEntity>()
        );

        return new PageResultVo(page);
    }

}