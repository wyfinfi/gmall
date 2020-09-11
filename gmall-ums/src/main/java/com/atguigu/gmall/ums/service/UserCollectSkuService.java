package com.atguigu.gmall.ums.service;

import com.atguigu.gmall.ums.entity.UserCollectSkuEntity;
import com.baomidou.mybatisplus.extension.service.IService;
import com.atguigu.gmall.common.bean.PageResultVo;
import com.atguigu.gmall.common.bean.PageParamVo;

/**
 * 关注商品表
 *
 * @author wyfinfi
 * @email 888888888888@hehe.com
 * @date 2020-08-18 22:41:27
 */
public interface UserCollectSkuService extends IService<UserCollectSkuEntity> {

    PageResultVo queryPage(PageParamVo paramVo);
}

