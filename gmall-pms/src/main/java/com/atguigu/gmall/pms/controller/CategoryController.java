package com.atguigu.gmall.pms.controller;

import java.util.List;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.atguigu.gmall.pms.entity.CategoryEntity;
import com.atguigu.gmall.pms.service.CategoryService;
import com.atguigu.gmall.common.bean.PageResultVo;
import com.atguigu.gmall.common.bean.ResponseVo;
import com.atguigu.gmall.common.bean.PageParamVo;

/**
 * 商品三级分类
 *
 * @author wyfinfi
 * @email 888888888888@hehe.com
 * @date 2020-08-18 21:06:00
 */
@Api(tags = "商品三级分类 管理")
@RestController
@RequestMapping("pms/category")
public class CategoryController {

    @Autowired
    private CategoryService categoryService;

    @GetMapping("all/{cid3}")
    public  ResponseVo<List<CategoryEntity>> queryCategoriesByCid3(@PathVariable("cid3") Long cid3){
        List<CategoryEntity> itemCategories =this.categoryService.queryCategoriesByCid3(cid3);
        return ResponseVo.ok(itemCategories);
    }
    @GetMapping("subs/{pid}")
    public ResponseVo<List<CategoryEntity>> queryCategoriesWithSub(@PathVariable("pid") Long pid){
        List<CategoryEntity> categoryEntityList=
                this.categoryService.queryCategoriesWithSub(pid);
        return ResponseVo.ok(categoryEntityList);
    }

    @ApiOperation("根据父id查询")
    @GetMapping("parent/{parentId}")
    public ResponseVo<List<CategoryEntity>> queryCategory(@PathVariable("parentId") Long parentId){
        QueryWrapper<CategoryEntity> wrapper = new QueryWrapper<>();
        if (parentId!=-1){
            wrapper.eq("parent_id", parentId);
        }
        //List<CategoryEntity> categoryEntityList=this.categoryService.list(wrapper);
        List<CategoryEntity> categoryEntityList=this.categoryService.queryCategory(parentId);
        return ResponseVo.ok(categoryEntityList);
    }

    /**
     * 列表
     */
    @GetMapping
    @ApiOperation("分页查询")
    public ResponseVo<PageResultVo> queryCategoryByPage(PageParamVo paramVo){
        PageResultVo pageResultVo = categoryService.queryPage(paramVo);

        return ResponseVo.ok(pageResultVo);
    }


    /**
     * 信息
     */
    @GetMapping("{id}")
    @ApiOperation("详情查询")
    public ResponseVo<CategoryEntity> queryCategoryById(@PathVariable("id") Long id){
		CategoryEntity category = categoryService.getById(id);

        return ResponseVo.ok(category);
    }

    /**
     * 保存
     */
    @PostMapping
    @ApiOperation("保存")
    public ResponseVo<Object> save(@RequestBody CategoryEntity category){
		categoryService.save(category);

        return ResponseVo.ok();
    }

    /**
     * 修改
     */
    @PostMapping("/update")
    @ApiOperation("修改")
    public ResponseVo update(@RequestBody CategoryEntity category){
		categoryService.updateById(category);

        return ResponseVo.ok();
    }

    /**
     * 删除
     */
    @PostMapping("/delete")
    @ApiOperation("删除")
    public ResponseVo delete(@RequestBody List<Long> ids){
		categoryService.removeByIds(ids);

        return ResponseVo.ok();
    }

}