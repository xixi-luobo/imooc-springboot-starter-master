package com.imooc.controller;

import com.imooc.pojo.IMoocJSONResult;
import com.imooc.utils.ExportTextUtil;
import com.imooc.utils.ProxyUtil;
import com.imooc.utils.TxtFileReader;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.n3r.idworker.utils.JsonTranscoder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.*;

import java.text.SimpleDateFormat;



import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.jsoup.Connection;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import java.util.Date;

import java.util.List;


@RestController
@RequestMapping("kh")
public class KongHaoController {
    final static Logger logger = LoggerFactory.getLogger(KongHaoController.class);
    Map<String, String> cookies = null;

    ProxyUtil proxyUtil = null;
    List<String> ok = new ArrayList<>();
    List<String> no = new ArrayList<>();



    @RequestMapping("/readNumber")
    public void saveUser( ) throws Exception {
        ServletRequestAttributes servletRequestAttributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        HttpServletResponse response = servletRequestAttributes.getResponse();
//        if (proxyUtil == null) {
//            getProxy();//获取代理
//        }d
//
//        TxtFileReader.Builder builder = new TxtFileReader.Builder(new File("C:\\Users\\Administrator\\Desktop\\333.txt"),
//
//                line -> {
////                    boolean b = checkPhoneNumber(line);
////                    if (b) {
//                    //  checkPhone(line);
//                    // //                    }
//                    //logger.info(line);
//                    checkPhone(line);
//
//                });
//
//        TxtFileReader txtFileReader = builder.build();
//        txtFileReader.start();

        exportON(response);
        exportOK(response);

    }

    @RequestMapping("/readNumber1")
    public void saveUser1() throws Exception {


    }



