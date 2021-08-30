package com.imooc.utils;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.StringUtils;
public class ExportTextUtil {

    private static final Logger LOGGER = LoggerFactory.getLogger(ExportTextUtil.class);

    /**
     * 导出文本文件
     *
     * @param response
     * @param jsonString
     * @param fileName
     */
    public static void writeToTxt1 (HttpServletResponse response, String jsonString, String fileName) {//设置响应的字符集
        response.setCharacterEncoding("utf-8");
        //设置响应内容的类型
        response.setContentType("text/plain");
        //设置文件的名称和格式
        response.addHeader(
                "Content-Disposition",
                "attachment; filename="
                        + AbstractFileUtil.genAttachmentFileName(fileName, "JSON_FOR_UCC_")
                        + ".txt");//通过后缀可以下载不同的文件格式
        BufferedOutputStream buff = null;
        ServletOutputStream outStr = null;
        try {
            outStr = response.getOutputStream();
            buff = new BufferedOutputStream(outStr);
            buff.write(delNull(jsonString).getBytes("UTF-8"));
            buff.flush();
            buff.close();
        } catch (Exception e) {
            LOGGER.error("导出文件文件出错，e:{}", e);
        } finally {
            try {
                buff.close();
                outStr.close();
            } catch (Exception e) {
                LOGGER.error("关闭流对象出错 e:{}", e);
            }
        }
    }

    public static void writeToTxt2 (HttpServletResponse response, String jsonString, String fileName) {//设置响应的字符集
        response.setCharacterEncoding("utf-8");
        //设置响应内容的类型
        response.setContentType("text/plain");
        //设置文件的名称和格式
        response.addHeader(
                "Content-Disposition",
                "attachment; filename="
                        + AbstractFileUtil.genAttachmentFileName(fileName, "JSON_FOR_UCC_")
                        + ".txt");//通过后缀可以下载不同的文件格式
        BufferedOutputStream buff = null;
        ServletOutputStream outStr = null;
        try {
            outStr = response.getOutputStream();
            buff = new BufferedOutputStream(outStr);
            buff.write(delNull(jsonString).getBytes("UTF-8"));
            buff.flush();
            buff.close();
        } catch (Exception e) {
            LOGGER.error("导出文件文件出错，e:{}", e);
        } finally {
            try {
                buff.close();
                outStr.close();
            } catch (Exception e) {
                LOGGER.error("关闭流对象出错 e:{}", e);
            }
        }
    }

    /**
     * 如果字符串对象为 null，则返回空字符串，否则返回去掉字符串前后空格的字符串
     *
     * @param str
     * @return
     */
    public static String delNull (String str) {
        String returnStr = "";
        if (!StringUtils.isEmpty(str)) {
            returnStr = str.trim();
        }
        return returnStr;
    }


    public void multFileDownload(HttpServletResponse response,String[] files,String path,String downloadfile) throws Exception {
        response.setContentType("multipart/form-data");//1.设置文件ContentType类型，这样设置，会自动判断下载文件类型
        response.setHeader("Content-Disposition", "attachment;fileName="+downloadfile);
        ServletOutputStream out;
        FileInputStream instream = null;
        try {
            ZipOutputStream zipstream=new ZipOutputStream(response.getOutputStream());
            for (String file:files) {
                if (!new File(path+file).exists()) {
                    continue;
                }
                instream=new FileInputStream(path+file);
                ZipEntry entry = new ZipEntry(path+file);
                zipstream.putNextEntry(entry);
                byte[] buffer = new byte[1024];
                int len = 0;
                while (len != -1){
                    len = instream.read(buffer);
                    zipstream.write(buffer,0,buffer.length);
                }
                instream.close();
                zipstream.closeEntry();
                zipstream.flush();
            }
            zipstream.finish();
            zipstream.close();
        } catch (IOException e) {
            new RuntimeException(e.getMessage());
        }
    }
}
