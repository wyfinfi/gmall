package com.atguigu.gmall.ums.service;

import com.atguigu.gmall.ums.entity.UserCollectShopEntity;

import com.atguigu.gmall.common.bean.PageResultVo;
import com.atguigu.gmall.common.bean.PageParamVo;
import com.baomidou.mybatisplus.extension.service.IService;


/**
 * 关注店铺表
 *
 * @author wyfinfi
 * @email 888888888888@hehe.com
 * @date 2020-08-18 22:41:27
 */
public interface UserCollectShopService extends IService<UserCollectShopEntity> {

    PageResultVo queryPage(PageParamVo paramVo);
}

