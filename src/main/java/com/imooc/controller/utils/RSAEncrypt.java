package com.imooc.controller.utils;
import javax.crypto.Cipher;
import java.math.BigInteger;
import java.security.KeyFactory;
import java.security.PublicKey;
import java.security.spec.RSAPublicKeySpec;
import java.text.SimpleDateFormat;
import java.util.Base64;
import java.util.Date;

public class RSAEncrypt {

    private static String exponent = "AQAB";
    private static byte[] KEY = {(byte)0x93,(byte)0x22,(byte)0x5F,(byte)0xE7,(byte)0x11,(byte)0xC8,(byte)0xCA,(byte)0x91,(byte)0xD2,(byte)0xB0,(byte)0xEE,(byte)0xDA,(byte)0xC9,(byte)0x47,(byte)0x75,(byte)0x9C,(byte)0x5D,(byte)0x13,(byte)0xD4,(byte)0x16,(byte)0x4C,(byte)0x3B,(byte)0xAE,(byte)0x35,(byte)0xB2,(byte)0xAA,(byte)0xA2,(byte)0xF8,(byte)0x67,(byte)0x47,(byte)0x75,(byte)0xD4,(byte)0x86,(byte)0x67,(byte)0xA5,(byte)0x36,(byte)0x34,(byte)0xAA,(byte)0xC9,(byte)0x72,(byte)0x87,(byte)0xE5,(byte)0xC7,(byte)0x0C,(byte)0x1A,(byte)0x79,(byte)0x2E,(byte)0xFD,(byte)0x4E,(byte)0x63,(byte)0x22,(byte)0xA0,(byte)0xF5,(byte)0x22,(byte)0x2F,(byte)0x07,(byte)0xEA,(byte)0x85,(byte)0x12,(byte)0xC8,(byte)0x24,(byte)0x69,(byte)0xE5,(byte)0xB6,(byte)0x4D,(byte)0xDC,(byte)0x0E,(byte)0x7A,(byte)0xE4,(byte)0xBD,(byte)0xE5,(byte)0xDF,(byte)0xB1,(byte)0xE9,(byte)0x79,(byte)0x1D,(byte)0xE6,(byte)0x80,(byte)0x15,(byte)0x7B,(byte)0x35,(byte)0x49,(byte)0xB9,(byte)0x0F,(byte)0x7B,(byte)0xC4,(byte)0x3B,(byte)0x7B,(byte)0xDF,(byte)0xC4,(byte)0x4E,(byte)0x1B,(byte)0xE7,(byte)0x7A,(byte)0x0E,(byte)0x18,(byte)0xCD,(byte)0x8B,(byte)0xC1,(byte)0xED,(byte)0x7E,(byte)0x92,(byte)0x0A,(byte)0xBE,(byte)0xAC,(byte)0x41,(byte)0xD1,(byte)0x46,(byte)0x4B,(byte)0x34,(byte)0x19,(byte)0x82,(byte)0x40,(byte)0x27,(byte)0x66,(byte)0xCE,(byte)0xD3,(byte)0x57,(byte)0x5E,(byte)0x2D,(byte)0xEE,(byte)0x93,(byte)0xD0,(byte)0xB5,(byte)0x87,(byte)0xF1,(byte)0x0A,(byte)0x1D};

    public static void main(String[] args) {
       System.out.println(getEntrypt("314512"));
    }


    public static String getEntrypt(String param){
        String params = "leadeon" + param + new SimpleDateFormat("yyyyMMddhhmmss").format(new Date());
        String module = Base64.getEncoder().encodeToString(KEY);
        byte[] en = encrypt(params, module);
        String result = Base64.getEncoder().encodeToString(en);
        result = result.replaceAll("\n", "");
        return result;
    }

    public static byte[] encrypt(String content, String module) {
        try {
            byte[] modulusBytes = Base64.getDecoder().decode(module);
            byte[] exponentBytes = Base64.getDecoder().decode(exponent);
            BigInteger modulus = new BigInteger(1, modulusBytes);
            BigInteger exponent = new BigInteger(1, exponentBytes);

            RSAPublicKeySpec rsaPubKey = new RSAPublicKeySpec(modulus, exponent);
            KeyFactory factory = KeyFactory.getInstance("RSA");
            PublicKey publicKey = factory.generatePublic(rsaPubKey);

            Cipher cipher = Cipher.getInstance("RSA");
            cipher.init(Cipher.ENCRYPT_MODE, publicKey);
            byte[] cipherData = cipher.doFinal(content.getBytes());
            return cipherData;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
}
