package com.imooc.utils;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.net.*;
import java.nio.charset.Charset;
import java.util.Map;
import java.util.Vector;
import java.util.zip.GZIPInputStream;

public class HttpRequest {
    final static Logger logger = LoggerFactory.getLogger(HttpRequest.class);
    private String defaultContentEncoding;
    private int connectTimeout = 1000;
    private int readTimeout = 1000;

    public HttpRequest() {
        this.defaultContentEncoding = Charset.defaultCharset().name();
    }


    public HttpResponse sendGet(String desc, String urlString, final Map<String, String> proxySettings) throws IOException {
        return this.send(desc, urlString, "GET", null, null, proxySettings);
    }


    public HttpResponse sendGet(String desc, String urlString, Map<String, String> params, final Map<String, String> proxySettings)
            throws IOException {
        return this.send(desc, urlString, "GET", params, null, proxySettings);
    }


    public HttpResponse sendGet(String desc, String urlString, Map<String, String> params,
                                Map<String, String> headers, final Map<String, String> proxySettings) throws IOException {

        return this.send(desc, urlString, "GET", params, headers, proxySettings);
    }


    public HttpResponse sendPost(String desc, String urlString, final Map<String, String> proxySettings) throws IOException {
        return this.send(desc, urlString, "POST", null, null, proxySettings);
    }


    public HttpResponse sendPost(String desc, String urlString, Map<String, String> params, final Map<String, String> proxySettings)
            throws IOException {

        return this.send(desc, urlString, "POST", params, null, proxySettings);
    }


    public HttpResponse sendPost(String desc, String urlString, Map<String, String> params,
                                 Map<String, String> headers, final Map<String, String> proxySettings) throws IOException {

        return this.send(desc, urlString, "POST", params, headers, proxySettings);
    }

    private HttpResponse send(String desc, String urlString, String method,
                              Map<String, String> parameters, Map<String, String> headers, final Map<String, String> proxySettings)
            throws IOException {
        logger.info("%发起'获取:'{}'的请求%", desc);
        HttpURLConnection urlConnection = null;

        if (method.equalsIgnoreCase("GET") && parameters != null) {
            StringBuffer param = new StringBuffer();
            int i = 0;
            for (String key : parameters.keySet()) {
                if (i == 0)
                    param.append("?");
                else
                    param.append("&");
                param.append(key).append("=").append(URLEncoder.encode(parameters.get(key), "utf-8"));
                i++;
            }
            urlString += param;
        }
        URL url = new URL(urlString);
        logger.info("-Request URL = {}", urlString);
        logger.info("-Request Method = {}", method);
        if (proxySettings != null) {
            logger.info("-Request Proxy = {}" , proxySettings);
            Proxy proxy = new Proxy(Proxy.Type.HTTP, new InetSocketAddress(proxySettings.get("ip"), Integer.parseInt(proxySettings.get("port"))));
            urlConnection = (HttpURLConnection) url.openConnection(proxy);
            if (proxySettings.containsKey("username")) {
                Authenticator authenticator = new Authenticator() {
                    public PasswordAuthentication getPasswordAuthentication() {
                        return (new PasswordAuthentication(proxySettings.get("username"),
                                proxySettings.get("password").toCharArray()));
                    }
                };
                Authenticator.setDefault(authenticator);
            }
        } else {
            urlConnection = (HttpURLConnection) url.openConnection();
        }

        urlConnection.setRequestMethod(method);
        urlConnection.setDoOutput(true);
        urlConnection.setDoInput(true);
        urlConnection.setUseCaches(false);

        urlConnection.setConnectTimeout(connectTimeout);
        urlConnection.setReadTimeout(readTimeout);

        if (headers != null) {
            logger.info("-Request Headers = {}", headers);
            for (String key : headers.keySet()) {
                urlConnection.addRequestProperty(key, headers.get(key));
            }
        }
        if (method.equalsIgnoreCase("POST") && parameters != null) {
            logger.info("-Request Nvps = {}" , parameters);
            StringBuffer param = new StringBuffer();
            int i = 0;
            for (String key : parameters.keySet()) {
                if (i > 0) param.append("&");
                param.append(key).append("=").append(URLEncoder.encode(parameters.get(key), "utf-8"));
                i++;
            }
            System.out.println(param.toString());
            urlConnection.getOutputStream().write(param.toString().getBytes());
            urlConnection.getOutputStream().flush();
            urlConnection.getOutputStream().close();
        }
        logger.info("%收到'获取:{}'的应答%", desc);
        return this.makeContent(desc, urlString, urlConnection);
    }


