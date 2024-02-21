package com.GunterPro7.utils;

import java.util.HashMap;
import java.util.Map;

public class JsonUtils {
    public static String valueOf(String string, String key) {
        return string.substring(string.indexOf(key + ":")).split(";")[0];
    }

    public static Map<String, String> asMap(String string) {
        Map<String, String> map = new HashMap<>();
        for (String part : string.split(";")) {
            String[] partsPart = part.split(":");
            map.put(partsPart[0], partsPart[1]);
        }

        return map;
    }
}
