package com.mmall.service;

import com.mmall.common.ServerResponse;
import com.mmall.pojo.User;

/*
            service层接口
 */
public interface IUserService {
    ServerResponse<User> login(String username, String password);
    ServerResponse<String> register(User user);
    ServerResponse<String> checkValid(String name_email,String type);
    ServerResponse<String> selectQuestion(String username);
    ServerResponse<String> checkAnswer(String username,String question,String answer);
    ServerResponse<String> forgetResetPassword(String username,String passwordNew,String forgetToken);
    ServerResponse<String> resetPassword(String passwordOld,String passwordNew,User user);
    ServerResponse<User> updateUserInfo(User user);
    ServerResponse<User> getInformation(Integer userId);
    ServerResponse<String> checkAdminRole(User user);
}
