package com.mmall.service.impl;

import com.mmall.common.Const;
import com.mmall.common.ServerResponse;
import com.mmall.common.TokenCache;
import com.mmall.dao.UserMapper;
import com.mmall.pojo.User;
import com.mmall.service.IUserService;
import com.mmall.util.MD5Util;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.UUID;

/*
            service层接口实现类
 */
@Service("iUserService")//声明成service，向controller层注入（两者名字要保持一致）
public class UserServiceImpl implements IUserService {

//  通过Spring Scan扫描包的形式，注入UserMapper对象
    @Autowired
    private UserMapper userMapper;

    /**
     * 业务1：登陆
     *             1.校验用户名是否存在
     *             2.MD5加密密码
     *             3.校验用户名和加密后的密码是否正确
     *             4.将DAO层返回的User对象的password置空，
     *             5.将User对象响应给前端
     * @param username      用户名
     * @param password      密码
     * @return
     */
    @Override
    public ServerResponse<User> login(String username, String password) {
        int resultCount =userMapper.checkUsername(username);
        if(resultCount ==0){
            return ServerResponse.createByErrorMsg("用户不存在");
        }
//      因为password MD5 加密，这里也要加密才能进行判断
        String MD5_Password=MD5Util.MD5EncodeUtf8(password);
        User user = userMapper.selectLogin(username,MD5_Password);
        if(user == null){
            return ServerResponse.createByErrorMsg("密码错误");
        }
        user.setPassword(StringUtils.EMPTY);
        return ServerResponse.createBySuccess("登陆成功",user);
    }

    /**
     * 业务2：注册
     *             1.校验用户名是否已经存在
     *             2.校验邮箱是否已经存在
     *             3.设置用户角色值为0：默认注册用户只是普通用户
     *             4.MD5加密password
     *             5.将注册信息User对象插入到数据库中
     *             6.将注册结果响应给前端
     * @param user      用户对象
     * @return
     */
    @Override
    public ServerResponse<String> register(User user) {
        ServerResponse<String> validResponse =this.checkValid(user.getUsername(),Const.USERNAME);
        if(! validResponse.isSuccess()){
            return validResponse;
        }
        validResponse = this.checkValid(user.getEmail(),Const.EMAIL);
        if(! validResponse.isSuccess()){
            return validResponse;
        }
        user.setRole(Const.Role.ROLE_CUSTOMER);
//        MD5加密password
        user.setPassword(MD5Util.MD5EncodeUtf8(user.getPassword()));
        int resultCount = userMapper.insert(user);
        if(resultCount == 0){
            return ServerResponse.createByErrorMsg("注册失败");
        }
        return ServerResponse.createBySuccessMsg("注册成功");
    }

    /**
     * 业务3：校验：根据参数类型，判断是校验用户名还是邮箱
     *             1.判断参数类型是否是用户名：
     *                 是：校验用户名是否已经存在
     *             2.判断参数类型是否是邮箱：
     *                 是：校验邮箱是否已经存在
     * @param name_email    用户名/邮箱
     * @param type          参数类型
     * @return
     */
    @Override
    public ServerResponse<String> checkValid(String name_email,String type){
        if(StringUtils.isNotBlank(type)){
            if(Const.USERNAME.equals(type)){
                int resultCount=userMapper.checkUsername(name_email);
                if(resultCount >0){
                    return ServerResponse.createByErrorMsg("用户名已存在");
                }
            }
            if(Const.EMAIL.equals(type)){
                int resultCount=userMapper.checkEmail(name_email);
                if(resultCount >0){
                    return ServerResponse.createByErrorMsg("邮箱已存在");
                }
            }
        }else{
            return ServerResponse.createByErrorMsg("参数错误");
        }
        return ServerResponse.createBySuccessMsg("校验成功");
    }

    /**
     * 业务4：查询问题：用户可以根据自己设置的问题和答案在未登录的情况下，重设密码
     *             1.校验用户名是否存在
     *             2.根据用户名查询用户设置的问题
     *             3.将问题响应给前端
     * @param username      用户名
     * @return
     */
    @Override
    public ServerResponse<String> selectQuestion(String username) {
        int resultCount = userMapper.checkUsername(username);
        if(resultCount ==0){
            return ServerResponse.createByErrorMsg("用户不存在");
        }
        String question = userMapper.selectQuestionByUsername(username);
        if(StringUtils.isNotBlank(question)){
            return ServerResponse.createBySuccess(question);
        }
        return ServerResponse.createByErrorMsg("未设置找回密码的问题");
    }

    /**
     * 业务5：校验问题的答案：
     *              1.校验该用户的问题，答案是否正确
     *              2.设置Token（UUID）
     *              3.将token保存在guava本地缓存中（key-value对）
     *              4.将token响应给前端（前端再使用该token验证用户合法后，才能重设密码）
     * @param username      用户名
     * @param question      问题
     * @param answer        答案
     * @return
     */
    @Override
    public ServerResponse<String> checkAnswer(String username, String question, String answer) {
        int resultCount = userMapper.checkAnswer(username,question,answer);
        if(resultCount >0){//找回密码的问题和答案都是正确的
            String forgetToken = UUID.randomUUID().toString();
            TokenCache.setKey("token_"+username,forgetToken);
            return ServerResponse.createBySuccess(forgetToken);
        }
        return ServerResponse.createByErrorMsg("密码找回问题的答案错误");
    }

