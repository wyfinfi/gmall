package com.atguigu.gmall.ums.controller;

import java.util.List;
import java.util.concurrent.TimeUnit;

import com.atguigu.gmall.common.utils.FormUtils;
import com.atguigu.gmall.common.utils.RandomUtils;
import com.atguigu.gmall.ums.entity.UserEntity;
import com.netflix.client.ClientException;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;


import com.atguigu.gmall.ums.service.UserService;
import com.atguigu.gmall.common.bean.PageResultVo;
import com.atguigu.gmall.common.bean.ResponseVo;
import com.atguigu.gmall.common.bean.PageParamVo;

/**
 * 用户表
 *
 * @author wyfinfi
 * @email 888888888888@hehe.com
 * @date 2020-08-18 22:41:27
 */
@Api(tags = "用户表 管理")
@RestController
@RequestMapping("ums/user")
public class UserController {

    @Autowired
    private UserService userService;
    @Autowired
    private RedisTemplate redisTemplate;

    @GetMapping("query")
    public ResponseVo<UserEntity> queryUSer(
            @RequestParam("loginName") String loginName,
            @RequestParam("password") String password
    ){
        UserEntity userEntity=userService.queryUser(loginName,password);
        return ResponseVo.ok(userEntity);
    }
    @GetMapping("send/{mobile}")
    public ResponseVo<Object> getCode(@PathVariable String mobile) throws ClientException, com.aliyuncs.exceptions.ClientException {

        //校验手机号是否合法
        if(StringUtils.isEmpty(mobile) || !FormUtils.isMobile(mobile)){
            return ResponseVo.fail("手机号不合法");
        }

        //生成验证码
        String checkCode = RandomUtils.getFourBitRandom();
        //发送验证码
        userService.send(mobile, checkCode);
        //将验证码存入redis缓存
        String key = "checkCode::" + mobile;
        redisTemplate.opsForValue().set(key, checkCode, 5, TimeUnit.MINUTES);

        return ResponseVo.ok("短信发送成功");
    }

    @PostMapping("register")
    public ResponseVo<Object> register(UserEntity userEntity,@RequestParam("code") String code){
        this.userService.register(userEntity,code);
        return ResponseVo.ok();
    }
    @GetMapping("check/{data}/{type}")
    public ResponseVo<Boolean> checkData(@PathVariable("data") String data,
                                         @PathVariable("type") Integer type){
        Boolean res=this.userService.checkData(data,type);
        return ResponseVo.ok(res);
    }
    /**
     * 列表
     */
    @GetMapping
    @ApiOperation("分页查询")
    public ResponseVo<PageResultVo> queryUserByPage(PageParamVo paramVo){
        PageResultVo pageResultVo = userService.queryPage(paramVo);

        return ResponseVo.ok(pageResultVo);
    }


    /**
     * 信息
     */
    @GetMapping("{id}")
    @ApiOperation("详情查询")
    public ResponseVo<UserEntity> queryUserById(@PathVariable("id") Long id){
		UserEntity user = userService.getById(id);

        return ResponseVo.ok(user);
    }

    /**
     * 保存
     */
    @PostMapping
    @ApiOperation("保存")
    public ResponseVo<Object> save(@RequestBody UserEntity user){
		userService.save(user);

        return ResponseVo.ok();
    }

    /**
     * 修改
     */
    @PostMapping("/update")
    @ApiOperation("修改")
    public ResponseVo update(@RequestBody UserEntity user){
		userService.updateById(user);

        return ResponseVo.ok();
    }

    /**
     * 删除
     */
    @PostMapping("/delete")
    @ApiOperation("删除")
    public ResponseVo delete(@RequestBody List<Long> ids){
		userService.removeByIds(ids);

        return ResponseVo.ok();
    }

}
