package com.imooc.controller.utils;

import java.util.Random;

public class CidUtil {

    public static String getCid(String imei, String mac, String androidId) {

        return DESUtil.encode(imei + "|#$" + getDeviceId() + "|#$" + androidId + "|#$" + mac + "|#$" + null, "@xi'an%lvdian#xitongbu~&");
    }

    public static String getDeviceId() {
        String key = "0123456789";
        StringBuilder stringBuilder = new StringBuilder();
        while (stringBuilder.length() != 13) {
            char ch = key.charAt(new Random().nextInt(key.length()));
            if (ch == '0' && stringBuilder.length() == 0) {
            } else {
                stringBuilder.append(ch);
            }
        }
        return stringBuilder.toString();
    }
}
