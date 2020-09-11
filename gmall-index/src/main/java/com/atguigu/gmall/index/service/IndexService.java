package com.atguigu.gmall.index.service;

import com.alibaba.fastjson.JSON;

import com.atguigu.gmall.common.bean.ResponseVo;
import com.atguigu.gmall.index.config.GmallCache;
import com.atguigu.gmall.index.feign.GmallPmsClient;
import com.atguigu.gmall.pms.entity.CategoryEntity;
import com.google.common.collect.Lists;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

/**
 * @Author Administrator
 * @Date 2020/9/1 20:42
 * @Version 1.0
 */
@Service
public class IndexService {
    private static final String KEY_PREFIX = "index:cates:";
    private static Thread thread;
    @Autowired
    private StringRedisTemplate redisTemplate;
    @Autowired
    private GmallPmsClient gmallPmsClient;


    public List<CategoryEntity> queryOneCatgories() {
        //从缓存中获取
        String cacheCategories = redisTemplate.opsForValue().get(KEY_PREFIX + 0);
        if ((StringUtils.isNotBlank(cacheCategories))) {
            return JSON.parseArray(cacheCategories, CategoryEntity.class);
        }
        ResponseVo<List<CategoryEntity>> listResponseVo = this.gmallPmsClient.queryCategory(0l);

        this.redisTemplate.opsForValue().set(KEY_PREFIX + 0, JSON.toJSONString(listResponseVo.getData()), 30, TimeUnit.DAYS);
        return listResponseVo.getData();

    }
    @GmallCache(prefix = "index:cates",timeout = 14400,random = 3600,lock = "lock")
    public List<CategoryEntity> queryTwoCategoryWithSub(Long pid) {
        //从缓存中获取
//        String cacheCategories = redisTemplate.opsForValue().get(KEY_PREFIX + pid);
//        if ((StringUtils.isNotBlank(cacheCategories))) {
//
//            return JSON.parseArray(cacheCategories, CategoryEntity.class);
//
//        }
        ResponseVo<List<CategoryEntity>> listResponseVo = this.gmallPmsClient.queryCategoriesWithSub(pid);
        //this.redisTemplate.opsForValue().set(KEY_PREFIX + pid, JSON.toJSONString(listResponseVo.getData()), 30, TimeUnit.DAYS);
        return listResponseVo.getData();
    }

    public void testLock() {
//        String value = this.redisTemplate.opsForValue().get("num");
//        if (StringUtils.isBlank(value)) {
////            return ResponseVo.ok(null);
////        }
//        int num = Integer.parseInt(value);
//        String count = String.valueOf(++num);
//        this.redisTemplate.opsForValue().set("num", count);
//        return ResponseVo.ok(count);
        String uuid = UUID.randomUUID().toString();
        Boolean lock= tryLock("lock", uuid, 300l);
        //Boolean lock = this.redisTemplate.opsForValue().setIfAbsent("lock", uuid,2,TimeUnit.SECONDS);
        if (lock) {
            String value = this.redisTemplate.opsForValue().get("num");
            if (StringUtils.isBlank(value)) {
                return ;
            }
            int num = Integer.parseInt(value);
            //放入redis
            this.redisTemplate.opsForValue().set("num",  String.valueOf(++num));
            try {
                TimeUnit.SECONDS.sleep(60);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            //测试可重入性
            this.trySubLock(uuid);
            // 2. 释放锁 del
            this.unlock("lock",uuid);
            //String script = "if redis.call('get', KEYS[1]) == ARGV[1] then return redis.call('del', KEYS[1]) else return 0 end";
            //this.redisTemplate.execute(new DefaultRedisScript<>(script), Arrays.asList("lock"),uuid);
            //            if(StringUtils.equals(this.redisTemplate.opsForValue().get("lock"),uuid)){
//                this.redisTemplate.delete("lock");
//            }
        }else{
            try {
                Thread.sleep(100);
                testLock();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

    }
    private void trySubLock(String uuid){
        //加锁
        Boolean lock = this.tryLock("lock", uuid, 300l);
        if(lock){
            System.out.println("分布式可重入锁");
            this.unlock("lock",uuid);
        }
    }
    private Boolean tryLock(String lockName,String uuid,Long expire){
        String script = "if (redis.call('exists', KEYS[1]) == 0 or redis.call('hexists', KEYS[1], ARGV[1]) == 1) " +
                "then" +
                "    redis.call('hincrby', KEYS[1], ARGV[1], 1);" +
                "    redis.call('expire', KEYS[1], ARGV[2]);" +
                "    return 1;" +
                "else" +
                "   return 0;" +
                "end";
        if(!this.redisTemplate.execute(new DefaultRedisScript<>(script,Boolean.class),Arrays.asList(lockName),uuid,expire.toString())){
            try {
                //没有获取到锁重试
                Thread.sleep(200);
                tryLock(lockName, uuid, expire);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        //锁续期
        this.renewTime(lockName,expire);
        return true;
    }
    private void unlock(String lockName,String uuid){
        String script = "if (redis.call('hexists', KEYS[1], ARGV[1]) == 0) then" +
                "    return nil;" +
                "end;" +
                "if (redis.call('hincrby', KEYS[1], ARGV[1], -1) > 0) then" +
                "    return 0;" +
                "else" +
                "    redis.call('del', KEYS[1]);" +
                "    return 1;" +
                "end;";
        Long result = this.redisTemplate.execute(new DefaultRedisScript<>(script, Long.class), Lists.newArrayList(lockName), uuid);
        if(result==null){
            throw  new IllegalMonitorStateException("attempt to unlock lock, not locked by lockName: "
                    + lockName + " with request: "  + uuid);
        }

    }
    private void renewTime(String lockName,Long expire){
        String script = "if redis.call('exists', KEYS[1]) == 1 then return redis.call('expire', KEYS[1], ARGV[1]) else return 0 end";
       thread= new Thread(()->{
            while (this.redisTemplate.execute(new DefaultRedisScript<>(script,Boolean.class),Lists.newArrayList(lockName),expire.toString())){
                try {
                    Thread.sleep(expire*2/3);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        });
       thread.start();
    }
}
