package com.GunterPro7.utils;

import java.util.HashMap;
import java.util.Map;

public class ColorNameDetector {

    // Predefined list of color names and their corresponding RGB values
    private static final Map<String, int[]> colorMap = new HashMap<>();
    static {
        colorMap.put("red", new int[]{255, 0, 0});
        colorMap.put("brown", new int[]{139, 69, 19});
        colorMap.put("green", new int[]{0, 255, 0});
        colorMap.put("blue", new int[]{0, 0, 255});
        colorMap.put("yellow", new int[]{255, 255, 0});
        colorMap.put("aqua", new int[]{0, 255, 255});
        colorMap.put("orange", new int[]{255, 128, 0});
        colorMap.put("lime", new int[]{128, 255, 128});
        colorMap.put("pink", new int[]{255, 0, 255});
        colorMap.put("white", new int[]{255, 255, 255});
        colorMap.put("black", new int[]{0, 0, 0});
    }

    // Method to calculate the Euclidean distance between two colors
    private static double getColorDistance(int[] color1, int[] color2) {
        double distance = 0;
        for (int i = 0; i < color1.length; i++) {
            distance += Math.pow(color1[i] - color2[i], 2);
        }
        return Math.sqrt(distance);
    }

    // Method to find the closest color name for a given color
    public static String getColorName(int color) {
        int red = (color >> 16) & 0xFF;
        int green = (color >> 8) & 0xFF;
        int blue = color & 0xFF;

        String closestColorName = null;
        double minDistance = Double.MAX_VALUE;

        for (Map.Entry<String, int[]> entry : colorMap.entrySet()) {
            double distance = getColorDistance(entry.getValue(), new int[]{red, green, blue});
            if (distance < minDistance) {
                minDistance = distance;
                closestColorName = entry.getKey();
            }
        }

        return closestColorName;
    }
}
