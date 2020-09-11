package com.atguigu.gmall.pms;

import com.atguigu.gmall.pms.entity.AttrEntity;
import com.atguigu.gmall.pms.entity.AttrGroupEntity;
import com.atguigu.gmall.pms.entity.SkuAttrValueEntity;
import com.atguigu.gmall.pms.entity.SpuAttrValueEntity;
import com.atguigu.gmall.pms.mapper.AttrMapper;
import com.atguigu.gmall.pms.mapper.SkuAttrValueMapper;
import com.atguigu.gmall.pms.mapper.SpuAttrValueMapper;
import com.atguigu.gmall.pms.service.AttrGroupService;
import com.atguigu.gmall.pms.vo.AttrValueVo;
import com.atguigu.gmall.pms.vo.ItemGroupVo;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@SpringBootTest
class GmallPmsApplicationTests {
    @Autowired
    private AttrMapper attrMapper;

    @Test
    void contextLoads() {
        //根据cid查询分组
        List<AttrGroupEntity> attrGroupEntities = attrGroupService.list(new QueryWrapper<AttrGroupEntity>().eq("category_id", cid));
        System.out.println("attrGroupEntities = " + attrGroupEntities);
        if (CollectionUtils.isEmpty(attrGroupEntities)) {
            //return null;
        }
        //遍历分组查询每个组下的attr
        List<ItemGroupVo> collect = attrGroupEntities.stream().map(group -> {
            ItemGroupVo itemGroupVo = new ItemGroupVo();
            itemGroupVo.setId(group.getId());
            itemGroupVo.setGroupName(group.getName());
            List<AttrEntity> attrEntities = this.attrMapper.selectList(new QueryWrapper<AttrEntity>()
                    .eq("group_id", group.getId()));
            System.out.println("attrEntities = " + attrEntities);
            List<Long> attrIds1 = attrEntities.stream().map(AttrEntity::getId)
                    .collect(Collectors.toList());
            //String attrIds= attrIds1.toString();
            System.out.println("attrIds = " + attrIds1);
            //attrId结合spuId查询规格参数对应值9
            List<SpuAttrValueEntity> spuAttrValueEntities = this.spuAttrValueMapper.selectList(
                    new QueryWrapper<SpuAttrValueEntity>().in("attr_id", attrIds1)
                            .eq("spu_id", spuId));
            System.out.println("spuAttrValueEntities = " + spuAttrValueEntities);
            //attrId结合skuId查询规格参数对应值
            List<SkuAttrValueEntity> skuAttrValueEntities = this.skuAttrValueMapper.selectList(
                    new QueryWrapper<SkuAttrValueEntity>().in("attr_id", attrIds1)
                            .eq("sku_id", skuId));
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
        //return collect;
    }
    @Autowired
    private AttrGroupService attrGroupService;

    @Autowired
    private SpuAttrValueMapper spuAttrValueMapper;
    @Autowired
    private SkuAttrValueMapper skuAttrValueMapper;
    Long spuId = 11l;
    Long skuId = 9l;
    Long cid = 225l;



}