    /**
     * 业务6：重设密码：通过问题答案获取token来重设密码，不用登陆
     *              1.校验token是否为空
     *              2.校验用户名是否存在
     *              3.从guava本地缓存中获取token
     *              4.校验前端传来的token与本地token是否一致
     *                  是：MD5加密新密码
     *                     更新用户密码：根据用户名
     *                     将更新结果返回给前端
     *              5.将校验结果返回给前端
     * @param username      用户名
     * @param passwordNew   新密码
     * @param forgetToken   token
     * @return
     */
    @Override
    public ServerResponse<String> forgetResetPassword(String username, String passwordNew, String forgetToken) {
        if(StringUtils.isBlank(forgetToken)){
            return ServerResponse.createByErrorMsg("参数错误，token需要传递");
        }
        int resultCount=userMapper.checkUsername(username);
        if(resultCount ==0){
            return ServerResponse.createByErrorMsg("用户名不存在");
        }
        String token = TokenCache.getKey(TokenCache.TOKEN_PREFIX+username);
        if(StringUtils.isBlank(token)){
            return ServerResponse.createByErrorMsg("token无效或者过期");
        }
        if(StringUtils.equals(forgetToken,token)){
            String MD5passwordNew = MD5Util.MD5EncodeUtf8(passwordNew);
            int rowCount =userMapper.updatePasswordByUsername(username,MD5passwordNew);
            if(rowCount >0){
                return ServerResponse.createBySuccessMsg("修改密码成功");
            }
        }
        else{
            return ServerResponse.createByErrorMsg("token错误，请重新获取");
        }
        return ServerResponse.createByErrorMsg("密码重置失败");
    }

    /**
     * 业务7：重设密码：登录状态下
     *              1.校验旧密码是否属于当前用户
     *              2.User对象重置密码password
     *              3.更新User对象到数据库中
     *              4.将更新结果返回给前端
     * @param passwordOld   旧密码
     * @param passwordNew   新密码
     * @param user          用户对象
     * @return
     */
    @Override
    public ServerResponse<String> resetPassword(String passwordOld,String passwordNew,User user){
        /*
            防止横向越权
                需要校验旧密码是否属于当前用户
         */
        int resultCount = userMapper.checkPassword(MD5Util.MD5EncodeUtf8(passwordOld),user.getId());
        if(resultCount == 0){
            return ServerResponse.createByErrorMsg("旧密码错误");
        }
        user.setPassword(MD5Util.MD5EncodeUtf8(passwordNew));
        resultCount = userMapper.updateByPrimaryKeySelective(user);
        if(resultCount >0){
            return ServerResponse.createBySuccessMsg("密码更新成功");
        }
        return ServerResponse.createByErrorMsg("密码更新失败");
    }

    /**
     * 业务8：更新用户信息：登录状态下
     *              1.校验邮箱是否存在：根据新更新的邮箱，用户Id
     *              2.新建User对象，将用户更新的信息保存到User对象中
     *              3.更新User对象到数据库中
     *              4.将更新结果返回给前端
     * @param user      用户对象
     * @return
     */
    @Override
    public ServerResponse<User> updateUserInfo(User user) {
        /*
            校验邮箱
                如果用户修改邮箱，不能与其他用户重复
                不修改邮箱，则需要跳过自己已经设置的邮箱
         */
        int resultCount = userMapper.checkEmailByUserId(user.getEmail(),user.getId());
        if(resultCount >0){
            return ServerResponse.createByErrorMsg("邮箱已存在");
        }
        User update_User = new User();
        update_User.setId(user.getId());
        update_User.setEmail(user.getEmail());
        update_User.setPhone(user.getPhone());
        update_User.setQuestion(user.getQuestion());
        update_User.setAnswer(user.getAnswer());

        resultCount = userMapper.updateByPrimaryKeySelective(update_User);
        update_User.setUsername(user.getUsername());
        if(resultCount >0){
            return ServerResponse.createBySuccess("更新用户信息成功",update_User);
        }
        return ServerResponse.createByErrorMsg("更新用户信息失败");
    }

    /**
     * 业务9：获取用户信息：登陆状态下
     *              1.根据用户Id查询该用户信息
     *              2.判断此用户是否存在
     *              3.将此用户密码置空
     *              4.将User对象返回给前端
     * @param userId    用户Id
     * @return
     */
    @Override
    public ServerResponse<User> getInformation(Integer userId) {
        User user = userMapper.selectByPrimaryKey(userId);
        if(user == null){
            return ServerResponse.createByErrorMsg("没有此用户");
        }
        user.setPassword(StringUtils.EMPTY);
        return ServerResponse.createBySuccess(user);
    }

    /**
     * 业务10：校验管理员用户
     *              1.判断此用户是否存在
     *              2.判断此用户角色是否是管理员角色
     *              3.将判断结果返回给前端
     * @param user      用户对象
     * @return
     */
    @Override
    public ServerResponse<String> checkAdminRole(User user) {
        if(user !=null && user.getRole() == Const.Role.ROLE_ADMIN){
            return ServerResponse.createBySuccess();
        }
        return ServerResponse.createByError();
    }

}
