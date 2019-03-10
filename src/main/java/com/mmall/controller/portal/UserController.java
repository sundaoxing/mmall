package com.mmall.controller.portal;

import com.mmall.common.Const;
import com.mmall.common.ResponseCode;
import com.mmall.common.ServerResponse;
import com.mmall.pojo.User;
import com.mmall.service.IUserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpSession;
/*
            前台模块（用户接口）
            controller层(相当于对servlet层的进一步封装)
 */
//添加spring Controller注解，使UserController具有Controller的功能（类似Servlet）
@Controller
//在UserController类上定义url的根路径的前缀
@RequestMapping("/user/")
public class UserController {
    //    通过Spring Scan扫描包的形式，注入iUserService对象
    @Autowired
    private IUserService iUserService;

    /**
     * 用户登陆
     *              1.调用业务层接口，登陆
     *              2.判断登陆是否成功
     *                  是：将用户信息返回给前端
     *                  否：将登陆失败信息返回给前端
     * @param username 用户名
     * @param password 密码
     * @param session
     * @return
     */
    //定义url根路径的访问的后缀
    @RequestMapping(value = "login.do", method = RequestMethod.POST )
    @ResponseBody//使用spring MVC插件jackson自动将返回值序列化成json格式
    public ServerResponse<User> login(String username, String password , HttpSession session){
        ServerResponse<User> response =iUserService.login(username,password);
        if(response.isSuccess()){
            session.setAttribute(Const.CURRENT_USER,response.getData());
        }
        return response;
    }

    /**
     * 用户登出
     *              1.从session中移除当前用户
     *              2.将登陆成功信息返回给前端
     * @param session
     * @return
     */
    @RequestMapping(value = "logout.do",method = RequestMethod.POST )
    @ResponseBody
    public ServerResponse<String> logout(HttpSession session){
        session.removeAttribute(Const.CURRENT_USER);
        return ServerResponse.createBySuccess();
    }

    /**
     * 用户注册
     *               1.调用业务层接口，注册
     * @param user 用户对象
     * @return
     */
    @RequestMapping(value = "register.do",method = RequestMethod.POST )
    @ResponseBody
    public ServerResponse<String> register(User user){
        return iUserService.register(user);
    }

    /**
     * 校验用户名和邮箱是否正确
     *               1.调用业务层接口，校验用户名和邮箱
     * @param name_email 用户名或者邮箱
     * @param type       参数类型
     * @return
     */
    @RequestMapping(value = "check_valid.do",method = RequestMethod.POST )
    @ResponseBody
    public ServerResponse<String> checkValid(String name_email,String type){
        return iUserService.checkValid(name_email,type);
    }

    /**
     * 登陆——从session中获取当前登陆用户信息
     *              1.从session中获取当前用户User对象
     *              2.判断User对象是否存在
     *                  是：将User对象返回给前端
     *                  否：将未登陆信息返回给前端
     * @param session
     * @return
     */
    @RequestMapping(value = "get_user_info.do",method = RequestMethod.POST )
    @ResponseBody
    public ServerResponse<User> getUserInfo(HttpSession session){
        User user = (User) session.getAttribute(Const.CURRENT_USER);
        if(user != null){
            return ServerResponse.createBySuccess(user);
        }
        return ServerResponse.createByErrorMsg("用户未登录,请先登陆");
    }

    /**
     * 忘记密码——根据用户名获取找回密码的问题
     *              1.调用业务层接口，查询问题
     * @param username 用户名
     * @return
     */
    @RequestMapping(value = "forget_get_question.do",method = RequestMethod.POST )
    @ResponseBody
    public ServerResponse<String> forgetGetQuestion(String username){
        return iUserService.selectQuestion(username);
    }

    /**
     * 忘记密码——根据用户名，问题，问题答案校验问题回答是否正确
     *               1.调用业务层接口，校验问题的答案
     * @param username 用户名
     * @param question 问题
     * @param answer   答案
     * @return
     */
    @RequestMapping(value = "forget_check_answer.do",method = RequestMethod.POST )
    @ResponseBody
    public ServerResponse<String> forgetCheckAnswer(String username,String question,String answer){
        return iUserService.checkAnswer(username,question,answer);
    }

    /**
     * 忘记密码——根据forgetCheckAnswer得到的正确的token重置密码
     *                1.调用业务层接口，重设密码
     * @param username      用户名
     * @param passwordNew   新密码
     * @param forgetToken   token字段
     * @return
     */
    @RequestMapping(value = "forget_reset_password.do",method = RequestMethod.POST )
    @ResponseBody
    public ServerResponse<String> forgetResetPassword(String username,String passwordNew,String forgetToken){
        return iUserService.forgetResetPassword(username,passwordNew,forgetToken);
    }

    /**
     * 登陆状态下——重置密码
     *                 1.从session中获取当前用户
     *                 2.判断该用户是否登陆
     *                      是：调用业务层：重设密码
     *                      否：返回用户未登录信息给前端
     * @param session
     * @param passwordOld   旧密码
     * @param passwordNew   新密码
     * @return
     */
    @RequestMapping(value = "reset_password.do",method = RequestMethod.POST )
    @ResponseBody
    public ServerResponse<String> resetPassword(HttpSession session,String passwordOld,String passwordNew){
        User user = (User) session.getAttribute(Const.CURRENT_USER);
        if(user == null){
            return ServerResponse.createByErrorMsg("用户未登录");
        }
        return iUserService.resetPassword(passwordOld,passwordNew,user);
    }

    /**
     * 登陆状态下——更新用户信息
     *                1. 从session中获取当前用户
     *                2.判断该用户是否登陆
     *                      否：返回用户未登录信息给前端
     *                      是：1.将当前用户的Id设置给用户更新的User对象中
     *                          2.将当前用户的username设置给用户更新的User对象中
     *                          3.调用业务层：更新用户信息
     *                          4.将更新结果返回给前端
     * @param session
     * @param user      用户对象
     * @return
     */
    @RequestMapping(value = "update_user_info.do",method = RequestMethod.POST )
    @ResponseBody
    public ServerResponse<User> updateUserInfo(HttpSession session,User user){
        User current_User = (User) session.getAttribute(Const.CURRENT_USER);
        if(user == null){
            return ServerResponse.createByErrorMsg("用户未登陆");
        }
        user.setId(current_User.getId());
        user.setUsername(current_User.getUsername());
        ServerResponse<User> response = iUserService.updateUserInfo(user);
        if(response.isSuccess()){
            session.setAttribute(Const.CURRENT_USER,response.getData());
        }
        return response;
    }

    /**
     * 登录状态下——获取用户信息：
     *                1.从session中获取当前用户
     *                2.判断当前用户是否登陆
     *                      是：调用业务层：获取用户信息
     *                      否：返回需要登陆状态码给前端
     * @param session
     * @return
     */
    @RequestMapping(value = "get_information.do",method = RequestMethod.POST )
    @ResponseBody
    public ServerResponse<User> getInformation(HttpSession session){
        User current_User = (User) session.getAttribute(Const.CURRENT_USER);
        if(current_User ==null){
            return ServerResponse.createByErrorCodeMsg(ResponseCode.NEED_LOGIN.getCode(),"未登录，请登陆");
        }
        return iUserService.getInformation(current_User.getId());
    }

}
