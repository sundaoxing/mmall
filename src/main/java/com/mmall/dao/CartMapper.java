package com.mmall.dao;

import com.mmall.pojo.Cart;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface CartMapper {
    int deleteByPrimaryKey(Integer id);

    int insert(Cart record);

    int insertSelective(Cart record);

    Cart selectByPrimaryKey(Integer id);

    int updateByPrimaryKeySelective(Cart record);

    int updateByPrimaryKey(Cart record);

//      查询购物车，根据用户id，商品id，返回Cart对象
    Cart selectCartByUserIdProductId(@Param("userId") Integer userId , @Param("productId") Integer productId);
//      查询购物车列表，根据用户id，返回Cart对象集合
    List<Cart> selectCartByUserId(Integer userId);
//      查询商品是否没有被勾选，根据用户id，返回查询的结果个数
    int selectCartProductAllCheckedStatusByUserId(Integer userId);
//      删除购物车列表，根据用户id，商品id列表，返回删除的结果个数
    int deleteByUserIdProductIds(@Param("userId") Integer userId,@Param("productIdList") List<String> productIdList);
//      更新购物车商品的勾选状态，根据用户id，商品id，商品勾选状态，返回更新的结果个数
    int checkedOrUnCheckedProduct(@Param("userId")Integer userId,@Param("productId") Integer productId,@Param("checked")Integer checked);
//      查询购物车中商品的个数，根据用户id，返回购物车中商品的个数
    int selectCartProductCount(Integer userId);
//      查询购物车，根据用户Id，返回购物车中已被勾选的的Cart对象的List列表
    List<Cart> selectCheckedByUserId(Integer userId);
}