package com.mmall.common;
/*
        支付平台枚举类：
 */
public enum PayPlatform {
    ALIPAY(1,"支付宝"),
    WXPAY(2,"微信支付"),
    Other(3,"其他支付平台");

    private int code;//状态码
    private String desc;//状态码说明

    PayPlatform(int code,String desc) {
        this.code=code;
        this.desc=desc;
    }

    //开放常量获取方法
    public int getCode() {
        return code;
    }

    public String getDesc() {
        return desc;
    }
}
