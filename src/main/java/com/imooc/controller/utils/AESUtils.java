package com.imooc.controller.utils;
import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.util.Base64;

public class AESUtils {

    public static String iv = "9791027341711819";
    public static String encrypt(String content, byte[] password) {
        SecretKeySpec key = new SecretKeySpec(password, "AES");
        try {
            Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
            cipher.init(Cipher.ENCRYPT_MODE, key, new IvParameterSpec(iv.getBytes()));
            return Base64.getEncoder().encodeToString(cipher.doFinal(content.getBytes()));
        } catch (Exception e) {

        }

        return null;
    }


    public static String decrypt(String content, byte[] password) {
        SecretKeySpec key = new SecretKeySpec(password, "AES");
        try {
            byte[] contentBytes = Base64.getDecoder().decode(content);
            Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
            cipher.init(Cipher.DECRYPT_MODE, key, new IvParameterSpec(iv.getBytes()));
            return new String(cipher.doFinal(contentBytes));
        } catch (Exception e) {

        }

        return null;
    }


}
