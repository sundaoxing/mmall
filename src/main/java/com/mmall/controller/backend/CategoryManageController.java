package com.mmall.controller.backend;

import com.mmall.common.Const;
import com.mmall.common.ResponseCode;
import com.mmall.common.ServerResponse;
import com.mmall.pojo.Category;
import com.mmall.pojo.User;
import com.mmall.service.ICategoryService;
import com.mmall.service.IUserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpSession;
import java.util.List;
import java.util.Set;
/*
            后台模块：分类接口
 */
@Controller
@RequestMapping("/manage/category")
public class CategoryManageController {

    @Autowired
    private ICategoryService iCategoryService;

    @Autowired
    private IUserService iUserService;

    /**
     * 管理员操作：添加商品分类，默认分类为根分类（0）
     *              1.从session中获取当前登陆User对象
     *              2.判断User对象是否为空
     *              3.调用service层接口，校验该User对象的角色是否为管理员
     *                  是：调用service层接口，将分类名称，父分类Id添加到数据库中
     *              4.返回无权限信息给前端
     * @param session       Session对象
     * @param categoryName 分类名称
     * @param parentId     父分类Id
     * @return
     */
    @RequestMapping(value = "add_category.do",method = RequestMethod.POST)
    @ResponseBody
    public ServerResponse<String> addCategory(HttpSession session,String categoryName,@RequestParam(value = "parentId",defaultValue = "0") Integer parentId){
        User user =(User) session.getAttribute(Const.CURRENT_USER);
        if(user == null){
            return ServerResponse.createByErrorCodeMsg(ResponseCode.NEED_LOGIN.getCode(),"用户未登录，请登陆");
        }
        if(iUserService.checkAdminRole(user).isSuccess()){
            return iCategoryService.addCategory(categoryName,parentId);
        }
        return ServerResponse.createByErrorMsg("无权限,请用管理员");
    }

    /**
     * 管理员操作：修改分类名字
     *              1.从session中获取当前登陆User对象
     *              2.判断User对象是否为空
     *              3.调用service层接口，校验该User对象的角色是否为管理员
     *                  是：调用service层接口，更新分类名称，父分类Id到数据库中
     *              4.返回无权限信息给前端
     * @param session        Session对象
     * @param categoryId    分类Id
     * @param categoryName  分类名称
     * @return
     */
    @RequestMapping(value = "update_category_name.do",method = RequestMethod.POST)
    @ResponseBody
    public ServerResponse<String> updateCategoryName(HttpSession session,Integer categoryId,String categoryName){
        User user =(User) session.getAttribute(Const.CURRENT_USER);
        if(user == null){
            return ServerResponse.createByErrorCodeMsg(ResponseCode.NEED_LOGIN.getCode(),"用户未登录，请登陆");
        }
        if(iUserService.checkAdminRole(user).isSuccess()) {
            return iCategoryService.updateCategoryName(categoryId,categoryName);
        }
        return ServerResponse.createByErrorMsg("无权限,请用管理员");
    }

    /**
     * 管理员操作：获取当前分类的所有一级子分类（不递归），只有一层
     *              1.从session中获取当前登陆User对象
     *              2.判断User对象是否为空
     *              3.调用service层接口，校验该User对象的角色是否为管理员
     *                  是：调用service层接口，获取当前分类的所有子分类
     *              4.返回无权限信息给前端
     * @param session       Session对象
     * @param categoryId    分类Id
     * @return
     */
    @RequestMapping(value = "get_child_parallel_category.do",method = RequestMethod.POST)
    @ResponseBody
    public ServerResponse<List<Category>> getChildParallelCategory(HttpSession session, @RequestParam(value = "categoryId",defaultValue = "0") Integer categoryId){
        User user =(User) session.getAttribute(Const.CURRENT_USER);
        if(user == null){
            return ServerResponse.createByErrorCodeMsg(ResponseCode.NEED_LOGIN.getCode(),"用户未登录，请登陆");
        }
        if(iUserService.checkAdminRole(user).isSuccess()) {
            return iCategoryService.getChildParallelCategory(categoryId);
        }
        return ServerResponse.createByErrorMsg("无权限,请用管理员");
    }

    /**
     * 管理员操作：获取当前分类和其子分类
     *             1.从session中获取当前登陆User对象
     *             2.判断User对象是否为空
     *             3.调用service层接口，校验该User对象的角色是否为管理员
     *                  是：调用service层接口，获取当前分类和其所有子分类
     *             4.返回无权限信息给前端
     * @param session       Session对象
     * @param categoryId    分类Id
     * @return
     */
    @RequestMapping(value = "get_current_and_child_category.do",method = RequestMethod.POST)
    @ResponseBody
    public ServerResponse<Set<Integer>> getCurrentAndChildCategoryId(HttpSession session, @RequestParam(value = "categoryId",defaultValue = "0") Integer categoryId){
        User user =(User) session.getAttribute(Const.CURRENT_USER);
        if(user == null){
            return ServerResponse.createByErrorCodeMsg(ResponseCode.NEED_LOGIN.getCode(),"用户未登录，请登陆");
        }
        if(iUserService.checkAdminRole(user).isSuccess()) {
            return iCategoryService.getCurrentAndChildCategoryId(categoryId);
        }
        return ServerResponse.createByErrorMsg("无权限,请用管理员");
    }
}
