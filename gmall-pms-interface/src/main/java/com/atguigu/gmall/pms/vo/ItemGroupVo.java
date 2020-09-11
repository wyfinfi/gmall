package com.atguigu.gmall.pms.vo;

import com.atguigu.gmall.pms.vo.AttrValueVo;
import lombok.Data;

import java.util.List;

/**
 * @Author Administrator
 * @Date 2020/9/3 15:01
 * @Version 1.0
 */
@Data
public class ItemGroupVo {
    private Long id;
    private String groupName;
    private List<AttrValueVo> attrValues;
}
