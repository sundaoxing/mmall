package com.mmall.service;

import com.mmall.common.ServerResponse;
import com.mmall.pojo.Shipping;
/*
        Service接口
 */
public interface IShippingService {
    ServerResponse add(Integer userId, Shipping shipping);
    ServerResponse delete(Integer userId, Integer shippingId);
    ServerResponse update(Integer userId, Shipping shipping);
    ServerResponse select(Integer userId, Integer shippingId);
    ServerResponse list(Integer pageNum,Integer pageSize,Integer userId);
}
