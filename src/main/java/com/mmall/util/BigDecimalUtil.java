package com.mmall.util;

import java.math.BigDecimal;
/*
            BigDecimal：数据值高精度处理工具类
 */
public class BigDecimalUtil {
    //加法
    public static BigDecimal add(double v1 ,double v2){
        BigDecimal b1 = new BigDecimal(Double.toString(v1));
        BigDecimal b2 = new BigDecimal(Double.toString(v2));
        return b1.add(b2);
    }
    //减法
    public static BigDecimal subtract(double v1 ,double v2){
        BigDecimal b1 = new BigDecimal(Double.toString(v1));
        BigDecimal b2 = new BigDecimal(Double.toString(v2));
        return b1.subtract(b2);
    }
    //乘法
    public static BigDecimal multiply(double v1 ,double v2){
        BigDecimal b1 = new BigDecimal(Double.toString(v1));
        BigDecimal b2 = new BigDecimal(Double.toString(v2));
        return b1.multiply(b2);
    }
    //除法：保留两位小数，四舍五入
    public static BigDecimal divide(double v1 ,double v2){
        BigDecimal b1 = new BigDecimal(Double.toString(v1));
        BigDecimal b2 = new BigDecimal(Double.toString(v2));
        return b1.divide(b2,2,BigDecimal.ROUND_HALF_UP);
    }
}
