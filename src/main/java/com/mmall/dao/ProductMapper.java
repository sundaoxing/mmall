package com.mmall.dao;

import com.mmall.pojo.Product;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Set;

public interface ProductMapper {
    int deleteByPrimaryKey(Integer id);

    int insert(Product record);

    int insertSelective(Product record);

    Product selectByPrimaryKey(Integer id);

    int updateByPrimaryKeySelective(Product record);

    int updateByPrimaryKey(Product record);

//      查询product对象：根据ProductId升序排列，返回所有商品的List集合
    List<Product> selectProductList();
//      查询Product对象：根据商品名称（模糊查询）或者商品Id，返回满足条件的商品List集合
    List<Product> selectProductByNameAndId(@Param(value = "productName") String productName,@Param(value = "productId") Integer ProductId);
//      查询Product对象：根据商品名称（模糊查询）或者当前商品所属分类的Id和其子分类的Id，返回满足条件的商品List集合
    List<Product> selectProductByNameAndCategoryIds(@Param(value = "productName") String productName, @Param(value = "categoryIdSet")Set<Integer> categoryIdSet);
}