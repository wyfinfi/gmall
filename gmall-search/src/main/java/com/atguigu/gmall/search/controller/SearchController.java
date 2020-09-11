package com.atguigu.gmall.search.controller;

import com.atguigu.gmall.common.bean.ResponseVo;
import com.atguigu.gmall.search.bean.SearchParamVo;
import com.atguigu.gmall.search.bean.SearchResponseVo;
import com.atguigu.gmall.search.search.SearchService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @Author Administrator
 * @Date 2020/8/27 14:22
 * @Version 1.0
 */
@Controller
@RequestMapping("search")
public class SearchController {
    @Autowired
    private SearchService searchService;

    @GetMapping
    public String search(SearchParamVo searchParamVo, Model model){
       SearchResponseVo responseVo=this.searchService.search(searchParamVo);
       model.addAttribute("response", responseVo);
       model.addAttribute("searchParam", searchParamVo);
        return "search";
    }
}
