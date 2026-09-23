package com.yupi.yupao.config;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;

import java.io.IOException;

/**
 * 性别反序列化器：支持前端传 "男"/"女" 字符串，自动转为 1/0
 */
public class GenderDeserializer extends JsonDeserializer<Integer> {

    @Override
    public Integer deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
        String value = p.getValueAsString();
        if (value == null) {
            return null;
        }
        value = value.trim();
        if ("男".equals(value) || "1".equals(value)) {
            return 1;
        }
        if ("女".equals(value) || "0".equals(value)) {
            return 0;
        }
        return null;
    }
}
