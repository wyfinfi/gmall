package com.atguigu.gmall.pms.Vo;

import com.atguigu.gmall.pms.entity.SpuAttrValueEntity;
import lombok.Data;
import org.apache.commons.lang3.StringUtils;
import org.springframework.util.CollectionUtils;

import java.util.List;

/**
 * @Author Administrator
 * @Date 2020/8/24 10:13
 * @Version 1.0
 */
public class SpuAttrValueVo extends SpuAttrValueEntity {
    private List<Object> valueSelected;

    public void setValueSelected(List<Object> valueSelected){
        // 如果接受的集合为空，则不设置
        if (CollectionUtils.isEmpty(valueSelected)){
            return;
        }
        this.setAttrValue(StringUtils.join(valueSelected, ","));
    }
}
