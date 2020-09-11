package com.atguigu.gmall.index.controller;

import com.atguigu.gmall.common.bean.ResponseVo;
import com.atguigu.gmall.index.service.IndexService;
import com.atguigu.gmall.pms.entity.CategoryEntity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.List;

/**
 * @Author Administrator
 * @Date 2020/9/1 20:41
 * @Version 1.0
 */
@Controller
public class IndexController {
    @Autowired
    private IndexService indexService;
    @ResponseBody
    @GetMapping("index/testlock")
    public ResponseVo<Object> testLock(){
       indexService.testLock();
        return ResponseVo.ok(null);
    }
    @GetMapping
    public String toIndex(Model model){
        List<CategoryEntity> categoryEntitiesthis=this.indexService.queryOneCatgories();
        model.addAttribute("categories", categoryEntitiesthis);
        return "index";
    }
    @ResponseBody
    @GetMapping("index/cates/{pid}")
    public ResponseVo<List<CategoryEntity>> queryTwoCategoryWithSub(@PathVariable("pid") Long pid){
        List<CategoryEntity> categoryEntities=this.indexService.queryTwoCategoryWithSub(pid);
        return ResponseVo.ok(categoryEntities);
    }
}
