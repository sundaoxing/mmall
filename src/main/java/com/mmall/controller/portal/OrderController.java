package com.mmall.controller.portal;

import com.google.common.collect.Maps;
import com.mmall.common.Const;
import com.mmall.common.ResponseCode;
import com.mmall.common.ServerResponse;
import com.mmall.pojo.User;
import com.mmall.service.IOrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.util.Iterator;
import java.util.Map;
/*
        前台模块：订单接口
 */
@Controller
@RequestMapping("/order/")
public class OrderController {
    @Autowired
    private IOrderService iOrderService;

    /**
     * 创建订单：
     *      1.从session中获取当前登陆用户User对象
     *      2.判断该user对象是否为空
     *          是：返回错误信息，强制用户登陆
     *      3.调用Service层接口，创建订单
     * @param session       Session对象
     * @param shippingId    收货地址ShippingId
     * @return
     */
    @RequestMapping(value = "create.do",method = RequestMethod.POST)
    @ResponseBody
    public ServerResponse create(HttpSession session ,Integer shippingId){
        User user = (User) session.getAttribute(Const.CURRENT_USER);
        if(user == null){
            return ServerResponse.createByErrorCodeMsg(ResponseCode.NEED_LOGIN.getCode(),ResponseCode.NEED_LOGIN.getDesc());
        }
        return iOrderService.createOrder(user.getId(),shippingId);
    }

    /**
     * 取消订单
     *      1.从session中获取当前登陆用户User对象
     *      2.判断该user对象是否为空
     *          是：返回错误信息，强制用户登陆
     *      3.调用Service层接口，取消订单
     * @param session   Session对象
     * @param orderNo   订单号
     * @return
     */
    @RequestMapping(value = "cancel.do",method = RequestMethod.POST)
    @ResponseBody
    public ServerResponse cancel(HttpSession session ,Long orderNo){
        User user = (User) session.getAttribute(Const.CURRENT_USER);
        if(user == null){
            return ServerResponse.createByErrorCodeMsg(ResponseCode.NEED_LOGIN.getCode(),ResponseCode.NEED_LOGIN.getDesc());
        }
        return iOrderService.cancel(user.getId(),orderNo);
    }

    /**
     * 获取订单中商品信息
     *      1.从session中获取当前登陆用户User对象
     *      2.判断该user对象是否为空
     *          是：返回错误信息，强制用户登陆
     *      3.调用Service层接口，获取订单中商品信息
     * @param session   Session对象
     * @return
     */
    @RequestMapping(value = "get_order_cart_product.do",method = RequestMethod.POST)
    @ResponseBody
    public ServerResponse getOrderCartProduct(HttpSession session){
        User user = (User) session.getAttribute(Const.CURRENT_USER);
        if(user == null){
            return ServerResponse.createByErrorCodeMsg(ResponseCode.NEED_LOGIN.getCode(),ResponseCode.NEED_LOGIN.getDesc());
        }
        return iOrderService.getOrderCartProduct(user.getId());
    }

    /**
     * 获取订单详情
     *      1.从session中获取当前登陆用户User对象
     *      2.判断该user对象是否为空
     *          是：返回错误信息，强制用户登陆
     *      3.调用Service层接口，获取订单详情
     * @param session   Session对象
     * @param orderNo   订单号
     * @return
     */
    @RequestMapping(value = "detail.do",method = RequestMethod.POST)
    @ResponseBody
    public ServerResponse orderDetail(HttpSession session,Long orderNo){
        User user = (User) session.getAttribute(Const.CURRENT_USER);
        if(user == null){
            return ServerResponse.createByErrorCodeMsg(ResponseCode.NEED_LOGIN.getCode(),ResponseCode.NEED_LOGIN.getDesc());
        }
        return iOrderService.orderDetail(user.getId(),orderNo);
    }


