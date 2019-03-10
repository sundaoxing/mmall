package com.mmall.service.impl;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.mmall.common.Const;
import com.mmall.common.ProductStatus;
import com.mmall.common.ResponseCode;
import com.mmall.common.ServerResponse;
import com.mmall.dao.CategoryMapper;
import com.mmall.dao.ProductMapper;
import com.mmall.pojo.Category;
import com.mmall.pojo.Product;
import com.mmall.service.ICategoryService;
import com.mmall.service.IProductService;
import com.mmall.util.DateTimeUtil;
import com.mmall.util.PropertiesUtil;
import com.mmall.vo.ProductDetailVo;
import com.mmall.vo.ProductListVo;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
/*
        service层
 */
@Service("iProductService")
public class ProductServiceImpl implements IProductService {
    @Autowired
    private ProductMapper productMapper;
    @Autowired
    private CategoryMapper categoryMapper;
    @Autowired
    private ICategoryService iCategoryService;

    /**
     * 业务1：添加或者更新商品信息：
     *          1.判断前端传入的商品信息Product对象是否为空
     *              是：返回错误信息给前端
     *              否：1.判断商品信息Product对象的副图片是否为空
     *                      否：将副图片的第一张图片设置为主图片
     *                  2.判断Product对象的Id是否为空
     *                      否：->代表更新商品信息
     *                              1.调用DAO层接口，将更新Product到数据库中
     *                              2.将更新结果返回给前端
     *                      是：->代表新增商品信息
     *                              1.调用DAO层接口，将新增Product插入到数据库中
     *                              2.将插入结果返回给前端
     * @param product   商品对象
     * @return
     */
    @Override
    public ServerResponse<String> addOrUpdateProduct(Product product) {
        if(product !=null){
            if(StringUtils.isNotBlank(product.getSubImages())){
                String MainImage = product.getSubImages().split(",")[0];
                if(StringUtils.isNotBlank(MainImage)){
                    product.setMainImage(MainImage);
                }
            }
            if(product.getId() !=null){
                int rowCount = productMapper.updateByPrimaryKey(product);
                if(rowCount >0){
                    return ServerResponse.createBySuccessMsg("更新商品信息成功");
                }
                return ServerResponse.createByErrorMsg("更新商品信息失败");
            }
            else{
                int rowCount = productMapper.insert(product);
                if(rowCount >0){
                    return ServerResponse.createBySuccessMsg("添加商品成功");
                }
                return ServerResponse.createByErrorMsg("添加商品失败");
            }
        }else{
            return ServerResponse.createByErrorMsg("添加或更新商品参数错误");
        }
    }

    /**
     * 业务2：设置商品销售状态
     *          1.判断参数ProductId和status是否为空
     *              是：返回错误信息给前端
     *              否：1.新建Product对象，设置productId，status，updateTime
     *                  2.调用DAO层接口，将product对象中不为空的字段更新到数据库中
     *                  3.将更新结果返回给前端
     * @param productId 商品Id
     * @param status    商品状态
     * @return
     */
    @Override
    public ServerResponse<String> setSaleStatus(Integer productId,Integer status) {
        if(productId == null || status== null){
            return ServerResponse.createByErrorCodeMsg(ResponseCode.ILLEGAL_ARGUMENT.getCode(),ResponseCode.ILLEGAL_ARGUMENT.getDesc());
        }
        Product product = new Product();
        product.setId(productId);
        product.setStatus(status);
        product.setUpdateTime(new Date());
        int rowCount=productMapper.updateByPrimaryKeySelective(product);
        if(rowCount >0){
            return ServerResponse.createBySuccessMsg("设置商品销售状态成功");
        }
        return ServerResponse.createByErrorMsg("设置商品销售状态失败");
    }

    /**
     * 业务3：获取商品详情——管理员操作
     *          1.判断ProductId是否为空
     *          2.调用DAO层接口，查询商品详情
     *          3.判断你查询的Product对象是否为空
     *              是：返回商品下架或删除信息给前端
     *              否：1.将Product对象包装成ProductDetailVo对象（优化的product对象）
     *                  2.将ProductDetailVo对象返回给前端
     * @param productId 商品Id
     * @return
     */
    @Override
    public ServerResponse<ProductDetailVo> getProductDetailManage(Integer productId) {
        if(productId ==null){
            return ServerResponse.createByErrorCodeMsg(ResponseCode.ILLEGAL_ARGUMENT.getCode(),ResponseCode.ILLEGAL_ARGUMENT.getDesc());
        }
        Product product =productMapper.selectByPrimaryKey(productId);
        if(product ==null){
            return ServerResponse.createByErrorMsg("商品已下架或者已删除");
        }
        ProductDetailVo productDetailVo = assembleProductDetailVo(product);
        return ServerResponse.createBySuccess(productDetailVo);
    }

