package com.mmall.util;

import com.mmall.common.Const;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Properties;

/*
    Properties工具类：

 */
public class PropertiesUtil {
    //日志打印
    private static Logger logger = LoggerFactory.getLogger(PropertiesUtil.class);
    //
    private static Properties properties;
    /*
        静态代码块：
            当PropertiesUtil类被加载时，自动被调用，不需要创建对象实例
     */
    static{
        properties =new Properties();
        try {
            //加载配置文件mmall.properties
            properties.load(new InputStreamReader(PropertiesUtil.class.getClassLoader()
                    .getResourceAsStream(Const.MMALLPROPERTIES),"UTF-8"));
        } catch (IOException e) {
            logger.error("配置文件"+Const.MMALLPROPERTIES+"加载异常",e);
        }
    }

    /**
     * 获取配置文件中key键对应的value值
     * @param key   配置文件的key键
     * @return      若value为空，返回null，否则直接返回value
     */
    public static String getProperty(String key){
        String value = properties.getProperty(key);
        if(StringUtils.isBlank(value)){
            return null;
        }
        return value.trim();
    }

    /**
     * 获取配置文件中key键对应的value值，
     * @param key           配置文件的key键
     * @param defaultValue  默认value值
     * @return              如果value值不存在，则使用默认值代替，否则返回value值
     */
    public static String getProperty(String key,String defaultValue){
        String value = properties.getProperty(key);
        if(StringUtils.isBlank(value)){
            value=defaultValue;
        }
        return value.trim();
    }
}
