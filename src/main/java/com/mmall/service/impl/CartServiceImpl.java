package com.mmall.service.impl;

import com.google.common.base.Splitter;
import com.mmall.common.Const;
import com.mmall.common.ProductStatus;
import com.mmall.common.ResponseCode;
import com.mmall.common.ServerResponse;
import com.mmall.dao.CartMapper;
import com.mmall.dao.ProductMapper;
import com.mmall.pojo.Cart;
import com.mmall.pojo.Product;
import com.mmall.service.ICartService;
import com.mmall.util.BigDecimalUtil;
import com.mmall.util.PropertiesUtil;
import com.mmall.vo.CartProductVo;
import com.mmall.vo.CartVo;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Service("iCartService")
public class CartServiceImpl implements ICartService {

    @Autowired
    private CartMapper cartMapper;
    @Autowired
    private ProductMapper productMapper;

    /**
     * 业务1：添加商品到购物车
     *          1.判断商品productId,商品数量count是否不为空
     *          2.调用DAO层，根据用户Id，商品Id，查询购物车中该商品条目Cart对象
     *          3.判断Cart对象是否为空
     *              为空：  ->说明购物车中没有该商品，直接插入
     *                      1.构造购物车商品条目Cart
     *                      2.对Cart对象的属性进行赋值
     *                      3.调用DAO层，将该Cart对象插入到数据库中
     *              不为空：->说明购物车中已经添加过该商品了，更新商品数量即可
     *                      1.更新购物车中该商品的购买个数
     *                      2.调用DAO层，将该Cart对象的不为空的字段更新到数据库中
     *          4.调用getCartVoLimit(Integer userId)方法，将CartProductVo对象转换成CartVo对象，返回给前端
     * @param userId        用户Id
     * @param productId     商品Id
     * @param count         商品数量
     * @return
     */
    @Override
    public ServerResponse<CartVo> add(Integer userId, Integer productId, Integer count) {
        if(productId ==null || count ==null){
            return ServerResponse.createByErrorCodeMsg(ResponseCode.ILLEGAL_ARGUMENT.getCode(),ResponseCode.ILLEGAL_ARGUMENT.getDesc());
        }
        Cart cart =cartMapper.selectCartByUserIdProductId(userId ,productId);
        if(cart ==null){
            Cart cartItem = new Cart();
            cartItem.setUserId(userId);
            cartItem.setProductId(productId);
            //默认新添加的商品是勾选状态
            cartItem.setChecked(ProductStatus.CHECKED.getCode());
            cartItem.setQuantity(count);
            cartMapper.insert(cartItem);
        } else{
            count = count+cart.getQuantity();
            cart.setQuantity(count);
            cartMapper.updateByPrimaryKeySelective(cart);
        }
        return this.list(userId);
    }

    /**
     * 业务2：在购物车中更新商品数量
     *          1.判断商品productId,商品数量count是否不为空
     *          2.调用DAO层，根据用户Id，商品Id，查询购物车中该商品条目Cart对象
     *          3.判断Cart对象是否不为空
     *              不为空：->更新商品数量
     *                      1.更新购物车中该商品的购买个数
     *                      2.调用DAO层，将该Cart对象的不为空的字段更新到数据库中
     *          4.调用getCartVoLimit(Integer userId)方法，将CartProductVo对象转换成CartVo对象，返回给前端
     * @param userId        用户Id
     * @param productId     商品Id
     * @param count         商品数量
     * @return
     */
    @Override
    public ServerResponse<CartVo> update(Integer userId, Integer productId, Integer count) {
        if(productId ==null || count ==null){
            return ServerResponse.createByErrorCodeMsg(ResponseCode.ILLEGAL_ARGUMENT.getCode(),ResponseCode.ILLEGAL_ARGUMENT.getDesc());
        }
        Cart cart =cartMapper.selectCartByUserIdProductId(userId ,productId);
        if(cart != null){
            cart.setQuantity(count);
        }
        cartMapper.updateByPrimaryKeySelective(cart);
        return this.list(userId);
    }

