package com.imooc.controller.utils;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import org.apache.commons.lang3.StringUtils;
import org.jsoup.Connection;
import org.jsoup.Jsoup;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;

public class CmccLogin {
    static String ssv = "899de5a35fd01eafba94a7051531a164ac6b478311ce778692c453cc1c0cb4df776be4fd";
    public static void main(String[] args) {
        initSv();
        sendSms("18370878317");
        Scanner sc = new Scanner(System.in);
        System.out.print("请输入短信验证码：");
        String ckCode = sc.next();
        login1("18370878317",ckCode);
        recharge("18782550918");
    }


    public static  void  initSv(){
        String imei = "460006906762712";//自己做修改
        String mac = "00:07:EC:F7:9C:18";//自己做修改
        String androidId = "50b36297fcd481fe";//自己做修改
        String url = "https://clientaccess.10086.cn/biz-orange/DN/init/startInit";
        String sv = "7.1"; //sdk版本
        String sp = "1920x1080";//分辨率
        String sb = "HUAWEI";//品牌  //自己做修改
        String sn = "Nova4";//型号 //自己做修改
        InitPo initPo = new InitPo();
        initPo.setAk(Consts.AK);
        initPo.setCid(CidUtil.getCid(imei,mac,androidId));
        initPo.setCtid(CidUtil.getCid(imei,mac,androidId));
        initPo.setCity("");
        initPo.setCv(Consts.VERSION);
        initPo.setEn("0");
        initPo.setImei(imei);
        initPo.setNt("3");
        initPo.setSb(sb);
        initPo.setSn(sn);
        initPo.setSp(sp);
        initPo.setSt(Consts.ST);
        initPo.setSv(sv);
        initPo.setT("");
        initPo.setTel(Consts.TEL);
        initPo.setXc(Consts.CHANNEL);
        initPo.setXk("");
        initPo.setProv("");
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("channelCode",Consts.CHANNEL);
        jsonObject.put("cityCode","");
        jsonObject.put("imei",imei);
        jsonObject.put("provinceCode","");
        jsonObject.put("ssk",CidUtil.getCid(imei,mac,androidId));
        initPo.setReqBody(jsonObject);
        String param = EncryptUtils.encryptPostPram(JSON.toJSONString(initPo));
        String xs = MD5Util.getMD5(url + "_" + JSON.toJSONString(initPo) + "_" + "Leadeon/SecurityOrganization");
        Map<String,String> headers = new HashMap<>();
        headers.put("x-qen","2");
        headers.put("xs",xs);
        headers.put("Content-Type","application/json");
        headers.put("Host","clientaccess.10086.cn");
        headers.put("Accept-Encoding","gzip");
        headers.put("User-Agent","okhttp/3.11.0");


        try {
          Connection.Response response =  Jsoup.connect(url)
                    .method(Connection.Method.POST)
                    .requestBody(param)
                    .headers(headers)
                    .ignoreContentType(true)
                    .execute();
          String body = EncryptUtils.decrypt(response.body());
          System.out.println(body);
          if(StringUtils.isNotBlank(body)){
              ssv = JSONObject.parseObject(body).getJSONObject("rspBody").getString("ssv");
              System.out.println(ssv);
          }

        } catch (IOException e) {
            e.printStackTrace();
        }

    }



