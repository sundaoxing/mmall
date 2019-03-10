package com.mmall.common;
/*
        支付类型枚举类：
 */
public enum PaymentType {
    ONLINE_PAY(1,"在线支付"),
    OTHER(2,"其他支付方式");

    private int code;//状态码
    private String desc;//状态码说明

    PaymentType(int code,String desc) {
        this.code=code;
        this.desc=desc;
    }

    /**
     * 根据枚举状态码获取枚举状态码说明信息：静态方法
     *      1.遍历枚举类的枚举对象数组
     *          判断枚举状态码是否为入参的状态码
     *              是：返回该枚举类对象
     *      2.抛出运行时异常，无该枚举类型
     * @param code      枚举状态码
     * @return
     */
    public static PaymentType getDescByCode(int code){
        for(PaymentType paymentType :values()){
            if(paymentType.code == code){
                return paymentType;
            }
        }
        throw new RuntimeException("PaymentType没有该枚举类型");
    }
    //开放常量获取方法
    public int getCode() {
        return code;
    }

    public String getDesc() {
        return desc;
    }
}
