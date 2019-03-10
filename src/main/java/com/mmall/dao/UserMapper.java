package com.mmall.dao;

import com.mmall.pojo.User;
import org.apache.ibatis.annotations.Param;

/*
            Dao层：使用mybatis做持久化层
                使用xml文件做Dao实现类
 */
public interface UserMapper {
    //自动生成
    int deleteByPrimaryKey(Integer id);
//      insert插入方法：不管User对象中成员是否为null，全部插入到User表中
    int insert(User record);
//      insertSelective插入方法：如果User对象中成员为null，则不插入到User表中
    int insertSelective(User record);

    User selectByPrimaryKey(Integer id);

    int updateByPrimaryKeySelective(User record);

    int updateByPrimaryKey(User record);

    //  根据业务需求，自己添加
//      查询用户名，返回查询结果的个数
    int checkUsername(String username);
//      查询邮箱，返回查询结果的个数
    int checkEmail(String email);

//      mybatis中dao层方法有多个参数时，需要使用Param注解来区分各个参数
//      查询用户，根据用户名和密码，返回User对象
    User selectLogin(@Param("username") String username,@Param("password") String password);
//      查询问题，根据用户名，返回问题
    String selectQuestionByUsername(String username);
//      查询答案，根据用户名，问题，答案，返回查询结果的个数
    int checkAnswer(@Param("username") String username,@Param("question") String question,@Param("answer") String answer);
//      更新密码，根据用户名，新密码，返回更新结果的个数
    int updatePasswordByUsername(@Param("username") String username, @Param("passwordNew") String passwordNew);
//      查询密码，根据旧密码，用户id，返回查询结果的个数
    int checkPassword(@Param("password") String password,@Param("userId") Integer userId);
//      查询邮箱，根据邮箱，用户id，返回查询结果的个数
    int checkEmailByUserId(@Param("eamil") String eamil,@Param("userId") Integer userId);
}