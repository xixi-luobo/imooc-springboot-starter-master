package org.n3r.idworker.utils;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.module.jaxb.JaxbAnnotationModule;

import java.io.IOException;
import java.util.List;
import java.util.Map;


/**
 * 
 * @Title: JsonUtils.java
 * @Package com.lee.utils
 * @Description: 自定义响应结构, 转换类
 * Copyright: Copyright (c) 2016
 * Company:Nathan.Lee.Salvatore
 * 
 * @author leechenxiang
 * @date 2016年4月29日 下午11:05:03
 * @version V1.0
 */
public class JsonTranscoder {
    private static ObjectMapper mapper;

    /**
     * @param o o必须有public的getter
     */
    public static String toJson(Object o) {
        try {
            return getMapper().writeValueAsString(o);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    public static String toPrettyJson(Object o) {
        try {
            return getMapper().writerWithDefaultPrettyPrinter().writeValueAsString(o);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * @param clazz clazz对应的类必须有默认构造器和public的setter
     */
    public static <T> T fromJson(String data, Class<T> clazz) {
        data = data.trim();
        try {
            JavaType type = getMapper().getTypeFactory().constructType(clazz);
            return getMapper().readValue(data, type);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static Map<String, Object> mapFromJson(String data) {
        return fromJson2(data, new TypeReference<Map<String, Object>>(){});
    }

    public static List<Map> listMapFromJson(String data) {
        return fromJson2(data, new TypeReference<List<Map>>(){});
    }

    public static <T> T fromJson2(String data, TypeReference valueTypeRef) {
        data = data.trim();
        try {
            return getMapper().readValue(data, valueTypeRef);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private static ObjectMapper getMapper() {
        if (mapper == null) {
            synchronized (JsonTranscoder.class) {
                if (mapper == null) {
                    mapper = new ObjectMapper();
                    mapper.registerModule(new JaxbAnnotationModule());
                    mapper.configure(JsonParser.Feature.ALLOW_UNQUOTED_FIELD_NAMES, true);
                    mapper.configure(JsonParser.Feature.ALLOW_SINGLE_QUOTES, true);
                    mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
                }
            }
        }
        return mapper;
    }
}
