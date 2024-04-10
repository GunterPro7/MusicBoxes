package com.GunterPro7.utils;

import net.minecraft.resources.ResourceLocation;
import org.jaudiotagger.audio.AudioFile;
import org.jaudiotagger.audio.AudioFileIO;

import java.io.FileOutputStream;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;

public class SoundUtils {
    private static final String path = "libraries/MusicBox/";
    public static final ResourceLocation location = new ResourceLocation("musicboxes", "custom1");

    public static int getAudioTickLength(InputStream stream) {
        int sec = readAudioFileLength(stream);
        return sec == -1 ? -1 : sec * 20;
    }

    public static int readAudioFileLength(InputStream stream) {
        try {
            Path tempFile = Files.createTempFile(path + "temp", ".ogg");
            try (FileOutputStream outputStream = new FileOutputStream(tempFile.toFile())) {
                byte[] buffer = new byte[1024];
                int length;
                while ((length = stream.read(buffer)) != -1) {
                    outputStream.write(buffer, 0, length);
                }
            }

            AudioFile audioFile = AudioFileIO.read(tempFile.toFile());
            int duration = audioFile.getAudioHeader().getTrackLength();
            Files.delete(tempFile); // delete the temporary file

            return duration;
        } catch (Exception e) {
            e.printStackTrace();
        }

        return -1;
    }
}
