package com.atguigu.gmall.pms.mapper;

import com.atguigu.gmall.pms.entity.AttrEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.springframework.stereotype.Repository;

/**
 * 商品属性
 * 
 * @author wyfinfi
 * @email 888888888888@hehe.com
 * @date 2020-08-18 21:06:00
 */
@Mapper
@Repository
public interface AttrMapper extends BaseMapper<AttrEntity> {
	
}