    /**
     *将Product对象优化成ProductDetailVo对象
     * @param product   商品对象
     * @return
     */
    private ProductDetailVo assembleProductDetailVo(Product product) {
        ProductDetailVo productDetailVo = new ProductDetailVo();
        productDetailVo.setId(product.getId());
        productDetailVo.setDetail(product.getDetail());
        productDetailVo.setSubtitle(product.getSubtitle());
        productDetailVo.setMainImage(product.getMainImage());
        productDetailVo.setSubImages(product.getSubImages());
        productDetailVo.setPrice(product.getPrice());
        productDetailVo.setCategoryId(product.getCategoryId());
        productDetailVo.setName(product.getName());
        productDetailVo.setStatus(product.getStatus());
        productDetailVo.setStock(product.getStock());

        //图片url的前缀
        productDetailVo.setImageHost(PropertiesUtil.getProperty("ftp.server.http.prefix","http://img.happymmall.com/"));
        //设置商品的父分类Id
        Category category =categoryMapper.selectByPrimaryKey(product.getCategoryId());
        if(category == null){
            productDetailVo.setParentCategoryId(0);//默认为根分类（一级类别）
        }
        productDetailVo.setParentCategoryId(category.getParentId());
        //优化商品的创建时间显示
        productDetailVo.setCreateTime(DateTimeUtil.dateToStrStandard(product.getCreateTime()));
        //优化商品的更新时间显示
        productDetailVo.setUpdateTime(DateTimeUtil.dateToStrStandard(product.getUpdateTime()));

        return productDetailVo;

    }

    /**
     * 业务4：获取商品列表——使用mybatis.pageHelper进行分页
     *          1.通过PageHelper设置分页个数，每页大小
     *          2.调用DAO层接口，获取所有商品列表List集合
     *          3.遍历商品列表List集合，将其转化为简略Product对象ProductListVo
     *          4.通过PageInfo开始进行分页
     *          5.将product集合替换成简略product对象集合
     *          6.将PageInfo结果集返回给前端
     * @param pageNum   第几页，默认值第1页
     * @param pageSize  每页大小：每页包含多少条商品信息，默认值10
     * @return
     */
    @Override
    public ServerResponse<PageInfo> getProductList(Integer pageNum, Integer pageSize) {
        //设置分页：分页个数，分页大小
        PageHelper.startPage(pageNum,pageSize);
        //查询商品列表
        List<Product>productList = productMapper.selectProductList();
        //替换成简略product对象
        List<ProductListVo> productListVoList = new ArrayList<>();
        for(Product productItem :productList){
            ProductListVo productListVo = assembleProductListVo(productItem);
            productListVoList.add(productListVo);
        }

        //开始分页
        PageInfo pageResult = new PageInfo(productList);
        //将结果集替换成简略product对象
        pageResult.setList(productListVoList);

        return ServerResponse.createBySuccess(pageResult);
    }

    /**
     * 将Product对象简略成ProductListVo对象
     * @param product   商品对象
     * @return
     */
    private ProductListVo assembleProductListVo(Product product) {
        ProductListVo productListVo = new ProductListVo();
        productListVo.setId(product.getId());
        productListVo.setSubtitle(product.getSubtitle());
        productListVo.setMainImage(product.getMainImage());
        productListVo.setPrice(product.getPrice());
        productListVo.setCategoryId(product.getCategoryId());
        productListVo.setName(product.getName());
        productListVo.setStatus(product.getStatus());

        productListVo.setImageHost(PropertiesUtil.getProperty("ftp.server.http.prefix","http://image.sunxing.fun/"));
        return productListVo;
    }

    /**
     * 业务5：搜索商品列表——根据商品名称（模糊查询）或者商品Id
     *          1.通过PageHelper设置分页个数，每页大小
     *          2.优化productName，方便sql查询使用like关键字
     *          3.调用DAO层接口，获取商品列表
     *          4.遍历商品列表List集合，将其转化为简略Product对象ProductListVo
     *          5.通过PageInfo开始进行分页
     *          6.将product集合替换成简略product对象集合
     *          7.将PageInfo结果集返回给前端
     * @param productName   商品名称
     * @param productId     商品Id
     * @param pageNum       第几页，默认值第1页
     * @param pageSize      每页大小：每页包含多少条商品信息，默认值10
     * @return
     */
    @Override
    public ServerResponse<PageInfo> searchProductList(String productName ,Integer productId,Integer pageNum, Integer pageSize){
        PageHelper.startPage(pageNum,pageSize);
        if(StringUtils.isNotBlank(productName)){
            productName="%"+productName+"%";
        }
        List<Product> productList=productMapper.selectProductByNameAndId(productName,productId);

        List<ProductListVo> productListVoList = new ArrayList<>();
        for(Product productItem :productList){
            ProductListVo productListVo = assembleProductListVo(productItem);
            productListVoList.add(productListVo);
        }

        PageInfo pageResult = new PageInfo(productList);
        pageResult.setList(productListVoList);

        return ServerResponse.createBySuccess(pageResult);
    }

