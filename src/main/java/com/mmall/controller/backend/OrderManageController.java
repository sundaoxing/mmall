package com.mmall.controller.backend;

import com.mmall.common.Const;
import com.mmall.common.ResponseCode;
import com.mmall.common.ServerResponse;
import com.mmall.pojo.User;
import com.mmall.service.IOrderService;
import com.mmall.service.IUserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpSession;
/*
        后台模块：订单接口
 */
@Controller
@RequestMapping("/manage/order")
public class OrderManageController {

    @Autowired
    private IOrderService iOrderService;
    @Autowired
    private IUserService iUserService;

    /**
     * 管理员操作：获取订单列表
     *              1.从session中获取当前登陆User对象
     *              2.判断User对象是否为空
     *                  是：返回错误信息，强制用户登陆
     *              3.调用service层接口，校验该User对象的角色是否为管理员
     *                  是：调用service层接口，获取订单列表
     *              4.返回无权限信息给前端
     * @param session   Session对象
     * @param pageNum   第几页，默认第1页
     * @param pageSize  分页大小，每页显示多少条订单信息，默认10
     * @return
     */
    @RequestMapping(value = "list.do",method = RequestMethod.POST)
    @ResponseBody
    public ServerResponse orderList(HttpSession session,
                                    @RequestParam(value = "pageNum",defaultValue = "1")Integer pageNum,
                                    @RequestParam(value = "pageSize",defaultValue = "10")Integer pageSize){
        User user = (User) session.getAttribute(Const.CURRENT_USER);
        if(user == null){
            return ServerResponse.createByErrorCodeMsg(ResponseCode.NEED_LOGIN.getCode(),"未登录，请登录");
        }
        if(iUserService.checkAdminRole(user).isSuccess()){
            return iOrderService.manageOrderList(pageNum,pageSize);
        }
        return ServerResponse.createByErrorMsg("无权限");
    }


    /**
     * 管理员操作：获取订单详情
     *              1.从session中获取当前登陆User对象
     *              2.判断User对象是否为空
     *                  是：返回错误信息，强制用户登陆
     *              3.调用service层接口，校验该User对象的角色是否为管理员
     *                  是：调用service层接口，获取订单详情
     *              4.返回无权限信息给前端
     * @param session   Session对象
     * @param orderNo   订单号
     * @return
     */
    @RequestMapping(value = "detail.do",method = RequestMethod.POST)
    @ResponseBody
    public ServerResponse orderDetail(HttpSession session,Long orderNo){
        User user = (User) session.getAttribute(Const.CURRENT_USER);
        if(user == null){
            return ServerResponse.createByErrorCodeMsg(ResponseCode.NEED_LOGIN.getCode(),"未登录，请登录");
        }
        if(iUserService.checkAdminRole(user).isSuccess()){
            return iOrderService.manageOrderDetail(orderNo);
        }
        return ServerResponse.createByErrorMsg("无权限");
    }

    /**
     * 管理员操作：订单查询：按订单号精确查询，后期可扩展为按关键字进行模糊查询
     *              1.从session中获取当前登陆User对象
     *              2.判断User对象是否为空
     *                  是：返回错误信息，强制用户登陆
     *              3.调用service层接口，校验该User对象的角色是否为管理员
     *                  是：调用service层接口，查询订单
     *              4.返回无权限信息给前端
     * @param session   Session对象
     * @param orderNo   订单号
     * @param pageNum   第几页，默认第1页
     * @param pageSize  分页大小，每页显示多少条订单信息，默认10
     * @return
     */
    @RequestMapping(value = "search.do",method = RequestMethod.POST)
    @ResponseBody
    public ServerResponse orderSearch(HttpSession session,Long orderNo,
                                    @RequestParam(value = "pageNum",defaultValue = "1")Integer pageNum,
                                    @RequestParam(value = "pageSize",defaultValue = "10")Integer pageSize){
        User user = (User) session.getAttribute(Const.CURRENT_USER);
        if(user == null){
            return ServerResponse.createByErrorCodeMsg(ResponseCode.NEED_LOGIN.getCode(),"未登录，请登录");
        }
        if(iUserService.checkAdminRole(user).isSuccess()){
            return iOrderService.manageOrderSearch(orderNo,pageNum,pageSize);
        }
        return ServerResponse.createByErrorMsg("无权限");
    }

    /**
     * 管理员操作：发货
     *          1.从session中获取当前登陆User对象
     *              2.判断User对象是否为空
     *                  是：返回错误信息，强制用户登陆
     *              3.调用service层接口，校验该User对象的角色是否为管理员
     *                  是：调用service层接口，获发货
     *              4.返回无权限信息给前端
     * @param session   Session对象
     * @param orderNo   订单号
     * @return
     */
    @RequestMapping(value = "send_goods.do",method = RequestMethod.POST)
    @ResponseBody
    public ServerResponse orderSendGoods(HttpSession session,Long orderNo){
        User user = (User) session.getAttribute(Const.CURRENT_USER);
        if(user == null){
            return ServerResponse.createByErrorCodeMsg(ResponseCode.NEED_LOGIN.getCode(),"未登录，请登录");
        }
        if(iUserService.checkAdminRole(user).isSuccess()){
            return iOrderService.manageOrderSendGoods(orderNo);
        }
        return ServerResponse.createByErrorMsg("无权限");
    }

}