    /**
     * 业务3：删除购物车中商品条目，根据用户Id，商品Id
     *      （可选：批量删除多个商品条目------>商品Id：类型String，使用","分隔每一个商品Id）
     *          1.将String类型的ProductId按照“，”分隔成list列表
     *          2.判断该列表是否为空
     *          3.调用DAO层接口，根据商品Id列表删除该用户下所有商品条目Cart
     *          4.调用getCartVoLimit(Integer userId)方法，将CartProductVo对象转换成CartVo对象，返回给前端
     * @param userId        用户Id
     * @param productIds    商品Id列表
     * @return
     */
    @Override
    public ServerResponse<CartVo> deleteProduct(Integer userId ,String productIds){
        List<String> productIdList = Splitter.on(",").splitToList(productIds);
        if(CollectionUtils.isEmpty(productIdList)){
            return ServerResponse.createByErrorCodeMsg(ResponseCode.ILLEGAL_ARGUMENT.getCode(),ResponseCode.ILLEGAL_ARGUMENT.getDesc());
        }
        cartMapper.deleteByUserIdProductIds(userId,productIdList);
        return this.list(userId);
    }

    /**
     * 业务4：获取当前用户下所有购物车商品条目列表
     *          1.调用getCartVoLimit(Integer userId)方法，将CartProductVo对象转换成CartVo对象，返回给前端
     * @param userId    用户Id
     * @return
     */
    @Override
    public ServerResponse<CartVo> list(Integer userId) {
        return ServerResponse.createBySuccess(this.getCartVoLimit(userId));
    }

    /**
     * 业务5：对购物车中商品条目进行勾选（全选/全不选  单选/单不选）
     *          productId 为空     ->全选/全不选
     *          productId 不为空   ->单选/单不选
     *          1.调用DAO层接口，更新购物车商品条目的勾选状态
     *          2.调用getCartVoLimit(Integer userId)方法，将CartProductVo对象转换成CartVo对象，返回给前端
     * @param userId        用户Id
     * @param productId     商品Id（可为空）
     * @param checked       商品勾选状态（4：勾选，5不勾选）
     * @return
     */
    @Override
    public ServerResponse<CartVo> selectOrUnSelect(Integer userId,Integer productId,Integer checked) {
        cartMapper.checkedOrUnCheckedProduct(userId,productId,checked);
        return this.list(userId);
    }

    /**
     * 业务6：获取当前用户购物车中所有商品的购买个数
     *          1.判断UserId是否为空
     *              是：返回0
     *          2.调用DAO层接口，查询购物车中商品的购买个数
     * @param userId
     * @return
     */
    @Override
    public ServerResponse<Integer> getCartProductCount(Integer userId) {
        if(userId ==null){
            return ServerResponse.createBySuccess(0);
        }
        return ServerResponse.createBySuccess(cartMapper.selectCartProductCount(userId));
    }

