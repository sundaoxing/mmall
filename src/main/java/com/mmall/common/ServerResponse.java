package com.mmall.common;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;

import java.io.Serializable;
/*
                通用服务端响应对象
                    序列化成json格式，响应给前端
 */
//当值value为null时，忽略键key（json序列化后都不会显示）
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ServerResponse<T> implements Serializable {
    private int status;//状态码
    private String msg;//状态码信息
    private T data;//响应数据（泛型支持）

    /*
        构造方法：设置成私有，通过静态方法向外开放
            原因：响应给前端的json格式会根据需求来调整参数，使用不同的构造器，
                 避免当data为String类型时，和String类型的msg产生冲突，
                 同时节约服务端传输的数据容量。
                1.状态码
                2.状态码+状态码信息
                3.状态码+响应数据
                4状态码+状态码信息+响应数据
     */
    private ServerResponse(int status){
        this.status=status;
    }
    private ServerResponse(int status,String msg){
        this.status=status;
        this.msg=msg;
    }
    private ServerResponse(int status,T data){
        this.status=status;
        this.data=data;
    }
    private ServerResponse(int status,String msg,T data){
        this.status=status;
        this.msg=msg;
        this.data=data;
    }

    @JsonIgnore//忽略isSuccess方法的json序列化
    //判断服务端响应状态码是否是成功状态码
    public boolean isSuccess(){
        return this.status==ResponseCode.SUCCESS.getCode();
    }
    //获取响应状态码
    public int getStatus(){
        return status;
    }
    //获取响应状态码信息
    public String getMsg(){
        return msg;
    }
    //获取响应数据
    public T getData(){
        return data;
    }

    //返回成功状态码：0的ServerResponse的实例对象
    public static <T> ServerResponse<T> createBySuccess(){
        return new ServerResponse<T>(ResponseCode.SUCCESS.getCode());
    }
    //返回成功状态码：0+状态码信息的ServerResponse的实例对象
    public static <T> ServerResponse<T> createBySuccessMsg(String msg){
        return new ServerResponse<T>(ResponseCode.SUCCESS.getCode(),msg);
    }
    //返回成功状态码：0+响应数据的ServerResponse的实例对象
    public static <T> ServerResponse<T> createBySuccess(T data){
        return new ServerResponse<T>(ResponseCode.SUCCESS.getCode(),data);
    }
    //返回成功状态码：0+状态码信息+响应数据的ServerResponse的实例对象
    public static <T> ServerResponse<T> createBySuccess(String msg,T data){
        return new ServerResponse<T>(ResponseCode.SUCCESS.getCode(),msg,data);
    }
    //返回失败状态码：1+错误描述信息的ServerResponse的实例对象
    public static <T> ServerResponse<T> createByError(){
        return new ServerResponse<T>(ResponseCode.ERROR.getCode(),ResponseCode.ERROR.getDesc());
    }
    //返回失败状态码：1+状态码信息的ServerResponse的实例对象
    public static <T> ServerResponse<T> createByErrorMsg(String errorMsg){
        return new ServerResponse<T>(ResponseCode.ERROR.getCode(),errorMsg);
    }
    //返回错误状态码：int类型+状态码信息的ServerResponse的实例对象
    public static <T> ServerResponse<T> createByErrorCodeMsg(int errorCode,String errorMsg){
        return new ServerResponse<T>(errorCode,errorMsg);
    }
}
