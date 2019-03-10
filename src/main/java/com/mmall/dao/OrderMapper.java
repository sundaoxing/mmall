package com.mmall.dao;

import com.mmall.pojo.Order;
import org.apache.ibatis.annotations.Param;

import java.util.List;
/*
        Order：DAO层接口
 */
public interface OrderMapper {
    int deleteByPrimaryKey(Integer id);

    int insert(Order record);

    int insertSelective(Order record);

    Order selectByPrimaryKey(Integer id);

    int updateByPrimaryKeySelective(Order record);

    int updateByPrimaryKey(Order record);

//      查询订单，根据用户Id和订单号（不是订单Id），返回Order对象
    Order selectByUserIdAndOrderNo(@Param("userId")Integer userId,@Param("orderNo") Long orderNo);
//      查询订单，根据订单号，返回Order对象
    Order selectByOrderNo(Long orderNo);
//      查询订单，根据用户Id，返回Order对象的List列表
    List<Order> selectByUserId(Integer userId);
//      查询订单（所有），返回Order对象的List列表
    List<Order> selectAll();
}