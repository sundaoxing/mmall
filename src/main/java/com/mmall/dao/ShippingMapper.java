package com.mmall.dao;

import com.mmall.pojo.Shipping;
import org.apache.ibatis.annotations.Param;

import java.util.List;
/*
            Shipping：DAO层：数据访问层，与数据库通信
 */
public interface ShippingMapper {
    int deleteByPrimaryKey(Integer id);

    int insert(Shipping record);

    int insertSelective(Shipping record);

    Shipping selectByPrimaryKey(Integer id);

    int updateByPrimaryKeySelective(Shipping record);

    int updateByPrimaryKey(Shipping record);

//      删除地址：根据用户Id和地址Id，返回删除结果的个数
    int deleteByUserIdShippingId(@Param("userId") Integer userId,@Param("shippingId") Integer shippingId);
//      更新地址：根据用户Id和地址Id，返回更新结果的个数
    int updateByUserIdShippingId(Shipping record);
//      查询地址：根据用户Id，地址Id，返回地址Shipping对象
    Shipping selectByUserIdShippingId(@Param("userId") Integer userId,@Param("shippingId") Integer shippingId);
//      查询地址：根据用户Id，返回地址Shipping对象List列表
    List<Shipping> selectByUserId(Integer userId);
}