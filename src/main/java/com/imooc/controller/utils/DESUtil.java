package com.imooc.controller.utils;
import javax.crypto.Cipher;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.DESedeKeySpec;
import javax.crypto.spec.IvParameterSpec;
import java.security.Key;
import java.util.Base64;

public class DESUtil {
    private static final String encoding = "utf-8";
    private static final String iv = "01234567";

    public static String encode(String str, String str2) {
        try {
            Key generateSecret = SecretKeyFactory.getInstance("desede").generateSecret(new DESedeKeySpec(str2.getBytes()));
            Cipher instance = Cipher.getInstance("desede/CBC/PKCS5Padding");
            instance.init(1, generateSecret, new IvParameterSpec(iv.getBytes()));
            return Base64.getEncoder().encodeToString(instance.doFinal(str.getBytes(encoding)));
        } catch (Exception e) {
           e.printStackTrace();
        }
        return "";
    }

}
