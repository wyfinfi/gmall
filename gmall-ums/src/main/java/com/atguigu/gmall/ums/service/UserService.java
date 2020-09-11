package com.atguigu.gmall.ums.service;

import com.aliyuncs.exceptions.ClientException;
import com.atguigu.gmall.ums.entity.UserEntity;
import com.baomidou.mybatisplus.extension.service.IService;
import com.atguigu.gmall.common.bean.PageResultVo;
import com.atguigu.gmall.common.bean.PageParamVo;

/**
 * 用户表
 *
 * @author wyfinfi
 * @email 888888888888@hehe.com
 * @date 2020-08-18 22:41:27
 */
public interface UserService extends IService<UserEntity> {

    PageResultVo queryPage(PageParamVo paramVo);

    Boolean checkData(String data, Integer type);

    void register(UserEntity userEntity, String code);

    void send(String mobile, String checkCode) throws ClientException;

    UserEntity queryUser(String loginName, String password);
}

