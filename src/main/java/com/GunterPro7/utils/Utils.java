package com.GunterPro7.utils;

import com.GunterPro7.entity.AudioCable;
import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.Vec3;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Utils {
    public static Vec3 vec3Of(String string) {
        String[] parts = string.split(", ");
        return new Vec3(Double.parseDouble(parts[0]), Double.parseDouble(parts[1]), Double.parseDouble(parts[2]));
    }

    public static Vec3 vec3Of(BlockPos blockPos) {
        return new Vec3(blockPos.getX(), blockPos.getY(), blockPos.getZ());
    }

    public static BlockPos blockPosOf(String string) {
        String[] parts = string.split(",");
        return new BlockPos(Integer.parseInt(parts[0]), Integer.parseInt(parts[1]), Integer.parseInt(parts[2]));
    }

    public static BlockPos blockPosOf(Vec3 vec3) {
        return new BlockPos((int) vec3.x, (int) vec3.y, (int) vec3.z);
    }

    public static String blockPosToString(BlockPos blockPos) {
        return blockPos.getX() + "," + blockPos.getY() + "," + blockPos.getZ();
    }

    public static String blockPosListToString(List<BlockPos> blockPosList) {
        StringBuilder stringBuilder = new StringBuilder();
        blockPosList.forEach(pos -> stringBuilder.append(blockPosToString(pos)).append(";"));

        return stringBuilder.toString();
    }

    public static BlockPos blockPosFromString(String blockPosString) {
        String[] parts = blockPosString.split(",");
        return new BlockPos(Integer.parseInt(parts[0]), Integer.parseInt(parts[1]), Integer.parseInt(parts[2]));
    }

    public static String audioCableListToString(List<AudioCable> audioCableList) {
        StringBuilder stringBuilder = new StringBuilder();

        for (AudioCable audioCable : audioCableList) {
            stringBuilder.append(audioCable.toString()).append("\n");
        }

        return stringBuilder.toString();
    }

    public static RandomAccessFile moveBytes(RandomAccessFile raf, int from, int to, int length) throws IOException {
        byte[] byteArray = new byte[length];
        raf.read(byteArray);

        raf.seek(from);
        if (to - from == length) {
            raf.setLength(from + length);
        }

        raf.write(byteArray);
        return raf;
    }

    public static Map<Integer, Boolean> integerBooleanListOf(String data) {
        Map<Integer, Boolean> ints = new HashMap<>();
        for (String part : data.split(";")) {
            if (!part.isEmpty()) {
                ints.put(Integer.parseInt(part.substring(1)), part.charAt(0) == '1');
            }
        }

        return ints;
    }

    public static String[] split(String string, String by) {
        String[] newString = string.split("(?=" + by + ")");

        for (int i = 0; i < newString.length; i++) {
            newString[i] = newString[i].replaceAll(by, "");
        }

        return newString;
    }

    public static String integerBooleanListToString(Map<Integer, Boolean> map) {
        StringBuilder sb = new StringBuilder();
        for (Map.Entry<Integer, Boolean> entry : map.entrySet()) {
            sb.append(entry.getValue() ? '1' : '0').append(entry.getKey()).append(";");
        }

        return sb.toString();
    }


    public <T> List<T> getStaticFields(Class<T> class_) {
        return null;
    }

    public static double getCenter(double x1, double x2) {
        return Math.max(x1, x2) - Math.abs(x1 - x2) / 2;
    }

    public static long getRandomId() {
        return 1L + (long) (Math.random() * (Long.MAX_VALUE - 1L));
    }

    public static List<Integer> integerListOf(String data) {
        List<Integer> ints = new ArrayList<>();
        for (String part : data.split(";")) {
            ints.add(Integer.valueOf(part));
        }

        return ints;
    }
}
