package com.ravik;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.Path;


import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.UnsupportedAudioFileException;
import java.nio.ByteBuffer;


public class Main {

    public static void main(String[] args) throws IOException, UnsupportedAudioFileException, LineUnavailableException {
        AudioMerger merger = new AudioMerger();
        MergeSound sound1 = new MergeSound(new File("D:\\Learning\\ML\\AudioMerge\\File2.wav"));
        MergeSound sound2 = new MergeSound(new File("D:\\Learning\\ML\\AudioMerge\\File2.wav"));

        int offset1 = 0;
        int offset2 = 0;
        ByteBuffer[] bStreams = new ByteBuffer[2];
        bStreams[0] = sound1.getBuffer();
        bStreams[1]= sound2.getBuffer();
        short[] indexes = new short[2];
        indexes[0] = sound1.getSampleSize();
        indexes[1] = sound2.getSampleSize();

        merger.raw_merge(bStreams, indexes);
        merger.saveToFile(new File("d:\\Learning\\ML\\AudioMerge\\out.wav"));
/**
            Path path1 = Paths.get("/file1.wav");
            Path path2 = Paths.get("/file2.wav");
            byte[] byte1 = Files.readAllBytes(path1);
            byte[] byte2 = Files.readAllBytes(path2);
            byte[] out = new byte[byte1.length];

            public Main() {

                byte[] byte1 = Files.readAllBytes(path1);
                byte[] byte2 = Files.readAllBytes(path2);

                for (int i=0; i<byte1.Length; i++)
                    out[i] = (byte) ((byte1[i] + byte2[i]) >> 1);
        }
 **/
    }

}