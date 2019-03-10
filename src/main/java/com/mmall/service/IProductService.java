package com.mmall.service;

import com.github.pagehelper.PageInfo;
import com.mmall.common.ServerResponse;
import com.mmall.pojo.Product;
import com.mmall.vo.ProductDetailVo;

public interface IProductService {
    //后端管理员商品处理
    ServerResponse<String> addOrUpdateProduct(Product product);
    ServerResponse<String> setSaleStatus(Integer productId,Integer status);
    ServerResponse<ProductDetailVo> getProductDetailManage(Integer productId);
    ServerResponse<PageInfo> getProductList(Integer pageNum, Integer pageSize);
    ServerResponse<PageInfo> searchProductList(String productName ,Integer productId,Integer pageNum, Integer pageSize);
    //前台商品处理
    ServerResponse<ProductDetailVo> getProductDetail(Integer productId);
    ServerResponse<PageInfo> getProductByKeyWordCategoryId(String ksyWord,Integer categoryId,Integer pageNum,Integer pageSize,String orderBy);
}