    /**
     * 业务6：获取商品详情
     *          1.判断ProductId是否为空
     *          2.调用DAO层接口，查询商品详情
     *          3.判断你查询的Product对象是否为空或者商品status是否为在售状态
     *              是：返回商品下架或删除信息给前端
     *              否：1.将Product对象包装成ProductDetailVo对象（优化的product对象）
     *                  2.将ProductDetailVo对象返回给前端
     * @param productId     商品Id
     * @return
     */
    @Override
    public ServerResponse<ProductDetailVo> getProductDetail(Integer productId) {
        if(productId ==null){
            return ServerResponse.createByErrorCodeMsg(ResponseCode.ILLEGAL_ARGUMENT.getCode(),ResponseCode.ILLEGAL_ARGUMENT.getDesc());
        }
        Product product =productMapper.selectByPrimaryKey(productId);
        if(product ==null){
            return ServerResponse.createByErrorMsg("商品已下架或者已删除");
        }
        if(product.getStatus() != ProductStatus.On_SALE.getCode()){
            return ServerResponse.createByErrorMsg("商品已下架或者已删除");
        }
        ProductDetailVo productDetailVo = assembleProductDetailVo(product);
        return ServerResponse.createBySuccess(productDetailVo);
    }

    /**
     * 业务7：搜索商品列表：根据关键字或者商品分类id
     *          1.判断关键字或者分类Id是否为空
     *              是：返回错误信息给前端
     *              否：1.若categoryId不为空
     *                  2.调用DAO层接口，查询该商品分类Category对象
     *                  3.若Category对象和关键字都为空，直接返回空的结果集
     *                  4.调用CategoryService层接口，获取当前categoryId和其子分类Id的Set集合
     *          2.优化keyWord关键字，方便sql查询使用like关键字
     *          3.通过PageInfo开始进行分页
     *          4.判断商品列表排序的方式orderBy是否不为空
     *              是：设置为orderBy指定的排序方式
     *              否：默认为desc降序的方法排列
     *          5.调用DAO层接口，获取商品列表
     *          6.遍历商品列表List集合，将其转化为简略Product对象ProductListVo
     *          7.通过PageInfo开始进行分页
     *          8.将product集合替换成简略product对象集合
     *          9.将PageInfo结果集返回给前端
     * @param ksyWord       搜索关键字
     * @param categoryId    所属分类Id
     * @param pageNum       第几页，默认值第1页
     * @param pageSize      每页大小：每页包含多少条商品信息，默认值10
     * @param orderBy       商品列表的排序方式
     * @return
     */
    @Override
    public ServerResponse<PageInfo> getProductByKeyWordCategoryId(String ksyWord, Integer categoryId, Integer pageNum, Integer pageSize,String orderBy) {
        if(StringUtils.isBlank(ksyWord) && categoryId ==null){
            return ServerResponse.createByErrorCodeMsg(ResponseCode.ILLEGAL_ARGUMENT.getCode(),ResponseCode.ILLEGAL_ARGUMENT.getDesc());
        }
        Set<Integer> categorySet = new HashSet<>();
        if(categoryId !=null){
            Category category = categoryMapper.selectByPrimaryKey(categoryId);
            //没有该分类，也没有关键字，直接返回空的结果集
            if(category ==null && StringUtils.isBlank(ksyWord)){
                PageHelper.startPage(pageNum,pageSize);
                List<ProductListVo> productListVoList = new ArrayList<>();
                PageInfo pageResult = new PageInfo(productListVoList);
                return ServerResponse.createBySuccess(pageResult);
            }
            categorySet = iCategoryService.getCurrentAndChildCategoryId(categoryId).getData();
        }

        if(StringUtils.isNotBlank(ksyWord)){
            ksyWord = "%"+ksyWord+"%";
        }
        PageHelper.startPage(pageNum,pageSize);
        //设置商品列表的排序方式
        if(StringUtils.isNotBlank(orderBy)){
            if(Const.ProductOrderBy.PRICE_DESC_ASC.contains(orderBy)){
                String [] orderByArray = orderBy.split("_");
                PageHelper.orderBy(orderByArray[0]+" "+orderByArray[1]);
            }
        }
        List<Product>productList =productMapper.selectProductByNameAndCategoryIds(StringUtils.isBlank(ksyWord)?null:ksyWord,
                categorySet.size()==0?null:categorySet);
        List<ProductListVo> productListVoList = new ArrayList<>();
        for(Product productItem:productList){
            ProductListVo productListVo = assembleProductListVo(productItem);
            productListVoList.add(productListVo);
        }
        PageInfo resultPage = new PageInfo(productList);
        resultPage.setList(productListVoList);
        return ServerResponse.createBySuccess(resultPage);
    }

}
