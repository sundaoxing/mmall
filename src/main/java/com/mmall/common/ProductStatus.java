package com.mmall.common;

/*
        商品状态枚举类：
 */
public enum ProductStatus {
    //  常量定义处，以后可以接着扩展使用
    On_SALE(1,"在售"),
    OFF_SALE(2,"下架"),
    DELETE_SALE(3,"删除"),

    CHECKED(4,"已勾选"),
    UN_CHECKED(5,"未勾选");

    private final int code;//状态码
    private final String desc;//状态码说明

    ProductStatus(int code,String desc){
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