    /**
     * 获取订单列表
     *      1.从session中获取当前登陆用户User对象
     *      2.判断该user对象是否为空
     *          是：返回错误信息，强制用户登陆
     *      3.调用Service层接口，获取订单列表
     * @param session   Session对象
     * @param pageNum   第几页，默认第1页
     * @param pageSize  每页大小，每页显示多少条订单信息，默认10
     * @return
     */
    @RequestMapping(value = "list.do",method = RequestMethod.POST)
    @ResponseBody
    public ServerResponse orderList(HttpSession session,
                                    @RequestParam(value = "pageNum",defaultValue = "1")Integer pageNum,
                                    @RequestParam(value = "pageSize",defaultValue = "10")Integer pageSize){
        User user = (User) session.getAttribute(Const.CURRENT_USER);
        if(user == null){
            return ServerResponse.createByErrorCodeMsg(ResponseCode.NEED_LOGIN.getCode(),ResponseCode.NEED_LOGIN.getDesc());
        }
        return iOrderService.orderList(user.getId(),pageNum,pageSize);
    }
    /*
                        支付接口
     */
    /**
     * 付款：
     *      1.从session中获取当前登陆用户
     *      2.判断当前用户User对象是否为空
     *          是：返回错误信息，强制用户登陆
     *      3.获取付款二维码存放路径（本地）
     *      4.调用Service层接口，扫二维码支付
     * @param session   Session对象
     * @param request   HttpServletRequest对象（包含支付宝成功处理订单后的返回信息）
     * @param orderNo   订单号
     * @return
     */
    @RequestMapping(value = "pay.do",method = RequestMethod.POST)
    @ResponseBody
    public ServerResponse pay(HttpSession session , HttpServletRequest request,Long orderNo){
        User user = (User) session.getAttribute(Const.CURRENT_USER);
        if(user == null){
            return ServerResponse.createByErrorCodeMsg(ResponseCode.NEED_LOGIN.getCode(),ResponseCode.NEED_LOGIN.getDesc());
        }
        String path = request.getSession().getServletContext().getRealPath("upload");
        return iOrderService.pay(orderNo,user.getId(),path);
    }

    /**
     * 支付宝回调：
     *      1.调用Service层接口，进行支付宝回调校验
     *      2.判断支付宝校验结果是否成功
     *          是：返回"success"字符串给支付宝
     *          否: 返回"failed"字符串给支付宝
     * @param request   HttpServletRequest对象（包含支付宝成功处理订单后的返回信息）
     * @return
     */
    @RequestMapping("alipay_callback.do")
    @ResponseBody
    public String alipayCallback(HttpServletRequest request){
        ServerResponse serverResponse = iOrderService.alipayCallback(request);
        if(serverResponse.isSuccess()){
            return Const.AlipayCallback.RESPONSE_SUCCESS;
        }
        return Const.AlipayCallback.RESPONSE_FAILED;
    }

    /**
     * 获取订单支付状态
     *      1.从session中获取当前登陆用户
     *      2.判断当前用户User对象是否为空
     *          是：返回错误信息，强制用户登陆
     *      3.调用Service层接口，获取订单支付状态结果
     *      4.判断支付是否已经支付
     *          是：返回true
     *          否：返回false
     * @param session   Session对象
     * @param orderNo   订单号
     * @return
     */
    @RequestMapping(value = "query_orderPay_status.do",method = RequestMethod.POST)
    @ResponseBody
    public ServerResponse<Boolean> queryOrderPayStatus(HttpSession session ,Long orderNo){
        User user = (User) session.getAttribute(Const.CURRENT_USER);
        if(user == null){
            return ServerResponse.createByErrorCodeMsg(ResponseCode.NEED_LOGIN.getCode(),ResponseCode.NEED_LOGIN.getDesc());
        }
        ServerResponse serverResponse =iOrderService.queryOrderPayStatus(user.getId(),orderNo);
        if(serverResponse.isSuccess()){
            return ServerResponse.createBySuccess(true);
        }
        return ServerResponse.createBySuccess(false);
    }
}
