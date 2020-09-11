package com.atguigu.gmall.sms.service.impl;

import com.atguigu.gmall.sms.entity.ItemSaleVo;
import com.atguigu.gmall.sms.entity.SkuFullReductionEntity;
import com.atguigu.gmall.sms.entity.SkuLadderEntity;
import com.atguigu.gmall.sms.intfc.vo.SkuSaleVo;
import com.atguigu.gmall.sms.mapper.SkuFullReductionMapper;
import com.atguigu.gmall.sms.mapper.SkuLadderMapper;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;


import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.atguigu.gmall.common.bean.PageResultVo;
import com.atguigu.gmall.common.bean.PageParamVo;

import com.atguigu.gmall.sms.mapper.SkuBoundsMapper;
import com.atguigu.gmall.sms.entity.SkuBoundsEntity;
import com.atguigu.gmall.sms.service.SkuBoundsService;
import org.springframework.util.CollectionUtils;


@Service("skuBoundsService")
public class SkuBoundsServiceImpl extends ServiceImpl<SkuBoundsMapper, SkuBoundsEntity> implements SkuBoundsService {
    @Autowired
    private SkuFullReductionMapper skuFullReductionMapper;
    @Autowired
    private SkuLadderMapper skuLadderMapper;
    @Override
    public void saveSkuSaleInfo(SkuSaleVo skuSaleVo) {
        SkuBoundsEntity skuBoundsEntity = new SkuBoundsEntity();
        BeanUtils.copyProperties(skuSaleVo, skuBoundsEntity);
        //数据库保存的是0-15，页面绑定的是0000-1111
        List<Integer> works = skuSaleVo.getWork();
        if (!CollectionUtils.isEmpty(works)) {
            skuBoundsEntity.setWork(works.get(0) * 8 + works.get(1) * 4 + works.get(2) * 2 + works.get(3));
        }
        this.save(skuBoundsEntity);
        //满减优惠
        SkuFullReductionEntity skuFullReductionEntity = new SkuFullReductionEntity();
        BeanUtils.copyProperties(skuSaleVo,skuFullReductionEntity);
        skuFullReductionEntity.setAddOther(skuSaleVo.getAddOther());
        this.skuFullReductionMapper.insert(skuFullReductionEntity);
        //数量折扣
        SkuLadderEntity skuLadderEntity = new SkuLadderEntity();
        BeanUtils.copyProperties(skuSaleVo,skuLadderEntity);
        this.skuLadderMapper.insert(skuLadderEntity);
    }

    @Override
    public List<ItemSaleVo> querySalesBySkuId(Long skuId) {
        ArrayList<ItemSaleVo> itemSaleVos = new ArrayList<>();
        //查询积分信息
        SkuBoundsEntity skuBoundsEntity = this.getOne(new QueryWrapper<SkuBoundsEntity>().eq("sku_id", skuId));
        ItemSaleVo bounds = new ItemSaleVo();
        bounds.setType("积分");
        bounds.setDesc("送"+skuBoundsEntity.getGrowBounds()+"成长积分，送"+skuBoundsEntity.getBuyBounds()+"购物积分");
      itemSaleVos.add(bounds);
        //查询满减信息
        SkuFullReductionEntity reductionEntity = this.skuFullReductionMapper.selectOne(
                new QueryWrapper<SkuFullReductionEntity>().eq("sku_id", skuId));
        ItemSaleVo reduction = new ItemSaleVo();
        reduction.setType("满减");
        reduction.setDesc("满"+reductionEntity.getFullPrice()+"减"+reductionEntity.getReducePrice());
        itemSaleVos.add(reduction);
        //查询打折信息
        SkuLadderEntity ladderEntity = this.skuLadderMapper.selectOne(
                new QueryWrapper<SkuLadderEntity>().eq("sku_id", skuId));
        ItemSaleVo ladder = new ItemSaleVo();
        ladder.setType("打折");
        ladder.setDesc("满"+ladderEntity.getFullCount()+"件打"+ladderEntity.getDiscount().divide(
                new BigDecimal(10))+"折");
        itemSaleVos.add(ladder);
        return  itemSaleVos;
    }

    @Override
    public PageResultVo queryPage(PageParamVo paramVo) {
        IPage<SkuBoundsEntity> page = this.page(
                paramVo.getPage(),
                new QueryWrapper<SkuBoundsEntity>()
        );

        return new PageResultVo(page);
    }


}