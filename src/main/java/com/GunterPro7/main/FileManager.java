package com.GunterPro7.main;

import net.minecraft.core.BlockPos;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

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

    static class Positions {
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
                for (String string : fileManager.getByKey("locations.txt").split(";")) {
                    blockPosList.add(blockPosFromString(string));
                }
            } catch (FileNotFoundException e) {
                throw new RuntimeException(e);
            }
        }

        public static void add(BlockPos blockPos) throws IOException {
            blockPosList.add(blockPos);
            fileManager.appendByKey(key, blockPosToString(blockPos));
        }

        public static void remove(BlockPos blockPos) throws IOException {
            blockPosList.remove(blockPos);
            fileManager.saveByKey(key, blockPosListToString(blockPosList));
        }

        public static List<BlockPos> getAll() {
            return blockPosList;
        }

        private static String blockPosToString(BlockPos blockPos) {
            return blockPos.getX() + "," + blockPos.getY() + "," + blockPos.getZ();
        }

        private static String blockPosListToString(List<BlockPos> blockPosList) {
            StringBuilder stringBuilder = new StringBuilder();
            blockPosList.forEach(pos -> stringBuilder.append(blockPosToString(pos)));

            return stringBuilder.toString();
        }

        private static BlockPos blockPosFromString(String blockPosString) {
            String[] parts = blockPosString.split(",");
            return new BlockPos(Integer.parseInt(parts[0]), Integer.parseInt(parts[1]), Integer.parseInt(parts[2]));
        }
    }
}
