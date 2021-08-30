package com.imooc.controller.utils;//package com.imooc.config.cmcc.utils;
//
//import com.alibaba.fastjson.JSON;
//import com.alibaba.fastjson.JSONArray;
//import com.alibaba.fastjson.JSONObject;
//import com.mh.mobile.crawler.common.utils.*;
//import com.mh.mobile.crawler.common.utils.cmcc.CidUtil;
//import com.mh.mobile.crawler.common.utils.cmcc.EncryptUtils;
//import com.mh.mobile.crawler.dto.cmcc.Consts;
//import com.mh.mobile.crawler.dto.cmcc.InitPo;
//import com.mh.mobile.crawler.http.enums.*;
//import com.mh.mobile.crawler.dto.carrier.CarrierInfo;
//import com.mh.mobile.crawler.http.HttpClient;
//import com.mh.mobile.crawler.http.ProxyHttpClient;
//import com.mh.mobile.crawler.http.model.QueryTimeInfo;
//import com.mh.mobile.crawler.http.model.SimpleHttpResponse;
//import com.mh.mobile.crawler.http.service.DynamicCodeHelper;
//import com.mh.mobile.crawler.parser.cmcc.ChinaCmccAppParser;
//import com.mh.mobile.crawler.processor.AbstractProcessor;
//import lombok.extern.slf4j.Slf4j;
//import org.apache.http.Header;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.context.annotation.Scope;
//import org.springframework.stereotype.Service;
//import org.springframework.util.CollectionUtils;
//
//import java.text.SimpleDateFormat;
//import java.util.ArrayList;
//import java.util.Calendar;
//import java.util.Date;
//import java.util.List;
//
//@Slf4j
//@Service
//@Scope("prototype")
//public class ChinaCmccApp extends AbstractProcessor {
//
//    private static final String CHANNEL_CODE = "A2081";
//    private static final String SYSTEM_BRAND = "HUAWEI";
//    private static final String SYSTEM_NUMBER = "Nova4";
//    private static final String SYSTEM_PIXEL = "1920x1080";
//    private static final String SYSTEM_VERSION = "7.1";
//
//    private static final String AK = "F4AA34B89513F0D087CA0EF11A3277469DC74905";
//
//
//    private static final String NO_DETAIL = "无详单数据";
//    private static final String NO_OPTION_IN_30_MINUTES = "已超过30分钟未进行任何操作，为保证账号安全，建议您重新登录";
//    private static final String PROVINCE_CODE = "provinceCode";
//    private static final String CITY_CODE = "cityCode";
//
//    private static final String PROVINCE = "province";
//    private static final String CITY = "city";
//    static  String ssv =  "899de5a35fd01eafba94a7051531a164ac6b478311ce778692c453cc1c0cb4df776be4fd";
//
//    @Autowired
//    private ChinaCmccAppParser parser;
//    /**
//     * 目前官方只能是200
//     */
//    private static final int UNIT = 200;
//
//    /**
//     * 需要对http响应解密
//     */
//    private boolean shouldDecryptHttpResponse = true;
//    private String encryptedPhone;
//    private String encryptedPwd;
//    private JSONObject loginRequestBodyJson;
//
//    /**
//     * 获取详单每页条数
//     */
//    private int getDetailSize(CarrierModule carrierModule, String result) {
//        if (StringUtils.isEmpty(result)) {
//            log.info("[{}]中国移动app 获取[{}]详单页面size失败：响应为空", context.getTaskId(), carrierModule.getDesc());
//            return 0;
//        }
//        JSONObject detailJson = JsonUtils.getJSONObject(result);
//        if (detailJson == null) {
//            log.info("[{}]中国移动app 获取[{}]详单页面size失败：非json格式", context.getTaskId(), carrierModule.getDesc());
//            return 0;
//        }
//        JSONObject rspBody = detailJson.getJSONObject("rspBody");
//        if (null == rspBody) {
//            log.info("[{}]中国移动app 获取[{}]详单页面size失败：未找到rspBody节点", context.getTaskId(), carrierModule.getDesc());
//            return 0;
//        }
//        String element = "callList";
//        if (CarrierModule.NET.equals(carrierModule)) {
//            element = "netPlayList";
//        } else if (CarrierModule.SMS.equals(carrierModule)) {
//            element = "messageList";
//        }
//        JSONArray jsonArray = rspBody.getJSONArray(element);
//        if (null == jsonArray) {
//            log.info("[{}]中国移动app 获取[{}]详单页面size失败：未找到[{}]节点", context.getTaskId(), carrierModule.getDesc(), element);
//            return 0;
//        }
//        return jsonArray.size();
//    }
//
//    /**
//     * 获取当前时间，格式“yyyyMMddHHmmssSSS”
//     */
//    private static String getNowTime() {
//        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmssSSS");
//        return sdf.format(new Date());
//    }
//
//    /**
//     * 初始化HttpHelper头参数
//     */
//    private static void initHttpClientHeaders(HttpClient httpClient) {
//        httpClient.getConfig().removeAllHeader();
//        httpClient.getConfig().updateHeader("x-qen", "2");
//        httpClient.getConfig().updateHeader("User-Agent", "okhttp/3.12.0");
//        httpClient.getConfig().updateHeader("Content-Type", "application/json; charset=UTF-8");
//        httpClient.getConfig().updateHeader("Accept-Encoding", "gzip");
////        httpClient.getConfig().updateHeader("Keep-Alive", "true");
//        httpClient.getConfig().updateHeader("Host", "clientaccess.10086.cn");
//    }
//
//    /**
//     * 设置xs请求头
//     */
//    private static void setXSHeaderValue(HttpClient httpClient, String url, String param) throws IllegalArgumentException {
//        if ((!StringUtils.isEmpty(url)) && (!StringUtils.isEmpty(param))) {
//            httpClient.getConfig().updateHeader("xs", MD5Utils.toMD5(url + "_" + param + "_" + "Leadeon/SecurityOrganization").toLowerCase());
//        } else {
//
//            throw new IllegalArgumentException("url或param参数不能为null或空字符串！");
//        }
//    }
//
//    private static Integer getPageNum(Integer totalCount, Integer pageSize) {
//        if (pageSize > 0) {
//            return totalCount / pageSize + (totalCount % pageSize == 0 ? 0 : 1);
//        } else {
//            return 1;
//        }
//    }
//
//    @Override
//    public boolean retryCheck(String result) {
//        if (StringUtils.isEmpty(result)) {
//            log.info("[{}]中国移动APP 响应结果为空或null，触发重试", context.getTaskId(), result);
//            return true;
//        } else if (result.contains(NO_DETAIL)) {
//            log.info("[{}]中国移动APP 响应结果包含\"{}\"，触发重试", context.getTaskId(), NO_DETAIL);
//            return true;
//        } else if (result.contains(NO_OPTION_IN_30_MINUTES)) {
//            log.info("[{}]中国移动APP 响应结果包含\"{}\"，触发重试", context.getTaskId(), NO_OPTION_IN_30_MINUTES);
//            return true;
//        } else if (result.contains("403 Forbidden")) {
//            log.info("[{}]中国移动APP 响应结果包含\"403 Forbidden\"，触发重试（上报代理失效并重借）", context.getTaskId());
//            if (getHttpClient() instanceof ProxyHttpClient) {
//                ProxyHttpClient proxyHttpClient = (ProxyHttpClient) getHttpClient();
//                proxyHttpClient.returnCrawlerProxy(messageHelper, p8ApiUrl, context);
//                proxyHttpClient.setProxy(messageHelper, p8ApiUrl, context);
//            }
//            return true;
//        }
//        return false;
//    }
//
//    @Override
//    public String resultHandle(String result) {
//        //多线程请考虑其他实现或加锁
//        if (shouldDecryptHttpResponse && !StringUtils.isEmpty(result)) {
//            return EncryptUtils.decrypt(result);
//        } else {
//            return result;
//        }
//    }
//
//
//    private String sendPost(String url, String param, boolean shouldDecryptHttpResponse) {
//        this.shouldDecryptHttpResponse = shouldDecryptHttpResponse;
//
//        return getHttpClient().sendPost(url, param);
//    }
//
//    /**
//     * 备用
//     */
//    private String sendGet(String url, boolean shouldDecryptHttpResponse) {
//        this.shouldDecryptHttpResponse = shouldDecryptHttpResponse;
//        return getHttpClient().sendGet(url);
//    }
//
//
//    /**
//     * 获取登录请求参数对象
//     */
//    private JSONObject getLoginRequestBodyJson() throws Exception {
//        String provinceCode = context.getMobileHCodeDto().getProvTelCode();
//        String cityCode = context.getMobileHCodeDto().getTelCode();
//        if (provinceCode == null || StringUtils.isEmpty(provinceCode.trim()) || cityCode == null || StringUtils.isEmpty(cityCode.trim())) {
//            cityCode = "021";
//            provinceCode = "210";
//            log.info("[{}]中国移动app 网关传参provinceCode或cityCode有null或空值，将采用上海地provinceCode:[{}],cityCode:[{}]", context.getTaskId(), provinceCode, cityCode);
//        }
////        long imei = ChinaCmccAppUtils.getRandomIMEI();
//        String imei = ChinaCmccAppUtils.getRandomIMEI() + "";
////        String ssk = imei + "|#$" + ChinaCmccAppUtils.getRandomNumStr(13) + "|#$" + ChinaCmccAppUtils.getRandomHexNumStr(16) + "|#$" + ChinaCmccAppUtils.getRandomMac4Qemu() + "|#$" + ChinaCmccAppUtils.getRandomMac4Qemu();
////        String ssk = imei + "|#$" + ChinaCmccAppUtils.getRandomNumStr(13) + "|#$" + ChinaCmccAppUtils.getRandomHexNumStr(16) + "|#$" + ChinaCmccAppUtils.getRandomMac4Qemu() + "|#$" + null;
////        ssk = ChinaCmccAppUtils.desEncrypt(ssk);
//        String ssk = CidUtil.getCid(imei, ChinaCmccAppUtils.getRandomMac4Qemu(), ChinaCmccAppUtils.getRandomHexNumStr(16));
//        JSONObject jsonObject = new JSONObject();
//        jsonObject.put("ak", AK);
//        jsonObject.put("cid", ssk);
//        jsonObject.put("city", cityCode);
//        jsonObject.put("ctid", ssk);
//        jsonObject.put("cv", "6.7.6");
//        jsonObject.put("en", "0");
//        jsonObject.put("imei", imei + "");
//        jsonObject.put("nt", "3");
//        jsonObject.put("prov", provinceCode);
//
//        JSONObject reqBody = new JSONObject();
//        reqBody.put("channelCode", CHANNEL_CODE);
//        reqBody.put("cityCode", "");
//        reqBody.put("imei", imei + "");
//        reqBody.put("provinceCode", "");
//        reqBody.put("ssk", ssk);
//
//        jsonObject.put("reqBody", reqBody);
//        jsonObject.put("sb", SYSTEM_BRAND);
//        jsonObject.put("sn", SYSTEM_NUMBER);
//        jsonObject.put("sp", SYSTEM_PIXEL);
//        jsonObject.put("st", "1");
//        jsonObject.put("sv", SYSTEM_VERSION);
//        jsonObject.put("t", "");
//        jsonObject.put("tel", "99999999999");
//        jsonObject.put("xc", CHANNEL_CODE);
//
////        InitPo initPo = new InitPo();
////        initPo.setAk(AK);
////        initPo.setCid(ssk);
////        initPo.setCtid(ssk);
////        initPo.setCity(cityCode);
////        initPo.setCv("6.7.6");
////        initPo.setEn("0");
////        initPo.setImei(imei +"");
////        initPo.setNt("3");
////        initPo.setProv(provinceCode);
////
////        initPo.setSb(SYSTEM_BRAND);
////        initPo.setSn(SYSTEM_NUMBER);
////        initPo.setSp(SYSTEM_PIXEL);
////        initPo.setSt("1");
////        initPo.setSv(SYSTEM_VERSION);
////        initPo.setT("");
////        initPo.setTel("99999999999");
////        initPo.setXc(CHANNEL_CODE);
////        initPo.setXk("");
////        JSONObject jsonObject = new JSONObject();
////        reqBody.put("channelCode", CHANNEL_CODE);
////        reqBody.put("cityCode", "");
////        reqBody.put("imei", imei);
////        reqBody.put("provinceCode", "");
////        reqBody.put("ssk", ssk);
////
////        initPo.setReqBody(jsonObject);
//
//
//        return jsonObject;
//    }
//
//
//    @Override
//    public CarrierResult doLogin() {
//        try {
//            log.info("[{}] 中国移动APP 登录开始，province:[{}] city:[{}]", context.getTaskId(), context.getMobileHCodeDto().getProvTelName(), context.getMobileHCodeDto().getCityName());
//            if (StringUtils.isEmpty(context.getPwd())) {
//                log.info("[{}]中国移动APP 账号密码不匹配，密码为空");
//                messageHelper.sendErrorMessage(context.getTaskId(), "账号密码不匹配");
//                return CarrierResult.FAIL;
//            }
//            //初始化
//            encryptedPhone = ChinaCmccAppUtils.rsaEncrypt(context.getPhone());
//            encryptedPwd = ChinaCmccAppUtils.rsaEncrypt(context.getPwd());
//            initHttpClientHeaders(getHttpClient());
//            getHttpClient().getConfig().setMaxRetryTimes(3);
//            loginRequestBodyJson = getLoginRequestBodyJson();
//            String url = "https://clientaccess.10086.cn/biz-orange/DN/init/startInit";
//            String param = loginRequestBodyJson.toJSONString();
//            setXSHeaderValue(getHttpClient(), url, param);
//            String result = sendPost(url, EncryptUtils.encryptPostPram(param), true);
//
//            if (StringUtils.isEmpty(result)) {
//                log.info("[{}]中国移动APP 访问获取ssv失败，官网响应为空");
//                messageHelper.sendFailMessage(context.getTaskId(), "官网系统繁忙，请稍后重试");
//                return CarrierResult.EXCEPTION;
//            } else if (!result.contains("\"retCode\":\"000000\"")) {
//                log.info("[{}]中国移动APP 访问获取ssv失败,官网响应异常");
//                messageHelper.sendFailMessage(context.getTaskId(), "官网系统繁忙，请稍后重试");
//                return CarrierResult.EXCEPTION;
//            }
//            String ssk = RegexUtils.matchValue("\"ssv\":\"(.*?)\"", result);
//            ssv = ssk;
//            if (StringUtils.isEmpty(ssk)) {
//                log.info("[{}]中国移动app参数ssk获取失败", context.getTaskId());
//                messageHelper.sendFailMessage(context.getTaskId(), "官网系统繁忙，请稍后重试");
//                return CarrierResult.EXCEPTION;
//            }
//            loginRequestBodyJson.put("xk", ssk);
//            DynamicCodeHelper dynamicCodeHelper = new DynamicCodeHelper() {
//                @Override
//                public CarrierResult sendDynamicCode() {
//                    return sendSms();
//                }
//
//                @Override
//                public CarrierResult verifyDynamicCode(String dynamicCode) {
//                    return verifySms(dynamicCode);
//                }
//            };
//            CarrierResult crawlResult = dynamicAuthorization(60, dynamicCodeHelper);
//            if (!CarrierResult.SUCCESS.equals(crawlResult)) {
//                return crawlResult;
//            }
//            return checkCallDetail();
//        } catch (Exception e) {
//            log.error("[{}]中国移动APP 登录异常", context.getTaskId(), e);
//            messageHelper.sendFailMessage(context.getTaskId(), "登录异常");
//            return CarrierResult.EXCEPTION;
//        }
//    }
//
//
//    /**
//     * 发送登录短信验证码
//     *
//     * @return
//     */
//    private CarrierResult sendSms() {
//
//
////        InitPo initPo = new InitPo();
////        initPo.setAk(AK);
////        initPo.setCid(CidUtil.getCid(imei,mac,androidId));
////        initPo.setCtid(CidUtil.getCid(imei,mac,androidId));
////        initPo.setCity("010");
////        initPo.setCv(Consts.VERSION);
////        initPo.setEn("0");
////        initPo.setImei(imei);
////        initPo.setNt("3");
////        initPo.setSb(sb);
////        initPo.setSn(sn);
////        initPo.setSp(sp);
////        initPo.setSt(Consts.ST);
////        initPo.setSv(sv);
////        initPo.setT("");
////        initPo.setTel(Consts.TEL);
////        initPo.setXc(Consts.CHANNEL);
////        initPo.setXk(ssv);
////        initPo.setProv("100");
////        JSONObject jsonObject = new JSONObject();
////        jsonObject.put("cellNum",phone);
////
////        initPo.setReqBody(jsonObject);
////        String param = EncryptUtils.encryptPostPram(JSON.toJSONString(initPo));
//
//        JSONObject reqBody = new JSONObject();
//        reqBody.put("cellNum", context.getPhone());
//        loginRequestBodyJson.put("reqBody", reqBody);
////        String time = MD5Utils.toMD5(getNowTime());
////        loginRequestBodyJson.put("t", time);
////        loginRequestBodyJson.put("tel", context.getPhone());
////        loginRequestBodyJson.put("xk", ssv);
//        String param = loginRequestBodyJson.toJSONString();
//        String url = "https://clientaccess.10086.cn/biz-orange/LN/uamrandcode/sendMsgLogin";
//        setXSHeaderValue(getHttpClient(), url, param);
//        String result = sendPost(url, EncryptUtils.encryptPostPram(param), true);
//        if (StringUtils.isEmpty(result)) {
//            log.info("[{}]中国移动APP 登录短信发送失败，官网响应为空", context.getTaskId());
//            messageHelper.sendFailMessage(context.getTaskId(), "登录短信验证码发送失败");
//            return CarrierResult.EXCEPTION;
//        } else if (result.contains("\"retCode\":\"000000\"")) {
//            log.info("[{}]中国移动APP 登录短信发送成功", context.getTaskId());
//            return CarrierResult.SUCCESS;
//        } else {
//            String retDesc = RegexUtils.matchValue("\"retDesc\":\"(.*?)\"", result);
//            if (retDesc.contains("您输入的号码不是移动手机号码")) {
//                log.info("[{}]中国移动APP 登录短信发送失败，您输入的不是移动手机号码", context.getTaskId());
//                messageHelper.sendErrorMessage(context.getTaskId(), "登录短信发送失败，您输入的不是移动手机号码");
//                return CarrierResult.FAIL;
//            } else {
//                log.info("[{}]中国移动APP 登录短信发送失败,{}", context.getTaskId(), retDesc);
//                messageHelper.sendFailMessage(context.getTaskId(), "登录短信发送失败");
//                return CarrierResult.EXCEPTION;
//            }
//        }
//    }
//
//    private InitPo getNewVerifySmsParam() {
//        String provinceCode = context.getMobileHCodeDto().getProvTelCode();
//        String cityCode = context.getMobileHCodeDto().getTelCode();
//        if (provinceCode == null || StringUtils.isEmpty(provinceCode.trim()) || cityCode == null || StringUtils.isEmpty(cityCode.trim())) {
//            cityCode = "021";
//            provinceCode = "210";
//            log.info("[{}]中国移动app 网关传参provinceCode或cityCode有null或空值，将采用上海地provinceCode:[{}],cityCode:[{}]", context.getTaskId(), provinceCode, cityCode);
//        }
//
//
//        String sv = "7.1"; //sdk版本
//        String sp = "1920x1080";//分辨率
//        String sb = "HUAWEI";//品牌
//        String sn = "Nova4";//型号
//        String imei = ChinaCmccAppUtils.getRandomIMEI() + "";
//        String mac = ChinaCmccAppUtils.getRandomMac4Qemu() + "";
//        String androidId = ChinaCmccAppUtils.getRandomHexNumStr(16) + "";
//        InitPo initPo = new InitPo();
//        initPo.setAk(Consts.AK);
//        initPo.setCid(CidUtil.getCid(imei, mac, androidId));
//        initPo.setCtid(CidUtil.getCid(imei, mac, androidId));
//        initPo.setCity(cityCode);
//        initPo.setCv(Consts.VERSION);
//        initPo.setEn("0");
//        initPo.setImei(imei);
//        initPo.setNt("3");
//        initPo.setSb(sb);
//        initPo.setSn(sn);
//        initPo.setSp(sp);
//        initPo.setSt(Consts.ST);
//        initPo.setSv(sv);
//        initPo.setT("");
//        initPo.setTel(Consts.TEL);
//        initPo.setXc(Consts.CHANNEL);
//        initPo.setXk(ssv);
//        initPo.setProv(provinceCode);
//        return initPo;
//    }
//
//    /**
//     * 校验登录短信验证码
//     *
//     * @param dynamicCode
//     * @return
//     */
//    private CarrierResult verifySms(String dynamicCode) {
//        if (StringUtils.isEmpty(dynamicCode)) {
//            return CarrierResult.RETRY;
//        }
//        InitPo initPo = getNewVerifySmsParam();
//        JSONObject reqBody = new JSONObject();
//        reqBody.put("cellNum", encryptedPhone);
//        reqBody.put("businessCode", "01");
//        reqBody.put("imei", initPo.getImei());
//        reqBody.put("passwd", encryptedPwd);
//        reqBody.put("smsPasswd", dynamicCode);
//
//        initPo.setReqBody(reqBody);
////        loginRequestBodyJson.put("reqBody", reqBody);
//        String url = "https://clientaccess.10086.cn/biz-orange/LN/tempIdentCode/getTmpIdentCode";
////        String param = loginRequestBodyJson.toJSONString();
//        String param = JSON.toJSONString(initPo);
//        setXSHeaderValue(getHttpClient(), url, param);
//        SimpleHttpResponse httpResponse = getHttpClient().sendPostResponse(url, EncryptUtils.encryptPostPram(param), Encodings.UTF8.getValue(), null);
//        String result = httpResponse.getStringData();
//        if (StringUtils.isEmpty(result)) {
//            log.info("[{}]中国移动APP 登录短信校验失败，官网响应为空", context.getTaskId());
//            messageHelper.sendFailMessage(context.getTaskId(), "登录短信校验失败");
//            return CarrierResult.EXCEPTION;
//        }
//        result = ChinaCmccAppUtils.AesDecrypt(result);
//        String retDesc = RegexUtils.matchValue("\"retDesc\":\"(.*?)\"", result);
//        retDesc = retDesc == null ? "" : retDesc;
//        String retCode = RegexUtils.matchValue("\"retCode\":\"(.*?)\"", result);
//        if (!"000000".equals(retCode) && !retDesc.contains("短信验证通过")) {
//            if ("213122".equals(retCode) || "312000".equals(retCode)) {
//                log.info("[{}]中国移动app 账号有误或不存在，请重新输入", context.getTaskId());
//                messageHelper.sendErrorMessage(context.getTaskId(), "账号有误或不存在");
//                return CarrierResult.FAIL;
//            } else if ("213120".equals(retCode)) {
//                log.info("[{}]中国移动app 账号密码不匹配，请重新输入", context.getTaskId());
//                messageHelper.sendErrorMessage(context.getTaskId(), "账号密码不匹配");
//                return CarrierResult.FAIL;
//            } else if ("213001".equals(retCode) || "213004".equals(retCode) || "213005".equals(retCode)) {
//                log.info("[{}]中国移动app 密码错误次数超过上限，请24小时后再试", context.getTaskId());
//                messageHelper.sendFailMessage(context.getTaskId(), "密码错误次数超过上限，请24小时后再试");
//                return CarrierResult.EXCEPTION;
//            } else if ("206094".equals(retCode)) {
//                log.info("[{}]中国移动app 官网正在系统升级", context.getTaskId());
//                messageHelper.sendFailMessage(context.getTaskId(), "官网正在系统升级");
//                return CarrierResult.EXCEPTION;
//            } else if ("213147".equals(retCode) || "213146".equals(retCode)) {
//                log.info("[{}]中国移动app 账号已被锁定，请24小时后再试", context.getTaskId());
//                messageHelper.sendFailMessage(context.getTaskId(), "账号已被锁定，请24小时后再试");
//                return CarrierResult.EXCEPTION;
//            } else if ("233025".equals(retCode)) {
//                log.info("[{}]中国移动app 账号已被锁定", context.getTaskId());
//                messageHelper.sendFailMessage(context.getTaskId(), "账号已被锁定");
//                return CarrierResult.EXCEPTION;
//            } else if ("213010".equals(retCode)) {
//                return CarrierResult.SKIP;
//            }
//            //下面两种不会出现，已在前面拦截掉
//            //310000 参数不合理，null的情况
//            //310001 参数为空
//            else if ("310002".equals(retCode)) {
//                //参数长度非法
//                if (retDesc.contains("smsPasswd")) {
//                    return CarrierResult.RETRY;
//                } else if (retDesc.contains("passwd")) {
//                    log.info("[{}]中国移动app 账号密码不匹配，请重新输入", context.getTaskId());
//                    messageHelper.sendErrorMessage(context.getTaskId(), "账号密码不匹配");
//                    return CarrierResult.FAIL;
//                } else {
//                    log.info("[{}]中国移动app 官网系统繁忙，请稍后重试", context.getTaskId());
//                    messageHelper.sendFailMessage(context.getTaskId(), "官网系统繁忙，请稍后重试");
//                    return CarrierResult.EXCEPTION;
//                }
//            } else if ("310003".equals(retCode)) {
//                //参数非纯数字
//                if (retDesc.contains("smsPasswd")) {
//                    return CarrierResult.RETRY;
//                } else if (retDesc.contains("passwd")) {
//                    log.info("[{}]中国移动app 请输入纯数字密码", context.getTaskId());
//                    messageHelper.sendErrorMessage(context.getTaskId(), "请输入纯数字密码");
//                    return CarrierResult.FAIL;
//                } else {
//                    log.info("[{}]中国移动app 登录短信验证码校验失败，未知原因[310003]", context.getTaskId());
//                    messageHelper.sendFailMessage(context.getTaskId(), "官网系统繁忙，请稍后重试");
//                    return CarrierResult.EXCEPTION;
//                }
//            } else if ("310004".equals(retCode)) {
//                //参数含有非法字符
//                if (retDesc.contains("smsPasswd")) {
//                    return CarrierResult.RETRY;
//                } else if (retDesc.contains("passwd")) {
//                    log.info("[{}]中国移动app 账号密码不匹配，请重新输入", context.getTaskId());
//                    messageHelper.sendErrorMessage(context.getTaskId(), "账号密码不匹配");
//                    return CarrierResult.FAIL;
//                } else {
//                    log.info("[{}]中国移动app 登录短信验证码校验失败，未知原因[310004]", context.getTaskId());
//                    messageHelper.sendFailMessage(context.getTaskId(), "登录短信验证码校验失败");
//                    return CarrierResult.EXCEPTION;
//                }
//            } else {
//                log.info("[{}]中国移动app 登录短信验证码校验失败{}", context.getTaskId(), result);
//                messageHelper.sendFailMessage(context.getTaskId(), "官网系统繁忙，请稍后重试");
//                return CarrierResult.EXCEPTION;
//            }
//        }
//        cacheContainer.putString(HttpRecordType.basic.name(), result);
//        String cityCode = RegexUtils.matchValue("\"cityCode\":\"(.*?)\"", result);
//        String provinceCode = RegexUtils.matchValue("\"provinceCode\":\"(.*?)\"", result);
//        //都不为空再赋值
//        if (!StringUtils.isEmpty(cityCode) && !StringUtils.isEmpty(provinceCode)) {
//            loginRequestBodyJson.put("city", cityCode);
//            loginRequestBodyJson.put("prov", provinceCode);
//        }
//        //更新cookie
//        String rspCookie = null;
//        for (Header header : httpResponse.getHeaders()) {
//            if ("Set-Cookie".equalsIgnoreCase(header.getName())) {
//                rspCookie = header.getValue();
//                break;
//            }
//        }
//        if (null != rspCookie) {
//            String[] cookies = rspCookie.split(";");
//            String name;
//            String value;
//            String[] nameValue;
//            for (String cookie : cookies) {
//                nameValue = cookie.split("=");
//                name = nameValue[0].trim();
//                value = nameValue.length > 1 ? nameValue[1].trim() : "";
//                if ("path".equalsIgnoreCase(name) || "domain".equalsIgnoreCase(name) || "expires".equalsIgnoreCase(name)) {
//                    continue;
//                }
//                getHttpClient().addCookie(name, value, "/", ".10086.cn");
//            }
//        }
//        return CarrierResult.SUCCESS;
//    }
//
//    /**
//     * 校验通话详单
//     *
//     * @return
//     */
//    private CarrierResult checkCallDetail() {
//        try {
//            JSONObject reqBody = new JSONObject();
//            String checkMonth = DateUtils.getCurrentDate("yyyy-MM");
//            reqBody.put("billMonth", checkMonth);
//            reqBody.put("cellNum", context.getPhone());
//            reqBody.put("page", 1);
//            reqBody.put("tmemType", "02");
//            reqBody.put("unit", UNIT);
//            loginRequestBodyJson.put("reqBody", reqBody);
//            loginRequestBodyJson.put("t", MD5Utils.toMD5(getNowTime()));
//            String url = "https://clientaccess.10086.cn/biz-orange/BN/queryDetail/getDetail";
//            String param = loginRequestBodyJson.toJSONString();
//            setXSHeaderValue(getHttpClient(), url, param);
//            String result = sendPost(url, EncryptUtils.encryptPostPram(param), true);
//            if (StringUtils.isEmpty(result)) {
//                log.info("[{}]中国移动APP 检测采集[{}]通话详单官网响应为空", context.getTaskId(), checkMonth);
//                messageHelper.sendFailMessage(context.getTaskId(), "官网系统繁忙，请稍后重试");
//                return CarrierResult.EXCEPTION;
//            } else if (result.contains("\"retCode\":\"000000\"") || result.contains("无详单数据")) {
//                log.info("[{}]中国移动APP 检测采集[{}]通话详单通过", context.getTaskId(), checkMonth);
//                return CarrierResult.SUCCESS;
//            } else {
//                String retDesc = RegexUtils.matchValue("\"retDesc\":\"(.*?)\"", result);
//                if (retDesc.contains("您办理了详单禁查业务")) {
//                    log.info("[{}]中国移动APP 已开通详单禁查功能，请关闭后再试", context.getTaskId());
//                    messageHelper.sendErrorMessage(context.getTaskId(), "已开通详单禁查功能，请关闭后再试");
//                    return CarrierResult.FAIL;
//                } else {
//                    log.info("[{}]中国移动APP 通话详单校验失败[{}]", context.getTaskId(), retDesc);
//                    messageHelper.sendFailMessage(context.getTaskId(), "官网系统繁忙，请稍后重试");
//                    return CarrierResult.EXCEPTION;
//                }
//            }
//        } catch (Exception e) {
//            log.error("[{}]中国移动App 检测采集详单异常", context.getTaskId(), e);
//            messageHelper.sendFailMessage(context.getTaskId(), "官网系统繁忙，请稍后重试");
//            return CarrierResult.EXCEPTION;
//        }
//    }
//
//
//    @Override
//    public void processBaseInfo() {
//        try {
//            String url = "https://clientaccess.10086.cn/biz-orange/BN/userInformationService/getUserInformation";
//            JSONObject jsonObject = (JSONObject) loginRequestBodyJson.clone();
//            JSONObject reqBody = new JSONObject();
//            reqBody.put("cellNum", context.getPhone());
//            jsonObject.put("reqBody", reqBody);
//            String param = jsonObject.toJSONString();
//            setXSHeaderValue(getHttpClient(), url, param);
//            String result = "";
//            int cs = 0;
//            while (cs < 5) {
//                result = sendPost(url, EncryptUtils.encryptPostPram(param), true);
//                if (org.springframework.util.StringUtils.isEmpty(result) || !result.contains("\"retCode\":\"000000\"")) {
//                    cs++;
//                    log.info("[{}]中国移动APP 基本信息采集失败-[第{}次]---[结果：{}]", context.getTaskId(), cs, result);
//                } else {
//                    log.info("[{}]中国移动APP 基本信息采集成功", context.getTaskId());
//                    break;
//                }
//            }
//            cacheContainer.putString(HttpRecordType.basic.name(), result);
//            //采集套餐信息
//            url = "https://clientaccess.10086.cn/biz-orange/BN/queryBuinessService/getQueryBusiness";
//            setXSHeaderValue(getHttpClient(), url, param);
//            result = sendPost(url, EncryptUtils.encryptPostPram(param), true);
//            if (org.springframework.util.StringUtils.isEmpty(result) || !result.contains("\"retCode\":\"000000\"")) {
//                log.info("[{}]中国移动APP 套餐信息采集失败", context.getTaskId());
//            } else {
//                log.info("[{}]中国移动APP 套餐信息采集成功", context.getTaskId());
//            }
//            cacheContainer.putString(HttpRecordType.packageName.name(), result);
//            //采集余额信息
//            url = "https://clientaccess.10086.cn/biz-orange/BN/realFeeQuery/getRealFee";
//            setXSHeaderValue(getHttpClient(), url, param);
//            result = sendPost(url, EncryptUtils.encryptPostPram(param), true);
//            if (org.springframework.util.StringUtils.isEmpty(result) || !result.contains("\"retCode\":\"000000\"")) {
//                log.info("[{}]中国移动APP 余额信息采集失败", context.getTaskId());
//            } else {
//                log.info("[{}]中国移动APP 余额信息采集成功", context.getTaskId());
//            }
//            cacheContainer.putString(HttpRecordType.amount.name(), result);
//        } catch (Exception ex) {
//            log.error("[{}] 中国移动 APP 采集基本信息出现异常 ", context.getTaskId(), ex);
//            messageHelper.sendMessage(context.getTaskId(), "采集基本信息出现异常");
//        }
//    }
//
//    @Override
//    public void processCallRecordInfo() {
//        getDetailInfo(CarrierModule.CALL);
//    }
//
//    @Override
//    public void processSmsInfo() {
//        getDetailInfo(CarrierModule.SMS);
//    }
//
//    @Override
//    public void processNetInfo() {
//        getDetailInfo(CarrierModule.NET);
//    }
//
//    /**
//     * 查询详单
//     */
//    private void getDetailInfo(CarrierModule carrierModule) {
//        try {
//            List<QueryTimeInfo> queryTimeInfos = getFullCrawlMonth(TimeTypeEnum.YYYYMM_1);
//            if (CollectionUtils.isEmpty(queryTimeInfos)) {
//                return;
//            }
//            List<String> list = new ArrayList<>();
//            String tmemType = "02";
//            HttpRecordType httpRecordType = HttpRecordType.call;
//            if (CarrierModule.SMS.equals(carrierModule)) {
//                tmemType = "03";
//                httpRecordType = HttpRecordType.sms;
//            } else if (CarrierModule.NET.equals(carrierModule)) {
//                tmemType = "04";
//                httpRecordType = HttpRecordType.net;
//            }
//            String param;
//            String month;
//            String result;
//            JSONObject reqBody = new JSONObject();
//            reqBody.put("cellNum", context.getPhone());
//            reqBody.put("page", 1);
//            reqBody.put("tmemType", tmemType);
//            reqBody.put("unit", UNIT);
//            JSONObject jsonObject = (JSONObject) loginRequestBodyJson.clone();
//            String url = "https://clientaccess.10086.cn/biz-orange/BN/queryDetail/getDetail";
//            for (int i = 0; i < queryTimeInfos.size(); i++) {
//                month = queryTimeInfos.get(i).getStartTime();
//                reqBody.put("billMonth", month);
//                //根据返回结果，动态更新总页数以及每页记录条数
//                int pageCount = 1;
//                for (int j = 1; j <= pageCount; j++) {
//                    reqBody.put("page", j);
//                    jsonObject.put("reqBody", reqBody);
//                    param = jsonObject.toJSONString();
//                    setXSHeaderValue(getHttpClient(), url, param);
//                    result = sendPost(url, EncryptUtils.encryptPostPram(param), true);
//                    list.add(result);
//                    if (StringUtils.isEmpty(result)) {
//                        log.info("[{}]中国移动APP {}{}第{}页采集失败，官网响应为空", context.getTaskId(), month, carrierModule.getDesc(), j);
//                    } else if (result.contains(NO_DETAIL)) {
//                        log.info("[{}]中国移动APP {}{}第{}页采集失败，{}", context.getTaskId(), month, carrierModule.getDesc(), j, NO_DETAIL);
//                    } else if (!result.contains("\"retCode\":\"000000\"")) {
//                        String retDesc = RegexUtils.matchValue("\"retDesc\":\"(.*?)\"", result);
//                        log.info("[{}]中国移动APP {}{}第{}页采集失败，{}", context.getTaskId(), month, carrierModule.getDesc(), j, retDesc);
//                    } else {
//                        if (j == 1) {
//                            String totalCountStr = RegexUtils.matchValue("\"totalCount\":\\s*\"(\\d*)\"", result);
//                            if (StringUtils.isEmpty(totalCountStr)) {
//                                log.info("[{}]中国移动APP [{}][{}]总条数匹配失败，只采集第1页", context.getTaskId(), month, carrierModule.getDesc());
//                            } else {
//                                int totalCount = Integer.parseInt(totalCountStr);
//                                int pageSize = getDetailSize(carrierModule, result);
//                                //广西移动 返回201条，有一条与前一页重复，且参数unit不能调整
//                                if (totalCount > pageSize) {
//                                    pageCount = getPageNum(totalCount, UNIT);
//                                    log.info("[{}]中国移动APP 采集[{}][{}]第1页成功！总页数：[{}] totalCount:[{}], pageSize:[{}] {}", context.getTaskId(), month, carrierModule.getDesc(), pageCount, totalCountStr, pageSize
//                                            , pageSize > UNIT ? String.format("注意：pageSize虽大于%s，但由于官网限制仍按%s处理，重复数据只能通过入库去重", UNIT, UNIT) : "");
//                                } else {
//                                    log.info("[{}]中国移动APP 采集[{}][{}]成功！totalCount:[{}], pageSize:[{}]", context.getTaskId(), month, carrierModule.getDesc(), totalCountStr, pageSize);
//                                }
//                            }
//                        } else {
//                            log.info("[{}]中国移动APP 采集[{}][{}]第{}页成功！总页数：[{}]", context.getTaskId(), month, carrierModule.getDesc(), j, pageCount);
//                        }
//                    }
//                }
//            }
//            cacheContainer.putStrings(httpRecordType.name(), list);
//        } catch (Exception e) {
//            log.error("[{}]中国移动APP 采集{}异常", context.getTaskId(), carrierModule.getDesc(), e);
//            messageHelper.sendMessage(context.getTaskId(), "采集" + carrierModule.getDesc() + "异常");
//        }
//    }
//
//
//    @Override
//    protected void processBillInfo() {
//        try {
//            //采集当前积分
//            String url = "https://clientaccess.10086.cn/biz-orange/BN/scoreQueryService/getScoreQuery";
//            JSONObject jsonObject = (JSONObject) loginRequestBodyJson.clone();
//            JSONObject reqBody = new JSONObject();
//            reqBody.put("cellNum", context.getPhone());
//            jsonObject.put("reqBody", reqBody);
//            String param = jsonObject.toJSONString();
//            setXSHeaderValue(getHttpClient(), url, param);
//            String result = sendPost(url, EncryptUtils.encryptPostPram(param), true);
//            if (StringUtils.isEmpty(result) || !result.contains("\"retCode\":\"000000\"")) {
//                log.info("[{}]中国移动APP 当前积分采集失败", context.getTaskId());
//            } else {
//                log.info("[{}]中国移动APP 当前积分采集成功", context.getTaskId());
//            }
//            cacheContainer.putString(HttpRecordType.points.name() + "_1", result);
//            //采集积分历史（官网按月，代码尝试最近6个月）
//            String endMonth = DateUtils.getCurrentDate("yyyy-MM-dd");
//            Calendar calendar = Calendar.getInstance();
//            calendar.add(Calendar.MONTH, -5);
//            String bgnMonth = DateUtils.getFirstDay(calendar, "yyyy-MM-dd");
//            url = "https://clientaccess.10086.cn/biz-orange/BN/scoreRecordService/getScoreRecord";
//            reqBody.put("startEnd", endMonth);
//            reqBody.put("startTime", bgnMonth);
//            jsonObject.put("reqBody", reqBody);
//            param = jsonObject.toJSONString();
//            setXSHeaderValue(getHttpClient(), url, param);
//            result = sendPost(url, EncryptUtils.encryptPostPram(param), true);
//            if (org.springframework.util.StringUtils.isEmpty(result) || !result.contains("\"retCode\":\"000000\"")) {
//                log.info("[{}]中国移动APP 积分历史采集失败", context.getTaskId());
//            } else {
//                log.info("[{}]中国移动APP 积分历史采集成功", context.getTaskId());
//            }
//            cacheContainer.putString(HttpRecordType.points.name() + "_2", result);
//            //账单
//            url = "https://clientaccess.10086.cn/biz-orange/BN/historyBillsService/getHistoryBills";
//            endMonth = DateUtils.getCurrentDate("yyyy-MM");
//            bgnMonth = DateUtils.getFirstDay(calendar, "yyyy-MM");
//            reqBody = new JSONObject();
//            reqBody.put("bgnMonth", bgnMonth);
//            reqBody.put("cellNum", context.getPhone());
//            reqBody.put("endMonth", endMonth);
//            jsonObject.put("reqBody", reqBody);
//            param = jsonObject.toJSONString();
//            setXSHeaderValue(getHttpClient(), url, param);
//            result = sendPost(url, EncryptUtils.encryptPostPram(param), true);
//            if (StringUtils.isEmpty(result) || !result.contains("\"retCode\":\"000000\"")) {
//                log.info("[{}]中国移动APP 最近6个月账单采集失败", context.getTaskId());
//            } else {
//                log.info("[{}]中国移动APP 最近6个月账单采集成功", context.getTaskId());
//            }
//            cacheContainer.putString(HttpRecordType.bill.name(), result);
//        } catch (Exception e) {
//            log.error("[{}] 中国移动APP 采集账单信息出现异常 ", context.getTaskId(), e);
//            messageHelper.sendMessage(context.getTaskId(), "采集账单信息出现异常");
//        }
//    }
//
//    @Override
//    protected void processPackageItemInfo() {
//        try {
//            String url = "https://clientaccess.10086.cn/biz-orange/BN/newComboMealResouceUnite/getNewComboMealResource";
//            JSONObject jsonObject = (JSONObject) loginRequestBodyJson.clone();
//            JSONObject reqBody = new JSONObject();
//            reqBody.put("cellNum", context.getPhone());
//            reqBody.put("tag", "3");
//            jsonObject.put("reqBody", reqBody);
//            String param = jsonObject.toJSONString();
//            setXSHeaderValue(getHttpClient(), url, param);
//            String result = sendPost(url, EncryptUtils.encryptPostPram(param), true);
//            if (org.springframework.util.StringUtils.isEmpty(result) || !result.contains("\"retCode\":\"000000\"")) {
//                log.info("[{}]中国移动APP 当月套餐余量采集失败", context.getTaskId());
//            } else {
//                log.info("[{}]中国移动APP 当月套餐余量采集成功", context.getTaskId());
//            }
//            cacheContainer.putString(HttpRecordType.packageItem.name(), result);
//        } catch (Exception e) {
//            log.error("[{}] 中国移动APP 采集套餐余量出现异常 ", context.getTaskId(), e);
//            messageHelper.sendMessage(context.getTaskId(), "采集套餐余量出现异常");
//        }
//
//    }
//
//    @Override
//    protected void processUserFamilyMember() {
//    }
//
//    @Override
//    protected void processUserRechargeItemInfo() {
//        try {
//            String endMonth = DateUtils.getCurrentDate("yyyy-MM-dd");
//            Calendar calendar = Calendar.getInstance();
//            calendar.add(Calendar.MONTH, -5);
//            String bgnMonth = DateUtils.getFirstDay(calendar, "yyyy-MM-dd");
//            JSONObject jsonObject = (JSONObject) loginRequestBodyJson.clone();
//            JSONObject reqBody = new JSONObject();
//            reqBody.put("cellNum", context.getPhone());
//            reqBody.put("startEnd", endMonth);
//            reqBody.put("startTime", bgnMonth);
//            jsonObject.put("reqBody", reqBody);
//            String param = jsonObject.toJSONString();
//            String url = "https://clientaccess.10086.cn/biz-orange/BN/payFeesHistory/getPayFeesHistory";
//            setXSHeaderValue(getHttpClient(), url, param);
//            String result = sendPost(url, EncryptUtils.encryptPostPram(param), true);
//            if (StringUtils.isEmpty(result)) {
//                log.info("[{}]中国移动APP 采集最近6个月的充值信息失败：响应为空", context.getTaskId());
//            } else if (result.contains("\"retCode\":\"000000\"")) {
//                log.info("[{}]中国移动APP 采集最近6个月的充值信息成功", context.getTaskId());
//            } else {
//                log.info("[{}]中国移动APP 采集最近6个月的充值信息失败：" + RegexUtils.matchValue("\"retDesc\":\"(.*?)\"", result), context.getTaskId());
//            }
//            cacheContainer.putString(HttpRecordType.recharge.name(), result);
//        } catch (Exception e) {
//            log.error("[{}] 中国移动APP 采集充值信息出现异常", context.getTaskId(), e);
//            messageHelper.sendMessage(context.getTaskId(), "采集充值信息出现异常");
//        }
//    }
//
//    @Override
//    protected void logout() {
//    }
//
//    @Override
//    public void doParse() {
//        CarrierInfo carrierInfo = new CarrierInfo();
//        parsing(parser, carrierInfo);
//        messageHelper.saveUpdateCarrierInfo(context, carrierInfo);
//    }
//}
