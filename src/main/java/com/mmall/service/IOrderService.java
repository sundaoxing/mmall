package com.mmall.service;

import com.mmall.common.ServerResponse;

import javax.servlet.http.HttpServletRequest;
import java.util.Map;

public interface IOrderService {
//    前台
    ServerResponse createOrder(Integer userId,Integer shippingId);
    ServerResponse cancel(Integer userId,Long orderNo);
    ServerResponse getOrderCartProduct(Integer userId);
    ServerResponse orderDetail(Integer userId,Long orderNo);
    ServerResponse orderList(Integer userId,Integer pageNum,Integer pageSize);

//    支付接口
    ServerResponse pay(Long orderNo,Integer userId,String path);
    ServerResponse alipayCallback(HttpServletRequest request);
    ServerResponse queryOrderPayStatus(Integer userId,Long orderNo);

//    后台
    ServerResponse manageOrderList(Integer pageNum,Integer pageSize);
    ServerResponse manageOrderDetail(Long orderNo);
    ServerResponse manageOrderSearch(Long orderNo,Integer pageNum,Integer pageSize);
    ServerResponse manageOrderSendGoods(Long orderNo);
}
