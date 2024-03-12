package com.GunterPro7;

import com.GunterPro7.entity.AudioCable;
import net.minecraft.core.BlockPos;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.phys.Vec3;

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

    public static class Positions {
        public static File file;
        private static final List<BlockPos> blockPosList = new ArrayList<>();
        private static final FileManager fileManager = new FileManager();
        private static final String key = "locations.bin";

        static {
            file = new File("libraries/MusicBox/" + key);

            if (!file.exists()) {
                try {
                    file.createNewFile();
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }

            try (RandomAccessFile raf = fileManager.rafByKey(key)) {
                while (raf.getFilePointer() != raf.length()) {
                    blockPosList.add(new BlockPos(raf.readInt(), raf.readInt(), raf.readInt()));
                }
            } catch (IOException e) {
                System.out.println("File is invalid!");
                e.printStackTrace();
            }
        }

        public static void add(BlockPos blockPos) throws IOException {
            blockPosList.add(blockPos);

            try (RandomAccessFile raf = fileManager.rafByKey(key)) {
                raf.seek(raf.length());
                raf.writeInt(blockPos.getX());
                raf.writeInt(blockPos.getY());
                raf.writeInt(blockPos.getZ());
            }
        }

        public static void remove(BlockPos blockPos) throws IOException {
            blockPosList.remove(blockPos);

            try (RandomAccessFile raf = fileManager.rafByKey(key)) {
                while (raf.getFilePointer() != file.length()) {
                    int x = raf.readInt();
                    int y = raf.readInt();
                    int z = raf.readInt();

                    if (blockPos.getX() == x && blockPos.getY() == y && blockPos.getZ() == z) {
                        long position = raf.getFilePointer();
                        byte[] byteArray = new byte[(int) (raf.length() - position)];
                        raf.read(byteArray);

                        raf.seek(position - Integer.BYTES * 3);

                        raf.setLength(raf.length() - Integer.BYTES * 3);

                        raf.write(byteArray);
                    }
                }
            } catch (EOFException e) {
                e.printStackTrace();
            }
        }

        public static List<BlockPos> getAll() {
            return blockPosList;
        }
    }

    public static class AudioCables {
        public static File file;
        private static final List<AudioCable> audioCableList = new ArrayList<>();
        private static final FileManager fileManager = new FileManager();
        private static final String key = "audioCables.bin";

        static {
            file = new File("libraries/MusicBox/" + key);

            if (!file.exists()) {
                try {
                    file.createNewFile();
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }

            try (RandomAccessFile raf = fileManager.rafByKey(key)) {
                while (raf.getFilePointer() != raf.length()) {
                    try {
                        Vec3 startPos = new Vec3(raf.readDouble(), raf.readDouble(), raf.readDouble());
                        Vec3 endPos = new Vec3(raf.readDouble(), raf.readDouble(), raf.readDouble());
                        BlockPos startBlock = new BlockPos(raf.readInt(), raf.readInt(), raf.readInt());
                        BlockPos endBlock = new BlockPos(raf.readInt(), raf.readInt(), raf.readInt());
                        DyeColor color = DyeColor.valueOf(raf.readUTF());

                        audioCableList.add(new AudioCable(startPos, endPos, startBlock, endBlock, color));
                    } catch (EOFException e) {
                        System.out.println("File is invalid!");
                    }
                }
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }

        public static void add(AudioCable audioCable) throws IOException {
            audioCableList.add(audioCable);

            try (RandomAccessFile raf = fileManager.rafByKey(key)) {
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

                raf.writeUTF(audioCable.getColor().name());
            }
        }

        public static void remove(AudioCable audioCable) throws IOException {
            audioCableList.remove(audioCable);

            remove(List.of(audioCable), false);
        }

        public static void removeAll(List<AudioCable> audioCables) throws IOException {
            AudioCables.audioCableList.removeAll(audioCables);

            remove(audioCables, true);
        }

        public static List<AudioCable> getAll() {
            return audioCableList;
        }

        private static void remove(List<AudioCable> audioCables, boolean all) throws IOException {
            try (RandomAccessFile raf = fileManager.rafByKey(key)) {
                while (raf.getFilePointer() != raf.length()) {
                    try {
                        Vec3 startPos = new Vec3(raf.readDouble(), raf.readDouble(), raf.readDouble());
                        Vec3 endPos = new Vec3(raf.readDouble(), raf.readDouble(), raf.readDouble());
                        BlockPos startBlock = new BlockPos(raf.readInt(), raf.readInt(), raf.readInt());
                        BlockPos endBlock = new BlockPos(raf.readInt(), raf.readInt(), raf.readInt());
                        DyeColor dyeColor = DyeColor.valueOf(raf.readUTF());

                        AudioCable curAudioCable = new AudioCable(startPos, endPos, startBlock, endBlock, dyeColor);

                        for (AudioCable audioCable : audioCables) {
                            if (audioCable.hashCode() == curAudioCable.hashCode() && audioCable.equals(curAudioCable)) {
                                removeEntry(raf, Double.BYTES * 6 + dyeColor.name().length() + 2);
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

        // Length: Double.BYTES * 6 + UTF
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
    }
}
