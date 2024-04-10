package com.GunterPro7;

import com.GunterPro7.entity.AudioCable;
import com.GunterPro7.utils.McUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.phys.Vec3;

import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class FileManager {
    private static final String path = "libraries/MusicBox/";

    static {
        File folder = new File(path);
        if (!folder.exists()) {
            folder.mkdirs();
        }
    }

    public RandomAccessFile rafByKey(String key) throws IOException {
        File file = new File(path + key);
        if (!file.exists()) {
            file.createNewFile();
        }

        return new RandomAccessFile(file, "rw");
    }

    public boolean existsByKey(String key) {
        return new File(path + key).exists();
    }


    public static class AudioCables {
        public static File file;
        public static final Map<ServerLevel, List<AudioCable>> audioCableList = new HashMap<>();
        private static final FileManager fileManager = new FileManager();
        private static final String key = "audio_cables/";

        static {
            file = new File("libraries/MusicBox/" + key);

            if (!file.exists()) {
                file.mkdirs();
            }
        }

        public static void load(ServerLevel level) {
            List<AudioCable> audioCables = new ArrayList<>();
            audioCableList.put(level, audioCables);

            try (RandomAccessFile raf = fileManager.rafByKey(getKey(level))) {
                while (raf.getFilePointer() != raf.length()) {
                    try {
                        Vec3 startPos = new Vec3(raf.readDouble(), raf.readDouble(), raf.readDouble());
                        Vec3 endPos = new Vec3(raf.readDouble(), raf.readDouble(), raf.readDouble());
                        BlockPos startBlock = new BlockPos(raf.readInt(), raf.readInt(), raf.readInt());
                        BlockPos endBlock = new BlockPos(raf.readInt(), raf.readInt(), raf.readInt());
                        int color = raf.readInt();

                        audioCables.add(new AudioCable(startPos, endPos, startBlock, endBlock, level, color));
                    } catch (EOFException e) {
                        System.out.println("File is invalid!");
                    }
                }
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }

        public static void add(AudioCable audioCable) throws IOException {
            ServerLevel level = (ServerLevel) audioCable.getLevel();
            if (!audioCableList.containsKey(level)) {
                load(level);
            }
            audioCableList.get(level).add(audioCable);

            try (RandomAccessFile raf = fileManager.rafByKey(getKey(level))) {
                raf.seek(raf.length());
                Vec3 startPos = audioCable.getStartPos();
                Vec3 endPos = audioCable.getEndPos();

                BlockPos startBlock = audioCable.getStartBlock();
                BlockPos endBlock = audioCable.getEndBlock();

                raf.writeDouble(startPos.x);
                raf.writeDouble(startPos.y);
                raf.writeDouble(startPos.z);
                raf.writeDouble(endPos.x);
                raf.writeDouble(endPos.y);
                raf.writeDouble(endPos.z);

                raf.writeInt(startBlock.getX());
                raf.writeInt(startBlock.getY());
                raf.writeInt(startBlock.getZ());
                raf.writeInt(endBlock.getX());
                raf.writeInt(endBlock.getY());
                raf.writeInt(endBlock.getZ());

                raf.writeInt(audioCable.getColor());
            }
        }

        public static void remove(AudioCable audioCable) throws IOException {
            ServerLevel level = (ServerLevel) audioCable.getLevel();
            if (!audioCableList.containsKey(level)) {
                load(level);
            }
            audioCableList.get(level).remove(audioCable);

            remove(List.of(audioCable), false);
        }

        public static void removeAll(List<AudioCable> audioCables) throws IOException {
            if (audioCables.size() > 0) {
                ServerLevel level = (ServerLevel) audioCables.get(0).getLevel();
                if (!audioCableList.containsKey(level)) {
                    load(level);
                }
                audioCableList.get(level).removeAll(audioCables);

                remove(audioCables, true);
            }
        }

        public static List<AudioCable> getAll(ServerLevel level) {
            if (!audioCableList.containsKey(level)) {
                load(level);
            }
            return audioCableList.get(level);
        }

        private static void remove(List<AudioCable> audioCables, boolean all) throws IOException {
            try (RandomAccessFile raf = fileManager.rafByKey(getKey((ServerLevel) audioCables.get(0).getLevel()))) {
                while (raf.getFilePointer() != raf.length()) {
                    try {
                        Vec3 startPos = new Vec3(raf.readDouble(), raf.readDouble(), raf.readDouble());
                        Vec3 endPos = new Vec3(raf.readDouble(), raf.readDouble(), raf.readDouble());
                        BlockPos startBlock = new BlockPos(raf.readInt(), raf.readInt(), raf.readInt());
                        BlockPos endBlock = new BlockPos(raf.readInt(), raf.readInt(), raf.readInt());
                        int dyeColor = raf.readInt();

                        AudioCable curAudioCable = new AudioCable(startPos, endPos, startBlock, endBlock, null, dyeColor);

                        for (AudioCable audioCable : audioCables) {
                            if (audioCable.hashCode() == curAudioCable.hashCode() && audioCable.equals(curAudioCable)) {
                                removeEntry(raf, Double.BYTES * 6 + Integer.BYTES * 6 + Integer.BYTES);
                                if (!all) {
                                    break;
                                }
                            }
                        }
                    } catch (EOFException e) {
                        System.out.println("File is invalid!");
                    }
                }
            }
        }

        // Length: Double.BYTES * 6 + UTF + UTF
        private static void removeEntry(RandomAccessFile raf, int size) throws IOException {
            long position = raf.getFilePointer();

            byte[] byteArray = new byte[(int) (raf.length() - raf.getFilePointer())];
            raf.read(byteArray);

            int newPos = (int) position - size;
            if (newPos < 0) {
                newPos = 0;
            }

            raf.seek(newPos);
            raf.setLength(raf.length() - size);
            raf.write(byteArray);

            raf.seek(newPos);
        }

        private static String getKey(ServerLevel level) {
            return key + McUtils.getIdentifierByLevel(level) + ".bin";
        }
    }
}
