package com.atguigu.gmall.pms.Vo;

import com.atguigu.gmall.pms.entity.AttrEntity;
import com.atguigu.gmall.pms.entity.AttrGroupEntity;
import lombok.Data;

import java.util.List;

/**
 * @Author Administrator
 * @Date 2020/8/23 15:09
 * @Version 1.0
 */
@Data
public class GroupVo  extends AttrGroupEntity {
    private List<AttrEntity> attrEntities;
}