    private HttpResponse makeContent(String desc, String urlString,
                                     HttpURLConnection urlConnection) throws IOException {
        HttpResponse response = new HttpResponse();
        try {
            InputStream in = urlConnection.getInputStream();
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(in));
            if ("gzip".equals(urlConnection.getContentEncoding()))
                bufferedReader = new BufferedReader(new InputStreamReader(new GZIPInputStream(in)));
            response.contentCollection = new Vector<String>();
            StringBuffer temp = new StringBuffer();
            String line = bufferedReader.readLine();
            while (line != null) {
                response.contentCollection.add(line);
                temp.append(line).append("\r\n");
                line = bufferedReader.readLine();
            }
            bufferedReader.close();

            String encoding = urlConnection.getContentEncoding();
            if (encoding == null)
                encoding = this.defaultContentEncoding;

            response.urlString = urlString;

            response.defaultPort = urlConnection.getURL().getDefaultPort();
            response.file = urlConnection.getURL().getFile();
            response.host = urlConnection.getURL().getHost();
            response.path = urlConnection.getURL().getPath();
            response.port = urlConnection.getURL().getPort();
            response.protocol = urlConnection.getURL().getProtocol();
            response.query = urlConnection.getURL().getQuery();
            response.ref = urlConnection.getURL().getRef();
            response.userInfo = urlConnection.getURL().getUserInfo();
            response.contentLength = urlConnection.getContentLength();

            response.content = new String(temp.toString().getBytes());
            response.contentEncoding = encoding;
            response.code = urlConnection.getResponseCode();
            response.message = urlConnection.getResponseMessage();
            response.contentType = urlConnection.getContentType();
            response.method = urlConnection.getRequestMethod();
            response.connectTimeout = urlConnection.getConnectTimeout();
            response.readTimeout = urlConnection.getReadTimeout();
            logger.info("-Request Status Code = {}" , response.getCode());
            if (response.getContent() != null) {
                logger.info("%获取:{} 成功%", desc);
                logger.info("-Request Body = {}", response.getContent());
            } else {
                logger.warn("%获取:{},失败：{}%", desc, response.getMessage());
            }
            return response;
        } catch (IOException e) {
            logger.warn("%获取:{ },出错:{} %", response.getMessage());
            throw e;
        } finally {
            if (urlConnection != null) {
                urlConnection.disconnect();
            }
        }
    }

    public static byte[] gunzip(byte[] bytes) {
        if (bytes == null || bytes.length == 0) {
            return null;
        }
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        ByteArrayInputStream in = new ByteArrayInputStream(bytes);
        try {
            GZIPInputStream ungzip = new GZIPInputStream(in);
            byte[] buffer = new byte[256];
            int n;
            while ((n = ungzip.read(buffer)) >= 0) {
                out.write(buffer, 0, n);
            }
        } catch (IOException e) {
            System.err.println("gzip uncompress error.");
            e.printStackTrace();
        }

        return out.toByteArray();
    }


    public String getDefaultContentEncoding() {
        return this.defaultContentEncoding;
    }


    public void setDefaultContentEncoding(String defaultContentEncoding) {
        this.defaultContentEncoding = defaultContentEncoding;
    }

    public int getConnectTimeout() {
        return connectTimeout;
    }

    public void setConnectTimeout(int connectTimeout) {
        this.connectTimeout = connectTimeout;
    }

    public int getReadTimeout() {
        return readTimeout;
    }

    public void setReadTimeout(int readTimeout) {
        this.readTimeout = readTimeout;
    }
}