    /**
     * 构建购物车CartVo对象：Cart->CartProductVo->CartVo
     *          1.创造CartVo对象
     *          2.调用DAO层接口，获取购物车Cart对象的List列表
     *          3.创造CartProductVo对象List列表
     *          4.初始化购物车的总价格为0
     *          5.判断购物车Cart的列表是否不为空
     *              是：1.遍历Cart列表，获取每一个Cart对象
     *                      1.创造CartProductVo对象
     *                      2.调用DAO层接口，根据商品Id获取该商品的详细信息
     *                      3.对CartProductVo对象的部分属性进行赋值
     *                      4.初始化购买数量的限制数量为0
     *                      5.判断该商品的当前库存量>=购物车中商品的购买数量
     *                          是：1.将购买数量的限制数量设置为购物车中商品的购买数量
     *                              2.将CartProductVo的LimitQuantity属性设置为"限制数量成功"
     *                          否：1.将购买数量的限制数量设置为商品的库存数量
     *                              2.将CartProductVo的LimitQuantity属性设置为"限制数量失败"
     *                              3.创造Cart对象，将quantity属性设置为购买数量的限制数量
     *                              4.调用DAO层接口，根据当前购物车的Id，更新该购物车Cart对象的quantity到数据库中
     *                      6.将CartProductVo的quantity属性设置为购买数量的限制数量
     *                      7.计算当前CartProductVo购物车商品条目的价格
     *                      8.设置当前CartProductVo购物车商品条目的勾选状态
     *                      9.判断当前CartProductVo购物车商品条目的勾选状态
     *                          是勾选：计算购物车的总价格：将条目的价格和购物车总价累加
     *                      10.将当前CartProductVo购物车商品条目添加到CartProductVo对象List列表中
     *           6.对CartVo中的相关属性进行赋值
     *           7.返回CartVo对象
     * @param userId    用户Id
     * @return
     */
    private CartVo getCartVoLimit(Integer userId){
        CartVo cartVo = new CartVo();
        //获取该用户的购物车中所有Cart对象列表
        List<Cart> cartList = cartMapper.selectCartByUserId(userId);
        List<CartProductVo> cartProductVoList = new ArrayList<>();
        //整个购物车的总价格：商品条目价格的累加
        BigDecimal cartTotalPrice = new BigDecimal("0");
        if(CollectionUtils.isNotEmpty(cartList)){
            for(Cart cartItem : cartList){
                CartProductVo cartProductVo = new CartProductVo();
                cartProductVo.setId(cartItem.getId());
                cartProductVo.setUserId(cartItem.getUserId());
                cartProductVo.setProductId(cartItem.getProductId());

                Product product =productMapper.selectByPrimaryKey(cartItem.getProductId());
                if(product !=null){
                    cartProductVo.setProductMainImage(product.getMainImage());
                    cartProductVo.setProductName(product.getName());
                    cartProductVo.setProductSubtitle(product.getSubtitle());
                    cartProductVo.setProductStatus(product.getStatus());
                    cartProductVo.setProductPrice(product.getPrice());
                    cartProductVo.setProductStock(product.getStock());
                    //购买限制数量（因为当购买量很多时，库存不一定足够多）
                    int buyLimitCount=0;
                    if(product.getStock() >= cartItem.getQuantity()){//库存足够
                        buyLimitCount = cartItem.getQuantity();
                        cartProductVo.setLimitQuantity(Const.CartProductLimit.LIMIT_NUM_SUCCESS);
                    }else{//库存不够
                        buyLimitCount = product.getStock();
                        cartProductVo.setLimitQuantity(Const.CartProductLimit.LIMIT_NUM_FAIL);
                        Cart cartForQuantity = new Cart();
                        cartForQuantity.setId(cartItem.getId());
                        //当库存不够时，此时购买量就是库存量，数据库中相应商品的数量也要随之更新，因为一开始商品数量是用户定义的
                        cartForQuantity.setQuantity(buyLimitCount);
                        cartMapper.updateByPrimaryKeySelective(cartForQuantity);
                    }
                    //购物车中一个商品条目的数量
                    cartProductVo.setQuantity(buyLimitCount);
                    //购物车中一个商品条目的价格：商品数量x商品价格
                    cartProductVo.setProductTotalPrice(BigDecimalUtil.multiply(product.getPrice()
                            .doubleValue(),cartProductVo.getQuantity().doubleValue()));
                    //购物车中一个商品条目的状态：是否勾选
                    cartProductVo.setProductChecked(cartItem.getChecked());
                }

                //判断购物车中一个商品条目是否被勾选
                if(cartProductVo.getProductChecked() == ProductStatus.CHECKED.getCode()){
                    //商品条目价格累加
                    cartTotalPrice = BigDecimalUtil.add(cartTotalPrice.doubleValue(),cartProductVo.getProductTotalPrice().doubleValue());
                }
                cartProductVoList.add(cartProductVo);
            }
        }
        cartVo.setCartTotalPrice(cartTotalPrice);
        cartVo.setCartProductVoList(cartProductVoList);
        cartVo.setAllChecked(isAllCheckedStatus(userId));
        cartVo.setImageHost(PropertiesUtil.getProperty("ftp.server.http.prefix"));
        return cartVo;
    }

    /**
     * 判断购物车中商品是否全部被勾选
     *          1.判断用户Id是否为空
     *          2.调用DAO层接口，根据用户id查询商品是否没有被勾选的个数是否等于0
     *              是：返回true
     *              否：返回false
     * @param userId    用户Id
     * @return
     */
    private boolean isAllCheckedStatus(Integer userId){
        if(userId ==null){
            return false;
        }
        return cartMapper.selectCartProductAllCheckedStatusByUserId(userId) == 0;
    }
}
