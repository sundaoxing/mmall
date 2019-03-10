package com.mmall.common;
/*
        订单状态枚举类：
 */
public enum OrderStatus {
    //  常量定义处，以后可以接着扩展使用
    CANCELED(0,"已取消"),
    NO_PAY(10,"未支付"),
    PAID(20,"已付款"),
    PAID_NO_SHIPPED(30,"已付款未发货"),
    SHIPPED(40,"已发货"),
    ORDER_SUCCESS(50,"订单完成"),
    ORDER_CLOSE(60,"订单关闭");


    private final int code;//状态码
    private final String desc;//状态码说明

    OrderStatus(int code,String desc){
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
    public static OrderStatus getDescByCode(int code){
        //values：该枚举类中所有枚举对象的数组
        for(OrderStatus orderStatus :values()){
            if(orderStatus.code == code){
                return orderStatus;
            }
        }
        throw new RuntimeException("OrderStatus没有该枚举类型");
    }
    //  开放常量获取方法
    public int getCode(){
        return code;
    }
    public String getDesc(){
        return desc;
    }
}
