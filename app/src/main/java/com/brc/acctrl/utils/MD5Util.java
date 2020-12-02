package com.brc.acctrl.utils;

import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * Created by Icarus.Lee on 2018/6/26.
 *
 * MD5工具类
 */
public class MD5Util {

    /**
     * 根据字符串返回MD5摘要值
     *
     * @param str
     * @return 摘要值
     */
    public static String getMD5(String str) {
        StringBuilder sb = new StringBuilder();
        try {
            MessageDigest md5 = MessageDigest.getInstance("MD5");
            // 这句是关键
            try {
                md5.update(str.getBytes("UTF-8"));
            } catch (UnsupportedEncodingException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
            byte[] bts = md5.digest();
            for (int i = 0; i < bts.length; i++) {
                int number = bts[i] & 0xff;
                String numStr = Integer.toHexString(number);
                if (numStr.length() == 1) {
                    sb.append("0");
                }
                sb.append(numStr);
            }
        } catch (NoSuchAlgorithmException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return sb.toString();
    }
}
