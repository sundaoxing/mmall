package com.mmall.util;

import com.mmall.common.Const;
import org.apache.commons.lang3.StringUtils;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import java.util.Date;

/*
        日期显示美化工具类
 */
public class DateTimeUtil {
    /**
     * 将指定时间格式的String类型的时间序列转换成Date类型的时间戳
     * @param dateTimeStr   日期字符串
     * @param formatStr     时间格式
     * @return
     */
    public static Date strToDate(String dateTimeStr,String formatStr){
        DateTimeFormatter dateTimeFormatter = DateTimeFormat.forPattern(formatStr);
        DateTime dateTime = dateTimeFormatter.parseDateTime(dateTimeStr);
        return dateTime.toDate();
    }

    /**
     * 将Date类型的时间戳转换成指定时间格式的String类型的时间序列
     * @param date          日期
     * @param formatStr     时间格式
     * @return
     */
    public static String dateToStr(Date date ,String formatStr){
        if(date == null){
            return StringUtils.EMPTY;
        }
        DateTime dateTime = new DateTime(date);
        return dateTime.toString(formatStr);
    }

    /**
     * 将yyyy-MM-dd HH:mm:ss格式的String类型的时间序列转换成Date类型的时间戳
     * @param dateTimeStr   日期字符串
     * @return
     */
    public static Date strToDateStandard(String dateTimeStr){
        DateTimeFormatter dateTimeFormatter = DateTimeFormat.forPattern(Const.STANDARDFORMAT);
        DateTime dateTime = dateTimeFormatter.parseDateTime(dateTimeStr);
        return dateTime.toDate();
    }

    /**
     * 将Date类型的时间戳转换成yyyy-MM-dd HH:mm:ss格式的String类型的时间序列
     * @param date      日期
     * @return
     */
    public static String dateToStrStandard(Date date){
        if(date == null){
            return StringUtils.EMPTY;
        }
        DateTime dateTime = new DateTime(date);
        return dateTime.toString(Const.STANDARDFORMAT);
    }
}
