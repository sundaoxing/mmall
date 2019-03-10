package com.mmall.controller.portal;

import com.mmall.common.Const;
import com.mmall.common.ProductStatus;
import com.mmall.common.ResponseCode;
import com.mmall.common.ServerResponse;
import com.mmall.pojo.User;
import com.mmall.service.ICartService;
import com.mmall.vo.CartVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpSession;
/*
        前台模块: 购物车接口
 */
@Controller
@RequestMapping("/cart/")
public class CartController {
    @Autowired
    private ICartService iCartService;

    /**
     * 添加商品到购物车
     *          1.从session中获取当前登陆User对象
     *          2.判断User对象是否为空
     *              是：返回错误信息，强制用户登陆
     *          3.调用Service层接口，将添加商品到购物车
     * @param session       Session对象
     * @param productId     商品Id
     * @param count         商品数量
     * @return
     */
    @RequestMapping(value = "add.do",method = RequestMethod.POST)
    @ResponseBody
    public ServerResponse<CartVo> add(HttpSession session, Integer productId, Integer count){
        User user=(User)session.getAttribute(Const.CURRENT_USER);
        if(user == null){
            return ServerResponse.createByErrorCodeMsg(ResponseCode.NEED_LOGIN.getCode(),ResponseCode.NEED_LOGIN.getDesc());
        }
        return iCartService.add(user.getId(),productId,count);
    }

    /**
     * 在购物车中更新商品数量
     *          1.从session中获取当前登陆User对象
     *          2.判断User对象是否为空
     *              是：返回错误信息，强制用户登陆
     *          3.调用Service层接口，在购物车中更新商品数量
     * @param session       Session对象
     * @param productId     商品Id
     * @param count         商品数量
     * @return
     */
    @RequestMapping(value = "update.do",method = RequestMethod.POST)
    @ResponseBody
    public ServerResponse<CartVo> update(HttpSession session, Integer productId, Integer count){
        User user=(User)session.getAttribute(Const.CURRENT_USER);
        if(user == null){
            return ServerResponse.createByErrorCodeMsg(ResponseCode.ILLEGAL_ARGUMENT.getCode(),ResponseCode.ILLEGAL_ARGUMENT.getDesc());
        }
        return iCartService.update(user.getId(),productId,count);
    }

    /**
     * 删除购物车中商品条目
     *          1.从session中获取当前登陆User对象
     *          2.判断User对象是否为空
     *              是：返回错误信息，强制用户登陆
     *          3.调用Service层接口，删除购物车中商品条目
     * @param session       Session对象
     * @param productIds    商品Id列表（String类型，以“，”分隔商品Id）
     * @return
     */
    @RequestMapping(value = "deleteProduct.do",method = RequestMethod.POST)
    @ResponseBody
    public ServerResponse<CartVo> deleteProduct(HttpSession session, String productIds){
        User user=(User)session.getAttribute(Const.CURRENT_USER);
        if(user == null){
            return ServerResponse.createByErrorCodeMsg(ResponseCode.ILLEGAL_ARGUMENT.getCode(),ResponseCode.ILLEGAL_ARGUMENT.getDesc());
        }
        return iCartService.deleteProduct(user.getId(),productIds);
    }

    /**
     *获取当前用户下所有购物车商品条目列表
     *          1.从session中获取当前登陆User对象
     *          2.判断User对象是否为空
     *              是：返回错误信息，强制用户登陆
     *          3.调用Service层接口，获取当前用户下所有购物车商品条目列表
     * @param session   Session对象
     * @return
     */
    @RequestMapping(value = "list.do",method = RequestMethod.POST)
    @ResponseBody
    public ServerResponse<CartVo> list(HttpSession session){
        User user=(User)session.getAttribute(Const.CURRENT_USER);
        if(user == null){
            return ServerResponse.createByErrorCodeMsg(ResponseCode.ILLEGAL_ARGUMENT.getCode(),ResponseCode.ILLEGAL_ARGUMENT.getDesc());
        }
        return iCartService.list(user.getId());
    }

