package com.GunterPro7.utils;

import java.util.List;

public class Utils {
    public <T> List<T> getStaticFields(Class<T> class_) {
        return null;
    }

    public static double getCenter(double x1, double x2) {
        return Math.max(x1, x2) - Math.abs(x1 - x2) / 2;
    }
}
