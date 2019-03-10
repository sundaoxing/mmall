package com.mmall.dao;

import com.mmall.pojo.OrderItem;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/*
        OrderItem：DAO层
 */
public interface OrderItemMapper {
    int deleteByPrimaryKey(Integer id);

    int insert(OrderItem record);

    int insertSelective(OrderItem record);

    OrderItem selectByPrimaryKey(Integer id);

    int updateByPrimaryKeySelective(OrderItem record);

    int updateByPrimaryKey(OrderItem record);

//      查询订单条目，根据用户Id和订单号，返回订单条目OrderItem的List列表
    List<OrderItem> selectByUserIdAndOrderNo(@Param("userId") Integer userId,@Param("orderNo") Long orderNo);
//      查询订单条目，根据订单号，返回订单条目OrderItem的List列表
    List<OrderItem> selectByOrderNo(Long orderNo);
//      批量插入订单条目，返回插入结果个数
    int batchInsert(@Param("orderItemList") List<OrderItem> orderItemList);
}