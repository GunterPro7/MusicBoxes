package com.GunterPro7.utils;

import com.GunterPro7.entity.AudioCable;
import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.Vec3;

import javax.sound.sampled.AudioFileFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.UnsupportedAudioFileException;
import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.util.*;

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
        return new BlockPos(Integer.parseInt(parts[0].trim()), Integer.parseInt(parts[1].trim()), Integer.parseInt(parts[2].trim()));
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
        Map<Integer, Boolean> ints = new TreeMap<>();
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

    public static <T> String listToShortString(List<T> tracks) {
        StringBuilder sb = new StringBuilder();
        tracks.forEach(track -> sb.append(track).append(";"));
        return sb.substring(0, sb.length() > 0 ? sb.length() - 1 : sb.length());
    }

    public static String intListToString(List<Integer> colors) {
        StringBuilder sb = new StringBuilder();
        colors.forEach(color -> sb.append(color).append(";"));

        return sb.toString();
    }

    public static String removeNonNumberChars(String value) {
        StringBuilder sb = new StringBuilder();
        for (char c : value.toCharArray()) {
            if (c >= 48 && c <= 57) {
                sb.append(c);
            }
        }
        return sb.toString();
    }

    public static float[] toDarkerColor(float[] rgb) {
        float[] darkerRgb = new float[3];
        for (int i = 0; i < 3; i++) {
            darkerRgb[i] = Math.max(rgb[i] - 40 / 255.0f, 0);
        }
        return darkerRgb;
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
            if (!data.isEmpty()) {
                ints.add(Integer.valueOf(part));
            }
        }

        return ints;
    }

    public static int getTrackLengthInTicks(InputStream inputStream) throws UnsupportedAudioFileException, IOException {
        AudioInputStream audioInputStream = AudioSystem.getAudioInputStream(inputStream);

        // Get the AudioFileFormat from the AudioInputStream
        AudioFileFormat fileFormat = AudioSystem.getAudioFileFormat(audioInputStream);

        // Get the duration of the audio file in seconds
        float durationInSeconds = fileFormat.getFrameLength() / fileFormat.getFormat().getFrameRate();
        return (int) (durationInSeconds * 20);
    }
}
