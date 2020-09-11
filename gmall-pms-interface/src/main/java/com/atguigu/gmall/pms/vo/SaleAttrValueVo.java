package com.atguigu.gmall.pms.vo;

import lombok.Data;

import java.util.Set;

/**
 * @Author Administrator
 * @Date 2020/9/3 15:03
 * @Version 1.0
 */
@Data
public class SaleAttrValueVo {
    private Long attrId;
    private String attrName;
    private Set<String> attrValues;
}