    /**
     *  电信号码是否为空号
     * @param phone
     * @return
     */
    public boolean tel(String phone){
        logger.info("这是个电信号码:{},尝试判断号码是否为空", phone);
        try {
            Connection.Response response = Jsoup.connect("http://wb.crm.189.cn:8182/recharge/buildOrder/buildOrderAction.do?action=createOrder&number="+phone+"&boundphone=&paymoney=50&channel=WX&openId=dsadsd4343&DPM=wechatPay")
                    .method(Connection.Method.GET)
                    .ignoreContentType(true)
                    .proxy(proxyUtil.getIp(), proxyUtil.getPort())
                    .execute();
            String body = response.body();


            Document doc = Jsoup.parse(body);
            if (response.statusCode() != 200) {
                logger.error("tel check phone fail:{}", phone, body);
            }


            Element orderId = doc.getElementById("orderId");

            if (orderId != null) {
                logger.info("[{}],不是空号", phone);
                return true;
            } else {
                logger.info("[{}],是空号", phone);
                return false;
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
        return true;
    }

    /**
     * 联通号码是否是空号
     * @param phone
     * @return
     */
    public boolean uni(String phone) throws IOException {
        logger.info("这是个联通号码:{},尝试判断号码是否为空", phone);
        try {
            Connection.Response response = Jsoup.connect("http://upay.10010.com/npfwap/NpfMob/customInfo/cellInfoQuery?commonBean.phoneNo="+phone+"")
                    .method(Connection.Method.POST)
                    .ignoreContentType(true)
                    .proxy(proxyUtil.getIp(), proxyUtil.getPort())
                    .execute();
            String body = response.body();


            if (response.statusCode() != 200) {
                logger.error("uni check phone fail:{}", phone, body);
            }

            Map<String, Object> dataMap = JsonTranscoder.mapFromJson(body);
            if ("error" .equals(dataMap.get("out"))) {
                logger.info("[{}],是空号", phone);
                return false;

            } else {
                logger.info("[{}],不是空号", phone);
                return true;
            }

        } catch (IOException e) {
            e.printStackTrace();
            if (e.getMessage().equals("Read timed out")) {
                getProxy();
            }
        }
        return true;
    }

    /**
     * 移动号码是否是空号
     * @param phone cookie 是个大问题
     * @return
     */
    public boolean mob(String phone) throws IOException {
        logger.info("这是个移动号码:{},尝试判断号码是否为空", phone);
        if (cookies == null) {
           getCookie();
        }

        try {
            String param = "{\"paymentType\":\"WEIXIN-WAP\",\"mobile\":\"" + phone + "\",\"goodsId\":\"9990900000000050002\",\"rechargeType\":\"package\",\"activityId\":\"\",\"smsCode\":null}";
            Connection.Response response = Jsoup.connect("https://open.10086.cn/h5/payorder/order")
                    .method(Connection.Method.POST)
                    .ignoreContentType(true)
                    .proxy(proxyUtil.getIp(), proxyUtil.getPort())
                    .requestBody(param)
                    //.header("Referer", "https://open.10086.cn/h5/buy/recharge/payment.html?id=9990900000000050001&rechargeType=package&phone=" + phone + "&activityId=")
                    .cookies(cookies)
                    .header("Content-Type", "application/json")
                    .execute();

            String body = response.body();
            if (response.statusCode() != 200) {
                logger.error("mob check phone fail:{}", phone, body);
            }

            if (body.contains("html")) {
                logger.warn("%检测 [{}] mob cookie 失效,一秒后获取mod cookie%", phone);
                Thread.sleep(1000l);
                getCookie();
            }

            Map<String, Object> dataMap = JsonTranscoder.mapFromJson(body);
            String bizCode = (String) dataMap.get("bizCode");
            if ("1".equals(bizCode)) {
                logger.info("%[{}],不是空号%", phone);
                return true;
            } else if ("account.is.closed".equals(bizCode)) {
                logger.info("%[{}],是空号%", phone);
                return false;
            } else if ("unknown.bizcode".equals(bizCode)) {
                logger.info("%[{}],是空号%", phone);
                return false;
            }  else {
                logger.error("%判断检测移动空号[{}]出错:[{}]%", phone,dataMap.get("bizDesc"));
            }

        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
        return true;
    }

    public static void method(String file, String content) {
        BufferedWriter out = null;
        try {
            out = new BufferedWriter(new OutputStreamWriter(
                    new FileOutputStream(file, true)));// true,进行追加写。
            out.write(content + "\r\n");



        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                out.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public void getCookie() throws IOException {
        getProxy();
        try {
            pageIndex1();
            pageIndex3();

            logger.info("%尝试获取 mob recharge cookies%");
            Connection.Response response = Jsoup.connect("https://open.10086.cn/h5/buy/recharge/traffic-recharge.html")
                    .method(Connection.Method.GET)
                    .cookies(cookies)
                   . proxy(proxyUtil.getIp(), proxyUtil.getPort())
                    .ignoreContentType(true)
                    .execute();

            logger.info("%收到'获取 recharge Cookie'的应答%");
            String body = response.body();
            logger.info("statusCode:{}", response.statusCode());
            logger.info("statusMessage:{}",response.statusMessage());

            if (response.statusCode() == 200) {
                logger.debug("%crawled cookies success%" );
            } else {
                logger.warn("%尝试 '获取 cookies fail':%" + body);
            }
            logger.info("cookies:{}",cookies.toString());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    /****
     * 进入移动和包流量充值页面
     * https://mobilehall.cmpay.com/hnAutoH5/pages/ninefive/index.html?logtype=extendmsg&msgtype=2&msgid=100000000&opcode=JHY10401
     * jhy10401 产品类别
     */
    private void pageIndex1() throws IOException {
        logger.info("%尝试进入 JHY10401 产品页面%");
        Connection.Response response = Jsoup.connect("https://mobilehall.cmpay.com/hnAutoH5/pages/ninefive/index.html?logtype=extendmsg&msgtype=2&msgid=100000000&opcode=JHY10401")
                .method(Connection.Method.GET)
                .proxy(proxyUtil.getIp(), proxyUtil.getPort())
                .ignoreContentType(true)
                .execute();

        String body = response.body();
        if (response.statusCode() == 200) {
            logger.info("%获取产品页面成功 %");
        } else {
            logger.info("%获取产品页面失败 statusCode=[{}] response={}%", response.statusCode(), body);
        }

    }

    /**
     *   获取产品加密url链接 P170808009
     * @throws IOException
     * @return  返回 url的加密链
     */

    private String pageIndex2() throws IOException {
        logger.info("%尝试获取 产品加密链 产品页面%");
        String param = "{\"pageId\":\"P170808009\",\"t\":\"" + System.currentTimeMillis() + "\"}";
        Connection.Response response = Jsoup.connect("https://mobilehall.cmpay.com:10002/mbh5/getUrl")
                .method(Connection.Method.POST)
                .requestBody(param)
                .header("t",formatMM())
                .header("Content-Type", "application/json")
               . proxy(proxyUtil.getIp(), proxyUtil.getPort())
                .ignoreContentType(true)
                .execute();

        String body = response.body();
        logger.info("%收到获取 URL 加密链的应答 %");
        Map<String, Object> dataMap = JsonTranscoder.mapFromJson(body);
        if ("000000".equals(dataMap.get("retcode"))) {
            logger.info("%收到获取 URL 加密链的成功 :{}%", dataMap.get("desc"));
            Map data = (Map) dataMap.get("biz");
            String accessUrl = (String) data.get("accessUrl");
            return accessUrl;

        } else {
            logger.info("%收到获取 URL 加密链的失败 :{}%", dataMap.get("desc"));
        }
        return null;
    }

    private void pageIndex3() throws IOException {
        String accessUrl = pageIndex2();
        logger.info("accessUrl ---:[{}]", accessUrl);
        logger.info("%尝试获取 accessUrl 链接%");

        Connection.Response response = Jsoup.connect(accessUrl)
                .method(Connection.Method.GET)
                .proxy(proxyUtil.getIp(), proxyUtil.getPort())
                .ignoreContentType(true)
                .execute();

        String body = response.body();
        cookies = response.cookies();
        if (response.statusCode() != 200) {
            logger.error("%尝试获取 accessUrl 链接 fail:{}%", body);
        }
    }


    private static boolean checkPhoneNumber(String phone) {
        String regex = "^((13[0-9])|(14[5|7])|(15([0-3]|[5-9]))|(17[013678])|(18[0,5-9]))\\d{8}$";
        if(phone.length() != 11){
            logger.error("%手机号[{}]长度不正确%",phone);
            return false;
        }else{
            Pattern p = Pattern.compile(regex);
            Matcher m = p.matcher(phone);
            boolean isMatch = m.matches();
            if(isMatch){
                return true;
            } else {
                logger.error("%手机号[{}]错误格式！！！%", phone);
                return false;
            }
      }
    }


    private  void checkPhone(String tempStr) throws IOException {
//        String ok = "E:\\konghao\\ok.txt";
//        String konghao = "E:\\konghao\\konghao.txt";

        logger.info("%尝试获取[{}]的归属地%", tempStr);

        try {
            Connection.Response response = Jsoup.connect("http://upay.10010.com/npfwap/NpfMob/customInfo/cellInfoQuery?commonBean.phoneNo=" + tempStr + "")
                    .method(Connection.Method.GET)
                    .ignoreContentType(true)
                    .proxy(proxyUtil.getIp(), proxyUtil.getPort())
                    .execute();
            String body = response.body();

            Map<String, Object> dataMap = JsonTranscoder.mapFromJson(body);
            String carrier = (String) dataMap.get("carrier");
            if ("UNI" .equals(carrier)) { //联通
                if (uni(tempStr)) {
                    //method(ok,tempStr);
                   // exportTxt(httpServletResponse,"正常",tempStr);]
                    ok.add(tempStr);
                } else {
                   //method("空号",tempStr);
                    //exportTxt(httpServletResponse,"空号",tempStr);
                    no.add(tempStr);
                }
            } else if ("TEL" .equals(carrier)) {//电信
                if (tel(tempStr)){
                   // method(ok,tempStr);
                    //exportTxt(httpServletResponse,"正常",tempStr);
                    ok.add(tempStr);
                } else {
                    no.add(tempStr);
                   // method(konghao,tempStr);
                //    exportTxt(httpServletResponse,"空号",tempStr);

                }
            } else if ("MOB" .equals(carrier)) { //移动
                if (mob(tempStr)){
                   // method(ok,tempStr);
                    //exportTxt(httpServletResponse,"正常",tempStr);
                    ok.add(tempStr);
                } else {
                   // method(konghao,tempStr);
                    //exportTxt(httpServletResponse,"空号",tempStr);
                    no.add(tempStr);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
            logger.info("error message :{}", e.getMessage());
            if (e.getMessage().equals("Read timed out") || e.getMessage().equals("Connection timed out: connect")) {
                getProxy();
            }
        }
   }



    private static String formatMM() {
        Date day = new Date();
        SimpleDateFormat df = new SimpleDateFormat("yyyyMMddHHmm");
        String format = df.format(day);
        return format;

    }

    private ProxyUtil getProxy() throws IOException {
        Connection.Response response = Jsoup.connect("http://www.zdopen.com/ShortProxy/GetIP/?api=202108291132304579&akey=0d01811f7808faf9&count=1&timespan=3&type=3")
                .method(Connection.Method.GET)
                .ignoreContentType(true)
                .header("Host", "www.zdopen.com")
                .execute();

        String body = response.body();
        logger.info("body:{}", body);
        Map<String, Object> dataMap = JsonTranscoder.mapFromJson(body);
        if ("10001".equals(dataMap.get("code"))) {
            Map data = (Map) dataMap.get("data");
            List<Map> proxy_list = (List<Map>) data.get("proxy_list");

            proxy_list.forEach(e->{
                String ip = (String) e.get("ip");
                Integer port = (Integer) e.get("port");
                proxyUtil = new ProxyUtil();
                proxyUtil.setIp(ip);
                proxyUtil.setPort(port);
            });
            return proxyUtil;
        }
        return null;
    }


    public void exportOK(HttpServletResponse response) {
        ok.add("sadasdas");
        ok.add("sadasdas");
        ok.add("sadasdas");
        ok.add("sadasdas");
        ok.add("sadasdas");


        StringBuffer sb1 = new StringBuffer();
        ok.forEach(e -> {
            logger.info("ok:----->{}", e);
            sb1.append(e);
            sb1.append("\r\n");//换行字符

        });
        ExportTextUtil.writeToTxt2(response, sb1.toString(), "正常");
    }


    public void exportON(HttpServletResponse response){


        no.add("tttttttttt");
        no.add("tttttttttt");no.add("tttttttttt");no.add("tttttttttt");no.add("tttttttttt");no.add("tttttttttt");
        no.add("tttttttttt");no.add("tttttttttt");no.add("tttttttttt");no.add("tttttttttt");
        no.add("tttttttttt");


        StringBuffer sb2 = new StringBuffer();
        no.forEach(e->{
            logger.info("no:----->{}", e);
            sb2.append(e);
            sb2.append("\r\n");//换行字符
        });
        ExportTextUtil.writeToTxt1(response, sb2.toString(), "空号");
        //exportTxt(response, sb2.toString(), "空号");
    }

}
