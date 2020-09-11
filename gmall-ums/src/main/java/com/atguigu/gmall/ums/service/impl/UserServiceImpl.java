package com.atguigu.gmall.ums.service.impl;

import com.aliyuncs.CommonRequest;
import com.aliyuncs.CommonResponse;
import com.aliyuncs.DefaultAcsClient;
import com.aliyuncs.IAcsClient;
import com.aliyuncs.exceptions.ClientException;
import com.aliyuncs.http.MethodType;
import com.aliyuncs.profile.DefaultProfile;
import com.atguigu.gmall.ums.entity.UserEntity;
import com.atguigu.gmall.ums.util.SmsProperties;
import com.google.gson.Gson;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.atguigu.gmall.common.bean.PageResultVo;
import com.atguigu.gmall.common.bean.PageParamVo;

import com.atguigu.gmall.ums.mapper.UserMapper;
import com.atguigu.gmall.ums.service.UserService;
import org.springframework.util.CollectionUtils;


@Service("userService")
public class UserServiceImpl extends ServiceImpl<UserMapper, UserEntity> implements UserService {
    @Autowired
    private StringRedisTemplate redisTemplate;
    @Autowired
    private UserMapper userMapper;
    @Autowired
    private SmsProperties smsProperties;

    private  static String key = "checkCode::";
    @Override
    public PageResultVo queryPage(PageParamVo paramVo) {
        IPage<UserEntity> page = this.page(
                paramVo.getPage(),
                new QueryWrapper<UserEntity>()
        );

        return new PageResultVo(page);
    }

    @Override
    public Boolean checkData(String data, Integer type) {
        QueryWrapper<UserEntity> wrapper = new QueryWrapper<>();
        switch (type){
            case 1:wrapper.eq("username",data); break;
            case 2:wrapper.eq("phone",data); break;
            case 3:wrapper.eq("email",data); break;
            default:
                return null;
        }
        return this.userMapper.selectCount(wrapper)==0;
    }

    @Override
    public void register(UserEntity userEntity, String code) {
        // 校验短信验证码

//         String cacheCode = this.redisTemplate.opsForValue().get(key+ userEntity.getPhone());
//         if (!StringUtils.equals(code, cacheCode)) {
//             return ;
//         }
        //生成盐
        String salt = UUID.randomUUID().toString().substring(0, 6);
        userEntity.setSalt(salt);
        //对密码加密
        userEntity.setPassword(DigestUtils.md5Hex(salt+userEntity.getPassword()));
        userMapper.insert(userEntity);
    }
    @Override
    public void send(String mobile, String checkCode) throws ClientException {
        //调用短信发送SDK，创建client对象
        DefaultProfile profile = DefaultProfile.getProfile(
                smsProperties.getRegionId(),
                smsProperties.getKeyId(),
                smsProperties.getKeySecret());
        IAcsClient client = new DefaultAcsClient(profile);

        //组装请求参数
        CommonRequest request = new CommonRequest();
        request.setSysMethod(MethodType.POST);
        request.setSysDomain("dysmsapi.aliyuncs.com");
        request.setSysVersion("2017-05-25");
        request.setSysAction("SendSms");
        request.putQueryParameter("RegionId", smsProperties.getRegionId());
        request.putQueryParameter("PhoneNumbers", mobile);
        request.putQueryParameter("SignName", smsProperties.getSignName());
        request.putQueryParameter("TemplateCode", smsProperties.getTemplateCode());

        Map<String, Object> param = new HashMap<>();
        param.put("code", checkCode);

        //将包含验证码的集合转换为json字符串
        Gson gson = new Gson();
        request.putQueryParameter("TemplateParam", gson.toJson(param));

        //发送短信
        CommonResponse response = client.getCommonResponse(request);

        //得到json字符串格式的响应结果
        String data = response.getData();

        //解析json字符串格式的响应结果
        HashMap<String, String> map = gson.fromJson(data, HashMap.class);
        String code = map.get("Code");
        String message = map.get("Message");

        //配置参考：短信服务->系统设置->国内消息设置
        //错误码参考：
        //https://help.aliyun.com/document_detail/101346.html?spm=a2c4g.11186623.6.613.3f6e2246sDg6Ry
        //控制所有短信流向限制（同一手机号：一分钟一条、一个小时五条、一天十条）
        if ("isv.BUSINESS_LIMIT_CONTROL".equals(code)) {
            log.error("短信发送过于频繁 " + "【code】" + code + ", 【message】" + message);
            //throw new GuliException(ResultCodeEnum.SMS_SEND_ERROR_BUSINESS_LIMIT_CONTROL);
        }

        if (!"OK".equals(code)) {
            log.error("短信发送失败 " + " - code: " + code + ", message: " + message);
            //throw new GuliException(ResultCodeEnum.SMS_SEND_ERROR);
        }
    }

    @Override
    public UserEntity queryUser(String loginName, String password) {
        List<UserEntity> userEntities = this.list(new QueryWrapper<UserEntity>().eq("username", loginName).or()
                .eq("phone", loginName).or()
                .eq("email", loginName));
        if(CollectionUtils.isEmpty(userEntities)){
            return null;
        }

        for (UserEntity userEntity : userEntities) {
            password =DigestUtils.md5Hex(userEntity.getSalt()+password);
            if(!StringUtils.equals(userEntity.getPassword(),password)){
                return userEntity;
            }
        }
        return null;
    }

}