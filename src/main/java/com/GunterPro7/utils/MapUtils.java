package com.GunterPro7.utils;

import java.util.HashMap;
import java.util.Map;

public class MapUtils {
    public static <T, U> Map<T, U> of(Object... elements) {
        Map<T, U> map = new HashMap<>();
        for (int i = 0; i < elements.length; i+=2) {
            T t = (T) elements[i];
            U u = (U) elements[i+1];
            map.put(t, u);
        }

        return map;
    }
}
