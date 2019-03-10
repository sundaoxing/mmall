package com.mmall.util;

import org.springframework.util.StringUtils;

import java.security.MessageDigest;
/*
    MD5加密工具类
 */
public class MD5Util {

    //将字节数组转换成16进制字符组成的字符串
    private static String byteArrayToHexString(byte b[]) {
        StringBuffer resultSb = new StringBuffer();
        for (int i = 0; i < b.length; i++)
            resultSb.append(byteToHexString(b[i]));

        return resultSb.toString();
    }
    //将一个字节转换成16进制的字符
    private static String byteToHexString(byte b) {
        int n = b;
        if (n < 0)
            n += 256;
        int d1 = n / 16;
        int d2 = n % 16;
        return hexDigits[d1] + hexDigits[d2];
    }

    /**
     * 根据指定字符集使用MD5算法加密字符串，返回加密后的字符串
     *
     * @param origin        源数据
     * @param charsetName   字符集
     * @return
     */
    private static String MD5Encode(String origin, String charsetName) {
        String resultString = null;
        try {
            resultString = new String(origin);
            MessageDigest md = MessageDigest.getInstance("MD5");
            if (charsetName == null || "".equals(charsetName))
                resultString = byteArrayToHexString(md.digest(resultString.getBytes()));
            else
                resultString = byteArrayToHexString(md.digest(resultString.getBytes(charsetName)));
        } catch (Exception exception) {
        }
        return resultString.toUpperCase();
    }

    /**
     * 根据UTF-8字符集使用MD5算法加密字符串，返回加密后的字符串
     * @param origin
     * @return
     */
    public static String MD5EncodeUtf8(String origin) {
//        origin = origin + PropertiesUtil.getProperty("password.salt", "");
        return MD5Encode(origin, "utf-8");
    }

    //16进制：进制数对应的字符串
    private static final String hexDigits[] = {"0", "1", "2", "3", "4", "5",
            "6", "7", "8", "9", "a", "b", "c", "d", "e", "f"};

}
