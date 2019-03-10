package com.mmall.common;
/*
*       响应状态码：
*           常量类：使用枚举类代替，增加常量说明
*/
public enum ResponseCode {

//  常量定义处，以后可以接着扩展使用
    SUCCESS(0,"Success"),//成功响应码
    ERROR(1,"Error"),//错误响应码
    NEED_LOGIN(10,"Need_Login"),//登陆响应码
    ILLEGAL_ARGUMENT(2,"ILLEGAL_ARGUMENT");//非法参数响应码

    private final int code;//状态码
    private final String desc;//状态码描述

    ResponseCode(int code,String desc){
        this.code=code;
        this.desc=desc;
    }
//  开放常量获取方法
    public int getCode(){
        return code;
    }
    public String getDesc(){
        return desc;
    }
}
