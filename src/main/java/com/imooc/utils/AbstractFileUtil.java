package com.imooc.utils;

public abstract class AbstractFileUtil {

    /**
     * 生成导出附件中文名。应对导出文件中文乱码
     * <p>
     * response.addHeader("Content-Disposition", "attachment; filename=" + cnName);
     *
     * @param cnName
     * @param defaultName
     * @return
     */
    public static String genAttachmentFileName (String cnName, String defaultName) {
        try {
            cnName = new String(cnName.getBytes("gb2312"), "ISO8859-1");
        } catch (Exception e) {
            cnName = defaultName;
        }
        return cnName;
    }
}
