package com.atguigu.gmall.pms.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.atguigu.gmall.common.bean.PageResultVo;
import com.atguigu.gmall.common.bean.PageParamVo;
import com.atguigu.gmall.pms.entity.CategoryEntity;

import java.util.List;
import java.util.Map;

/**
 * 商品三级分类
 *
 * @author wyfinfi
 * @email 888888888888@hehe.com
 * @date 2020-08-18 21:06:00
 */
public interface CategoryService extends IService<CategoryEntity> {

    PageResultVo queryPage(PageParamVo paramVo);


   List<CategoryEntity> queryCategory(Long parentId);


    List<CategoryEntity> queryCategoriesWithSub(Long pid);

    List<CategoryEntity> queryCategoriesByCid3(Long cid3);
}

