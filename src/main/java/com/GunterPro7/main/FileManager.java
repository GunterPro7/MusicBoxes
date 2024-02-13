package com.GunterPro7.main;

import net.minecraft.core.BlockPos;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class FileManager {
    static {
        for (String curValue : new String[]{"libraries/", "libraries/MusicBox/"}) {
            File folder = new File(curValue);
            if (!folder.exists()) {
                try {
                    folder.createNewFile();
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
        }
    }

    public void saveByKey(String key, String value) {

    }

    public boolean existsByKey(String key) {
        return getByKey(key) != null;
    }

    public String getByKey(String key) {
        return "";
    }

    public void appendByKey(String key, String value) {

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

            for (String string : fileManager.getByKey("locations.txt").split(";")) {
                blockPosList.add(blockPosFromString(string));
            }
        }

        public static void add(BlockPos blockPos) {
            blockPosList.add(blockPos);
            fileManager.appendByKey(key, blockPosToString(blockPos));
        }

        public static void remove(BlockPos blockPos) {
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