    public static void recharge(String phone){
        String imei = "460006906762712";//自己做修改
        String mac = "00:07:EC:F7:9C:18";//自己做修改
        String androidId = "50b36297fcd481fe";//自己做修改
        StringBuffer stringBuffer = new StringBuffer();
        stringBuffer.append("https://app.10086.cn/biz-orange/CHW/saveRechargeOrder/saveRechargeOrder?");
        stringBuffer.append("JSESSIONID=");
        stringBuffer.append("8f07dc73-9e52-401c-93d4-649203e32c72;UID=0cded1b920144a20ad5dd5d2a37efaf1;Comment=SessionServer-unity;Path=/;ticketID=PaaS");

        String sv = "7.1"; //sdk版本
        String sp = "1920x1080";//分辨率
        String sb = "HUAWEI";//品牌 //自己做修改
        String sn = "Nova4";//型号 //自己做修改
        Init init = new Init();

        init.setCtid(CidUtil.getCid(imei,mac,androidId));
     //   init.setT("JSESSIONID=f9db9840-a0c7-4b1d-9142-775b9cada9c9; UID=1fae93e46a67443c98763842bfe29551; Comment=SessionServer-unity; Path=/");
        init.setSn(sn);
        init.setCv(Consts.VERSION);
        init.setSt(Consts.ST);
        init.setSv(sv);
        init.setSp(sp);
        init.setXk(ssv);

        init.setXc(Consts.CHANNEL);
        init.setImei(imei);
        init.setNt("3");
        init.setSb(sb);
        init.setProv("280");
        init.setCity("0817");
        init.setTel(phone);

        JSONObject jsonObject = new JSONObject();
        jsonObject.put("operateId","4725");
        jsonObject.put("amount",29.94);
        jsonObject.put("chargeMoney",30);
        jsonObject.put("choseMoney","30");
        jsonObject.put("payPhoneNo",phone);
        jsonObject.put("rechargeNo","Y0XDIOX0XRGJ8a9fBeyX4LOvu7N30aWxTKx9smQQN7mlMU0ZCv14Y2cxZT18d1UGY7opBediu2fk8ZqTrABAhAk8ti2VH9903GJ6GlsOhopZ%2FfxfqLZhS8or9xV0IDnvD%2FqRwuEf26rMqRVkleyInF3DNxe3fWZCfPAGBTgFXWE%3D");
        jsonObject.put("numFlag","");

        init.setReqBody(jsonObject);

        String param = JSON.toJSONString(init);
        //String param = EncryptUtils.encryptPostPram(JSON.toJSONString(initPo));
        String xs = MD5Util.getMD5(stringBuffer.toString() + "_" + JSON.toJSONString(init) + "_" + "Leadeon/SecurityOrganization");
        System.out.println("xs:" + xs);
        Map<String,String> headers = new HashMap<>();
//        headers.put("Host","app.10086.cn");
//        headers.put("Connection", "keep-alive");
//        headers.put("Content-Length", "826");
//        headers.put("Sec-Fetch-Mode", "cors");
//        headers.put("xs", xs);
//        headers.put("Origin","https://app.10086.cn");
//        headers.put("User-Agent", "Mozilla/5.0 (Linux; U; 5.1.1; ADR6300 Build/FRF91) AppleWebKit/533.1 (KHTML, like Gecko) Version/4.0 Mobile Safari/533.1");
//        headers.put("Content-Type", "application/json; charset=UTF-8");
//        headers.put("Accept", "application/json, text/javascript, */*; q=0.01");
//        headers.put("application/json, text/javascript, */*; q=0.01", "XMLHttpRequest");
//        headers.put("Sec-Fetch-Site", "same-origin");
       headers.put("Referer", "https://app.10086.cn/leadeon-cmcc-static/v2.0/pages/recharge/recharge.html");
//        headers.put("Accept-Encoding", "gzip, deflate, br");
//        headers.put("Accept-Language", "zh-CN,zh;q=0.9,en-US;q=0.8,en;q=0.7");

        headers.put("x-qen","2");
        headers.put("xs",xs);
        headers.put("Content-Type","application/json");
        headers.put("Host","clientaccess.10086.cn");
        headers.put("Accept-Encoding","gzip");
        headers.put("User-Agent","okhttp/3.11.0");


        try {
            Connection.Response response =  Jsoup.connect(stringBuffer.toString())
                    .method(Connection.Method.POST)
                    .requestBody(param)
                    .headers(headers)
                    .ignoreContentType(true)
                    .timeout(1000)
                    .execute();

            System.out.println(response.body());

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void sendSms(String phone){
        String imei = "460006906762712";//自己做修改
        String mac = "00:07:EC:F7:9C:18";//自己做修改
        String androidId = "50b36297fcd481fe";//自己做修改
        String url = "https://clientaccess.10086.cn/biz-orange/LN/uamrandcode/sendMsgLogin";
        String sv = "7.1"; //sdk版本
        String sp = "1920x1080";//分辨率
        String sb = "HUAWEI";//品牌 //自己做修改
        String sn = "Nova4";//型号 //自己做修改
        InitPo initPo = new InitPo();
        initPo.setAk(Consts.AK);
        initPo.setCid(CidUtil.getCid(imei,mac,androidId));
        initPo.setCtid(CidUtil.getCid(imei,mac,androidId));
        initPo.setCity("");
        initPo.setCv(Consts.VERSION);
        initPo.setEn("0");
        initPo.setImei(imei);
        initPo.setNt("3");
        initPo.setSb(sb);
        initPo.setSn(sn);
        initPo.setSp(sp);
        initPo.setSt(Consts.ST);
        initPo.setSv(sv);
        initPo.setT("");
        initPo.setTel(Consts.TEL);
        initPo.setXc(Consts.CHANNEL);
        initPo.setXk(ssv);
        initPo.setProv("");
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("cellNum",phone);
        initPo.setReqBody(jsonObject);
        String param = EncryptUtils.encryptPostPram(JSON.toJSONString(initPo));
        String xs = MD5Util.getMD5(url + "_" + JSON.toJSONString(initPo) + "_" + "Leadeon/SecurityOrganization");
        Map<String,String> headers = new HashMap<>();
        headers.put("x-qen","2");
        headers.put("xs",xs);
        headers.put("Content-Type","application/json");
        headers.put("Host","clientaccess.10086.cn");
        headers.put("Accept-Encoding","gzip");
        headers.put("User-Agent","okhttp/3.11.0");
        try {
            Connection.Response response =  Jsoup.connect(url)
                    .method(Connection.Method.POST)
                    .requestBody(param)
                    .headers(headers)
                    .ignoreContentType(true)
                    .timeout(1000)
                    .execute();
            String body = EncryptUtils.decrypt(response.body());
            System.out.println(body);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void login(String phone,String password,String smsCode){
        String imei = "460006906762712";//自己做修改
        String mac = "00:07:EC:F7:9C:18";//自己做修改
        String androidId = "50b36297fcd481fe";//自己做修改
        String url = "https://clientaccess.10086.cn/biz-orange/LN/tempIdentCode/getTmpIdentCode";
        String sv = "7.1"; //sdk版本
        String sp = "1920x1080";//分辨率
        String sb = "HUAWEI";//品牌
        String sn = "Nova4";//型号
        InitPo initPo = new InitPo();
        initPo.setAk(Consts.AK);
        initPo.setCid(CidUtil.getCid(imei,mac,androidId));
        initPo.setCtid(CidUtil.getCid(imei,mac,androidId));
        initPo.setCity("0826");
        initPo.setCv(Consts.VERSION);
        initPo.setEn("0");
        initPo.setImei(imei);
        initPo.setNt("3");
        initPo.setSb(sb);
        initPo.setSn(sn);
        initPo.setSp(sp);
        initPo.setSt(Consts.ST);
        initPo.setSv(sv);
        initPo.setT("");
        initPo.setTel(Consts.TEL);
        initPo.setXc(Consts.CHANNEL);
        initPo.setXk(ssv);
        initPo.setProv("280");
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("cellNum",RSAEncrypt.getEntrypt(phone));
        jsonObject.put("businessCode","01");
        jsonObject.put("imei",imei);
        jsonObject.put("passwd", RSAEncrypt.getEntrypt(password));
        jsonObject.put("smsPasswd",smsCode);
        initPo.setReqBody(jsonObject);
        String param = EncryptUtils.encryptPostPram(JSON.toJSONString(initPo));
        String xs = MD5Util.getMD5(url + "_" + JSON.toJSONString(initPo) + "_" + "Leadeon/SecurityOrganization");
        Map<String,String> headers = new HashMap<>();
        headers.put("x-qen","2");
        headers.put("xs",xs);
        headers.put("Content-Type","application/json");
        headers.put("Host","clientaccess.10086.cn");
        headers.put("Accept-Encoding","gzip");
        headers.put("User-Agent","okhttp/3.11.0");
        try {
            Connection.Response response =  Jsoup.connect(url)
                    .method(Connection.Method.POST)
                    .requestBody(param)
                    .headers(headers)
                    .ignoreContentType(true)
                    .execute();
            String body = EncryptUtils.decrypt(response.body());
            System.out.println(body);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    public static void login1(String phone,String smsCode){
        String imei = "460006906762712";//自己做修改
        String mac = "00:07:EC:F7:9C:18";//自己做修改
        String androidId = "50b36297fcd481fe";//自己做修改
        String url = "https://clientaccess.10086.cn/biz-orange/LN/uamthreenetworklogin/login";
        String sv = "7.1"; //sdk版本
        String sp = "1920x1080";//分辨率
        String sb = "HUAWEI";//品牌
        String sn = "Nova4";//型号
        Init initPo = new Init();
        initPo.setAk(Consts.AK);
        initPo.setAppKey("00000159");
        initPo.setBaseFrameVersion("0.0.1");
        initPo.setComponentID("BI2021SA000055");
        initPo.setComponentVersion("2.0.0");
        initPo.setPackageName("packageName");

        initPo.setCid(CidUtil.getCid(imei,mac,androidId));
        initPo.setCtid(CidUtil.getCid(imei,mac,androidId));
        initPo.setCity("0755");
        initPo.setCv(Consts.VERSION);
        initPo.setImei(imei);
        initPo.setNt("3");
        initPo.setSb(sb);
        initPo.setSn(sn);
        initPo.setSp(sp);
        initPo.setSt(Consts.ST);
        initPo.setSv(sv);
        initPo.setT("");
        initPo.setTel(Consts.TEL);
        initPo.setXc(Consts.CHANNEL);
        initPo.setXk(ssv);
        initPo.setProv("200");
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("cellNum",RSAEncrypt.getEntrypt(phone));
        jsonObject.put("sendSmsFlag","0");
        jsonObject.put("imei",imei);
        jsonObject.put("verifyCode",smsCode);
        initPo.setReqBody(jsonObject);
        String param = EncryptUtils.encryptPostPram(JSON.toJSONString(initPo));
        String xs = MD5Util.getMD5(url + "_" + JSON.toJSONString(initPo) + "_" + "Leadeon/SecurityOrganization");
        Map<String,String> headers = new HashMap<>();
        headers.put("x-qen","2");
        headers.put("xs",xs);
        headers.put("Content-Type","application/json");
        headers.put("Host","clientaccess.10086.cn");
        headers.put("Accept-Encoding","gzip");
        headers.put("User-Agent","okhttp/3.11.0");
        try {
            Connection.Response response =  Jsoup.connect(url)
                    .method(Connection.Method.POST)
                    .requestBody(param)
                    .headers(headers)
                    .ignoreContentType(true)
                    .execute();
            String body = EncryptUtils.decrypt(response.body());
            System.out.println(body);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
