package com.GunterPro7.main;

import com.GunterPro7.entity.AudioCable;
import com.GunterPro7.utils.Utils;
import net.minecraft.core.BlockPos;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public class FileManager {
    private static final String path = "libraries/MusicBox/";

    static {
        File folder = new File(path);
        if (!folder.exists()) {
            folder.mkdirs();
        }
    }

    public void saveByKey(String key, String value) throws IOException {
        File file = new File(path + key);
        if (!file.exists()) {
            file.createNewFile();
        }

        try (FileWriter fileWriter = new FileWriter(file)) {
            fileWriter.write(value);
        }
    }

    public boolean existsByKey(String key) {
        return new File(path + key).exists();
    }

    public String getByKey(String key) throws FileNotFoundException {
        File file = new File(path + key);
        if (!file.exists()) {
            throw new FileNotFoundException("File " + file.getAbsolutePath() + " does not exist!");
        }

        StringBuilder value = new StringBuilder();
        try (BufferedReader bufferedReader = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = bufferedReader.readLine()) != null) {
                value.append(line);
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        return value.toString();
    }

    public void appendByKey(String key, String value) throws IOException {
        saveByKey(key, getByKey(key) + value);
    }

    public static class Positions {
        public static File file;
        private static final List<BlockPos> blockPosList = new ArrayList<>();
        private static final FileManager fileManager = new FileManager();
        private static final String key = "locations.txt";

        static {
            file = new File("libraries/MusicBox/" + key);

            if (!file.exists()) {
                try {
                    file.createNewFile();
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }

            try {
                for (String string : fileManager.getByKey(key).split(";")) {
                    if (!string.isEmpty())
                        blockPosList.add(Utils.blockPosFromString(string));
                }
            } catch (FileNotFoundException e) {
                throw new RuntimeException(e);
            }
        }

        public static void add(BlockPos blockPos) throws IOException {
            blockPosList.add(blockPos);
            fileManager.appendByKey(key, Utils.blockPosToString(blockPos) + ";");
        }

        public static void remove(BlockPos blockPos) throws IOException {
            blockPosList.remove(blockPos);
            fileManager.saveByKey(key, Utils.blockPosListToString(blockPosList));
        }

        public static List<BlockPos> getAll() {
            return blockPosList;
        }
    }

    public static class AudioCables {
        public static File file;
        private static final List<AudioCable> audioCableList = new ArrayList<>();
        private static final FileManager fileManager = new FileManager();
        private static final String key = "audioCables.txt";

        static {
            file = new File("libraries/MusicBox/" + key);

            if (!file.exists()) {
                try {
                    file.createNewFile();
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }

            //try {
            //    for (String string : fileManager.getByKey(key).split("\n")) {
            //        if (!string.isEmpty())
            //            audioCableList.add(AudioCable.fromString(string));
            //    }
            //} catch (FileNotFoundException e) {
            //    throw new RuntimeException(e);
            //}
        }

        public static void add(AudioCable audioCable) throws IOException {
            audioCableList.add(audioCable);
            fileManager.appendByKey(key, audioCable.toString() + "\n");
        }

        public static void remove(AudioCable audioCable) throws IOException {
            audioCableList.remove(audioCable);
            fileManager.saveByKey(key, Utils.audioCableListToString(audioCableList));
        }

        public static void removeAll(List<AudioCable> audioCableList) throws IOException {
            AudioCables.audioCableList.removeAll(audioCableList);
            fileManager.saveByKey(key, Utils.audioCableListToString(AudioCables.audioCableList));
        }

        public static List<AudioCable> getAll() {
            return audioCableList;
        }
    }
}
