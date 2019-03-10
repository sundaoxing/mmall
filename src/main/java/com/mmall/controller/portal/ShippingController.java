package com.mmall.controller.portal;

import com.mmall.common.Const;
import com.mmall.common.ResponseCode;
import com.mmall.common.ServerResponse;
import com.mmall.pojo.Shipping;
import com.mmall.pojo.User;
import com.mmall.service.IShippingService;
import org.apache.ibatis.annotations.Param;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpSession;
/*
        收货地址接口：
            Controller层：负责与前端进行通信
 */
@Controller
@RequestMapping("/shipping/")
public class ShippingController {

    @Autowired
    private IShippingService iShippingService;

    /**
     * 添加收货地址
     *          1.从session中获取当前登陆User对象
     *          2.判断User对象是否为空
     *              是：返回错误信息，强制用户登陆
     *          3.调用Service层接口，添加收货地址
     * @param session   Session对象
     * @param shipping  地址Shipping对象
     * @return
     */
    @RequestMapping(value = "add.do",method = RequestMethod.POST)
    @ResponseBody
    public ServerResponse add(HttpSession session , Shipping shipping){
        User user = (User) session.getAttribute(Const.CURRENT_USER);
        if(user == null){
            return ServerResponse.createByErrorCodeMsg(ResponseCode.NEED_LOGIN.getCode(),ResponseCode.NEED_LOGIN.getDesc());
        }
        return iShippingService.add(user.getId(),shipping);
    }

    /**
     * 删除收货地址
     *          1.从session中获取当前登陆User对象
     *          2.判断User对象是否为空
     *              是：返回错误信息，强制用户登陆
     *          3.调用Service层接口，删除收货地址
     * @param session       Session对象
     * @param shippingId    地址Id
     * @return
     */
    @RequestMapping(value = "delete.do",method = RequestMethod.POST)
    @ResponseBody
    public ServerResponse delete(HttpSession session , Integer shippingId){
        User user = (User) session.getAttribute(Const.CURRENT_USER);
        if(user == null){
            return ServerResponse.createByErrorCodeMsg(ResponseCode.NEED_LOGIN.getCode(),ResponseCode.NEED_LOGIN.getDesc());
        }
        return iShippingService.delete(user.getId(),shippingId);
    }

    /**
     * 更新收货地址
     *          1.从session中获取当前登陆User对象
     *          2.判断User对象是否为空
     *              是：返回错误信息，强制用户登陆
     *          3.调用Service层接口，更新收货地址
     * @param session   Session对象
     * @param shipping  地址Shipping对象
     * @return
     */
    @RequestMapping(value = "update.do",method = RequestMethod.POST)
    @ResponseBody
    public ServerResponse update(HttpSession session , Shipping shipping){
        User user = (User) session.getAttribute(Const.CURRENT_USER);
        if(user == null){
            return ServerResponse.createByErrorCodeMsg(ResponseCode.NEED_LOGIN.getCode(),ResponseCode.NEED_LOGIN.getDesc());
        }
        return iShippingService.update(user.getId(),shipping);
    }

    /**
     * 查询收货地址详细信息
     *          1.从session中获取当前登陆User对象
     *          2.判断User对象是否为空
     *              是：返回错误信息，强制用户登陆
     *          3.调用Service层接口，查询收货地址
     * @param session       Session对象
     * @param shippingId    地址Id
     * @return
     */
    @RequestMapping(value = "select.do",method = RequestMethod.POST)
    @ResponseBody
    public ServerResponse select(HttpSession session , Integer shippingId){
        User user = (User) session.getAttribute(Const.CURRENT_USER);
        if(user == null){
            return ServerResponse.createByErrorCodeMsg(ResponseCode.NEED_LOGIN.getCode(),ResponseCode.NEED_LOGIN.getDesc());
        }
        return iShippingService.select(user.getId(),shippingId);
    }

    /**
     * 获取当前用户的收货地址List列表
     *          1.从session中获取当前登陆User对象
     *          2.判断User对象是否为空
     *              是：返回错误信息，强制用户登陆
     *          3.调用Service层接口，获取当前用户的收货地址List列表
     * @param pageNum       第几页，默认第1页
     * @param pageSize      页面大小，每页包含多少条地址信息，默认值10
     * @param session       Session对象
     * @return
     */
    @RequestMapping(value = "list.do",method = RequestMethod.POST)
    @ResponseBody
    public ServerResponse list(@RequestParam(value = "pageNum",defaultValue = "1")Integer pageNum,
                               @RequestParam(value = "pageSize",defaultValue = "10")Integer pageSize,
                               HttpSession session){
        User user = (User) session.getAttribute(Const.CURRENT_USER);
        if(user == null){
            return ServerResponse.createByErrorCodeMsg(ResponseCode.NEED_LOGIN.getCode(),ResponseCode.NEED_LOGIN.getDesc());
        }
        return iShippingService.list(pageNum,pageSize,user.getId());
    }
}
