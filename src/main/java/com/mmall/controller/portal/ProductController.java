package com.mmall.controller.portal;

import com.github.pagehelper.PageInfo;
import com.mmall.common.ServerResponse;
import com.mmall.service.IProductService;
import com.mmall.vo.ProductDetailVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

/*
        前台模块：商品接口
 */
@Controller
@RequestMapping("/product/")
public class ProductController {

    @Autowired
    private IProductService iProductService;

    /**
     * 查询商品详情
     *          1.调用服务层接口：getProductDetail(productId)
     * @param productId     商品Id
     * @return
     */
    @RequestMapping(value = "get_product_detail.do",method = RequestMethod.POST)
    @ResponseBody
    public ServerResponse<ProductDetailVo> productDetail(Integer productId){
        return iProductService.getProductDetail(productId);
    }

    /**
     * 根据关键字或者商品分类id搜索商品列表
     *          1.调用服务层接口：getProductByKeyWordCategoryId(keyWord,categoryId,pageNum,pageSize,orderBy)
     * @param keyWord       关键字
     * @param categoryId    分类Id
     * @param pageNum       第几页，默认值第1页
     * @param pageSize      每页大小：每页包含多少条商品信息，默认值10
     * @param orderBy       商品列表的排序方式（按照价格升序/降序）
     * @return
     */
    @RequestMapping(value = "get_product_list.do",method = RequestMethod.POST)
    @ResponseBody
    public ServerResponse<PageInfo> productList(@RequestParam(value = "keyWord",required = false) String keyWord,
                                         @RequestParam(value = "categoryId",required = false) Integer categoryId,
                                         @RequestParam(value = "pageNum",defaultValue = "1") Integer pageNum,
                                         @RequestParam(value = "pageSize",defaultValue = "10") Integer pageSize,
                                         @RequestParam(value = "orderBy",defaultValue = "price_desc") String orderBy){
        return iProductService.getProductByKeyWordCategoryId(keyWord,categoryId,pageNum,pageSize,orderBy);
    }
}
