package com.mmall.service.impl;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.mmall.common.ServerResponse;
import com.mmall.dao.ShippingMapper;
import com.mmall.pojo.Shipping;
import com.mmall.service.IShippingService;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
/*
            Shipping ：  Service层：处理业务逻辑
 */
@Service("iShippingService")
public class ShippingServiceImpl implements IShippingService {

    @Autowired
    private ShippingMapper shippingMapper;

    /**
     * 业务1.添加收货地址
     *          1.Shipping对象的userId属性设置为从Session中获取的userId（防止横向越权）
     *          2.调用DAO层接口，添加新地址到数据库
     *          3.判断数据库插入结果
     *               成功：1.构造地址Id字段（key：value）->（shippingId：XX）
     *                     2.将shippingId返回给前端
     *               失败：返回失败信息
     * @param userId        用户Id
     * @param shipping      地址Shipping对象
     * @return
     */
    @Override
    public ServerResponse add(Integer userId, Shipping shipping) {
        shipping.setUserId(userId);
        int rowCount = shippingMapper.insert(shipping);
        if(rowCount >0){
            Map map = new HashMap();
            map.put("shippingId",shipping.getId());
            return ServerResponse.createBySuccess("添加地址成功",map);
        }
        return ServerResponse.createByErrorMsg("添加地址失败");
    }

    /**
     * 业务2：删除收货地址
     *          1.调用DAO层接口，根据用户Id，地址Id删除地址
     *          2.判断数据库插入结果
     *               成功：返回成功信息
     *               失败：返回失败信息
     * @param userId        用户Id
     * @param shippingId    地址Id
     * @return
     */
    @Override
    public ServerResponse delete(Integer userId, Integer shippingId) {
        int rowCount = shippingMapper.deleteByUserIdShippingId(userId,shippingId);
        if(rowCount >0){
            return ServerResponse.createBySuccessMsg("删除地址成功");
        }
        return ServerResponse.createByErrorMsg("删除地址失败");
    }

    /**
     * 业务3：更新收货地址
     *          1.Shipping对象的userId属性设置为从Session中获取的userId（防止横向越权）
     *          2.调用DAO层接口，更新新地址到数据库
     *          3.判断数据库更新结果
     *               成功：返回成功信息
     *               失败：返回失败信息
     * @param userId        用户Id
     * @param shipping      地址Shipping对象
     * @return
     */
    @Override
    public ServerResponse update(Integer userId, Shipping shipping) {
        shipping.setUserId(userId);
        int rowCount = shippingMapper.updateByUserIdShippingId(shipping);
        if(rowCount >0){
            return ServerResponse.createBySuccessMsg("修改地址成功");
        }
        return ServerResponse.createByErrorMsg("修改地址失败");
    }

    /**
     * 业务4：查询收货地址详细信息
     *          1.调用DAO层接口，根据用户Id，地址Id查询地址
     *          2.判断查询到的Shipping对象是否为空
     *               成功：返回成功信息和Shipping对象
     *               失败：返回失败信息
     * @param userId        用户Id
     * @param shippingId    地址Id
     * @return
     */
    @Override
    public ServerResponse select(Integer userId, Integer shippingId) {
        Shipping shipping = shippingMapper.selectByUserIdShippingId(userId,shippingId);
        if(shipping != null){
            return ServerResponse.createBySuccess("获取地址详情成功",shipping);
        }
        return ServerResponse.createByErrorMsg("获取地址详情失败");
    }

    /**
     * 获取当前用户的收货地址List列表
     *          1.使用PageHelper开始分页，设置分页个数，每页大小
     *          2.调用DAO层，查询当前用户的地址List列表
     *          3.判断该地址List列表是否不为空
     *              是：构造分页对象PageInfo，并返回给前端
     *              否：返回错误信息
     * @param pageNum       第几页，默认第1页
     * @param pageSize      页面大小，每页包含多少条地址信息，默认值10
     * @param userId        用户Id
     * @return
     */
    @Override
    public ServerResponse list(Integer pageNum,Integer pageSize,Integer userId) {
        PageHelper.startPage(pageNum,pageSize);
        List<Shipping> shippingList = shippingMapper.selectByUserId(userId);
        if(CollectionUtils.isNotEmpty(shippingList)){
            PageInfo pageInfo = new PageInfo(shippingList);
            return ServerResponse.createBySuccess(pageInfo);
        }
        return ServerResponse.createByErrorMsg("获取地址列表失败");
    }
}
