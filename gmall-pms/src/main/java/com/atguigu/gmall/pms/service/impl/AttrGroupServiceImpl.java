package com.atguigu.gmall.pms.service.impl;

import com.atguigu.gmall.pms.Vo.GroupVo;
import com.atguigu.gmall.pms.entity.AttrEntity;
import com.atguigu.gmall.pms.entity.SkuAttrValueEntity;
import com.atguigu.gmall.pms.entity.SpuAttrValueEntity;
import com.atguigu.gmall.pms.mapper.AttrMapper;
import com.atguigu.gmall.pms.mapper.SkuAttrValueMapper;
import com.atguigu.gmall.pms.mapper.SpuAttrValueMapper;
import com.atguigu.gmall.pms.vo.AttrValueVo;
import com.atguigu.gmall.pms.vo.ItemGroupVo;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.atguigu.gmall.common.bean.PageResultVo;
import com.atguigu.gmall.common.bean.PageParamVo;

import com.atguigu.gmall.pms.mapper.AttrGroupMapper;
import com.atguigu.gmall.pms.entity.AttrGroupEntity;
import com.atguigu.gmall.pms.service.AttrGroupService;
import org.springframework.util.CollectionUtils;


@Service("attrGroupService")
public class AttrGroupServiceImpl extends ServiceImpl<AttrGroupMapper, AttrGroupEntity> implements AttrGroupService {

    @Autowired
    private AttrMapper attrMapper;
    @Autowired
    private SpuAttrValueMapper spuAttrValueMapper;
    @Autowired
    private SkuAttrValueMapper skuAttrValueMapper;
    @Override
    public List<GroupVo> quertByCid(Long cid) {
        List<AttrGroupEntity> attrGroupEntityList = this.list(new QueryWrapper<AttrGroupEntity>().eq("category_id", cid));
        if(CollectionUtils.isEmpty(attrGroupEntityList)){
            return null;
        }
        ArrayList<GroupVo> groupVos = new ArrayList<>();
        for (AttrGroupEntity groupEntity : attrGroupEntityList) {
                GroupVo groupVo = new GroupVo();
            BeanUtils.copyProperties(groupEntity,groupVo);
            List<AttrEntity> attrEntities = this.attrMapper.selectList(
                    new QueryWrapper<AttrEntity>().
                            eq("group_id", groupEntity.getId()).eq("type", 1));
            groupVo.setAttrEntities(attrEntities);
            groupVos.add(groupVo);

        }
        return groupVos;
//        return attrGroupEntityList.stream().map(attrGroupEntity -> {
//            GroupVo groupVo = new GroupVo();
//            BeanUtils.copyProperties(attrGroupEntity, groupVo);
//            // 查询规格参数，只需查询出每个分组下的通用属性就可以了（不需要销售属性）
//            List<AttrEntity> attrEntities = this.attrMapper.selectList(new QueryWrapper<AttrEntity>().eq("group_id", attrGroupEntity.getId()).eq("type", 1));
//            groupVo.setAttrEntities(attrEntities);
//            return groupVo;
//        }).collect(Collectors.toList());
    }

    @Override
    public List<ItemGroupVo> queryGroupsBySpuIdAndCid(Long spuId, Long skuId, Long cid) {
        //根据cid查询分组
        List<AttrGroupEntity> attrGroupEntities = this.list(new QueryWrapper<AttrGroupEntity>().eq("category_id", cid));
       if(CollectionUtils.isEmpty(attrGroupEntities)){
           return null;
       }
       //遍历分组查询每个组下的attr
        List<ItemGroupVo> collect = attrGroupEntities.stream().map(group -> {
            ItemGroupVo itemGroupVo = new ItemGroupVo();
            itemGroupVo.setId(group.getId());
            itemGroupVo.setGroupName(group.getName());
            List<AttrEntity> attrEntities = this.attrMapper.selectList(new QueryWrapper<AttrEntity>()
                    .eq("group_id", itemGroupVo.getId()));
            List<Long> attrIds1 = attrEntities.stream().map(AttrEntity::getId)
                    .collect(Collectors.toList());
            String attrIds = attrIds1.toString();
            System.out.println("attrIds = " + attrIds);
            //attrId结合spuId查询规格参数对应值9
            List<SpuAttrValueEntity> spuAttrValueEntities = this.spuAttrValueMapper.selectList(
                    new QueryWrapper<SpuAttrValueEntity>()
                            .eq("spu_id", spuId).in("attr_id", attrIds));
            System.out.println("spuAttrValueEntities = " + spuAttrValueEntities);
            //attrId结合skuId查询规格参数对应值
            List<SkuAttrValueEntity> skuAttrValueEntities = this.skuAttrValueMapper.selectList(
                    new QueryWrapper<SkuAttrValueEntity>()
                            .eq("sku_id", skuId).in("attr_id", attrIds));
            System.out.println("skuAttrValueEntities = " + skuAttrValueEntities);
            ArrayList<AttrValueVo> attrValueVos = new ArrayList<>();
            if (!CollectionUtils.isEmpty(spuAttrValueEntities)) {
                List<AttrValueVo> spuAttrValueVos = spuAttrValueEntities.stream().map(attrValue -> {
                    AttrValueVo attrValueVo = new AttrValueVo();
                    BeanUtils.copyProperties(attrValue, attrValueVo);
                    return attrValueVo;
                }).collect(Collectors.toList());
                attrValueVos.addAll(spuAttrValueVos);
            }
            if (!CollectionUtils.isEmpty(skuAttrValueEntities)) {
                List<AttrValueVo> skuAttrValueVos = skuAttrValueEntities.stream().map(attrValue -> {
                    AttrValueVo attrValueVo = new AttrValueVo();
                    BeanUtils.copyProperties(attrValue, attrValueVo);
                    return attrValueVo;
                }).collect(Collectors.toList());
                attrValueVos.addAll(skuAttrValueVos);
            }
            itemGroupVo.setAttrValues(attrValueVos);
            return itemGroupVo;
        }).collect(Collectors.toList());
       return collect;
    }

    @Override
    public PageResultVo queryPage(PageParamVo paramVo) {
        IPage<AttrGroupEntity> page = this.page(
                paramVo.getPage(),
                new QueryWrapper<AttrGroupEntity>()
        );

        return new PageResultVo(page);
    }

}