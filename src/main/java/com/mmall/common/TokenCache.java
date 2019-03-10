package com.mmall.common;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.TimeUnit;

/*
        本地Token缓存类：
            google实现：guava
 */
public class TokenCache {
    //日志
    private static Logger logger= LoggerFactory.getLogger(TokenCache.class);
    //token字段的前缀
    public static final String TOKEN_PREFIX = "token_";
    //本地缓存
    private static LoadingCache<String,String> localCache = CacheBuilder.newBuilder()
            .initialCapacity(1000).maximumSize(10000).expireAfterAccess(1, TimeUnit.HOURS)
            .build(new CacheLoader<String, String>() {
                /*
                   默认数据加载实现，当调用get方法取值的时候，如果key没有
                   对应的值，就调用该方法进行加载
                 */
                @Override
                public String load(String s) throws Exception {
                    return "null";
                }
            });

    //设置token的key-value对
    public static void setKey(String key,String value){
        localCache.put(key,value);
    }

    //根据key键获取value值
    public static String getKey(String key){
        String value = null;
        try{
            value=localCache.get(key);
            if("null".equals(value)){
                return null;
            }
            return value;
        }catch (Exception e){
            logger.error("localCache get error",e);
        }
        return null;
    }
}
