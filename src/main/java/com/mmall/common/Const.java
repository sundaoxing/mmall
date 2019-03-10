package com.mmall.common;

import com.google.common.collect.Sets;

import java.util.HashSet;
import java.util.Set;

/*
        常量类
 */
public class Const {
    //当前登陆用户（session中的key）
    public static final String CURRENT_USER="current_user";
    //校验类型：type
    public static final String USERNAME="username";
    public static final String EMAIL="email";

    //PropertiesUtil工具类的加载文件名
    public static final String MMALLPROPERTIES = "mmall.properties";

    //标准时间格式
    public static final String STANDARDFORMAT ="yyyy-MM-dd HH:mm:ss";
    /*
        用户角色常量值：
        用户角色：普通用户/管理员用户 是一个组
        通过内部接口类来实现类似枚举的功能
        优点：轻量级，简单易用
     */
    public interface Role{
        int ROLE_CUSTOMER=0; //普通用户
        int ROLE_ADMIN   =1; //管理员
    }

    //搜索商品时排序处理（按照价格升序，降序来排序商品的显示顺序）
    public  interface ProductOrderBy{
        Set<String> PRICE_DESC_ASC = Sets.newHashSet("price_desc","price_asc");
    }

    //购物车中商品购买数量限制信息
    public interface CartProductLimit{
        String LIMIT_NUM_SUCCESS ="LIMIT_NUM_SUCCESS";//限制成功
        String LIMIT_NUM_FAIL = "LIMIT_NUM_FAIL";//限制失败
    }

    //支付宝回调信息
    public interface AlipayCallback{
        //支付宝回调中订单状态信息
        String WAIT_BUYER_PAY = "WAIT_BUYER_PAY";//等待付款
        String TRADE_SUCCESS = "TRADE_SUCCESS";//交易成功

        //支付宝回调后本地校验结果信息
        String RESPONSE_SUCCESS = "success";//校验成功字符串
        String RESPONSE_FAILED = "failed";//校验失败字符串
    }


}