    /**
     * 对购物车中商品条目进行勾选：全选
     *          1.从session中获取当前登陆User对象
     *          2.判断User对象是否为空
     *              是：返回错误信息，强制用户登陆
     *          3.调用Service层接口，对购物车中所有商品条目进行勾选
     * @param session   Session对象
     * @return
     */
    @RequestMapping(value = "select_all.do",method = RequestMethod.POST)
    @ResponseBody
    public ServerResponse<CartVo> selectAll(HttpSession session){
        User user=(User)session.getAttribute(Const.CURRENT_USER);
        if(user == null){
            return ServerResponse.createByErrorCodeMsg(ResponseCode.ILLEGAL_ARGUMENT.getCode(),ResponseCode.ILLEGAL_ARGUMENT.getDesc());
        }
        return iCartService.selectOrUnSelect(user.getId(),null, ProductStatus.CHECKED.getCode());
    }

    /**
     * 对购物车中商品条目进行勾选：全不选
     *          1.从session中获取当前登陆User对象
     *          2.判断User对象是否为空
     *              是：返回错误信息，强制用户登陆
     *          3.调用Service层接口，对购物车中所有商品条目进行不勾选
     * @param session   Session对象
     * @return
     */
    @RequestMapping(value = "unSelect_all.do",method = RequestMethod.POST)
    @ResponseBody
    public ServerResponse<CartVo> unSelectAll(HttpSession session){
        User user=(User)session.getAttribute(Const.CURRENT_USER);
        if(user == null){
            return ServerResponse.createByErrorCodeMsg(ResponseCode.ILLEGAL_ARGUMENT.getCode(),ResponseCode.ILLEGAL_ARGUMENT.getDesc());
        }
        return iCartService.selectOrUnSelect(user.getId(),null, ProductStatus.UN_CHECKED.getCode());
    }

    /**
     * 对购物车中商品条目进行勾选：单选
     *          1.从session中获取当前登陆User对象
     *          2.判断User对象是否为空
     *              是：返回错误信息，强制用户登陆
     *          3.调用Service层接口，对购物车中商品Id条目进行勾选
     * @param session   Session对象
     * @param productId     商品Id
     * @return
     */
    @RequestMapping(value = "select_one.do",method = RequestMethod.POST)
    @ResponseBody
    public ServerResponse<CartVo> selectOne(HttpSession session,Integer productId){
        User user=(User)session.getAttribute(Const.CURRENT_USER);
        if(user == null){
            return ServerResponse.createByErrorCodeMsg(ResponseCode.ILLEGAL_ARGUMENT.getCode(),ResponseCode.ILLEGAL_ARGUMENT.getDesc());
        }
        return iCartService.selectOrUnSelect(user.getId(),productId, ProductStatus.CHECKED.getCode());
    }

    /**
     * 对购物车中商品条目进行勾选：单不选
     *          1.从session中获取当前登陆User对象
     *          2.判断User对象是否为空
     *              是：返回错误信息，强制用户登陆
     *          3.调用Service层接口，对购物车中商品Id条目进行不勾选
     * @param session   Session对象
     * @param productId     商品Id
     * @return
     */
    @RequestMapping(value = "unSelect_one.do",method = RequestMethod.POST)
    @ResponseBody
    public ServerResponse<CartVo> unSelectOne(HttpSession session,Integer productId){
        User user=(User)session.getAttribute(Const.CURRENT_USER);
        if(user == null){
            return ServerResponse.createByErrorCodeMsg(ResponseCode.ILLEGAL_ARGUMENT.getCode(),ResponseCode.ILLEGAL_ARGUMENT.getDesc());
        }
        return iCartService.selectOrUnSelect(user.getId(),productId, ProductStatus.UN_CHECKED.getCode());
    }

    /**
     * 获取当前用户购物车中所有商品的购买个数
     *          1.从session中获取当前登陆User对象
     *          2.判断User对象是否为空
     *              是：返回0
     *          3.调用Service层接口，获取当前用户购物车中所有商品的购买个数
     * @param session   Session对象
     * @return
     */
    @RequestMapping(value = "get_cart_product_count.do",method = RequestMethod.POST)
    @ResponseBody
    public ServerResponse<Integer>getCartProductCount(HttpSession session){
        User user=(User)session.getAttribute(Const.CURRENT_USER);
        if(user == null){
            return ServerResponse.createBySuccess(0);
        }
        return iCartService.getCartProductCount(user.getId());
    }

}
