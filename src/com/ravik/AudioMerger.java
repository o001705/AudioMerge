package com.ravik;

import static java.lang.Math.ceil;
import static java.lang.Math.round;

import java.io.*;
import java.nio.ByteBuffer;
import java.util.ArrayList;

public class AudioMerger {
    private short audioFormat = 1;
    private int sampleRate = 48000;
    private short sampleSize = 16;
    private short channels = 2;
    private short blockAlign = (short) (sampleSize * channels / 8);
    private int byteRate = sampleRate * sampleSize * channels / 8;
    private ByteBuffer audioBuffer;
    private ArrayList<MergeSound> sounds = new ArrayList<MergeSound>();

    String printArray(byte[] array) {
        String retVal = "";
        for (int i = 0; i < 10; i++ )
            retVal.concat(String.valueOf(array[i]));

        return retVal;
    }
    /*
       This function merges two streams;
       channels - ByteStream for all channels as array
       SampleSizes - Array of sample sizes in each stream (8, 16, .. etc)
       This implementation assumes;
               - All Channels have same size buffers, future versions can have different buffer sizes
               - SampleSize is same for all channels, future versions can have different sample sizes
     */
    public void raw_merge(ByteBuffer[] channels, short[] sampleSizes ) {
        audioBuffer = ByteBuffer.allocate(channels[0].capacity()*channels.length +10);
        /*
            Read SampleSize/8 byres from each channel
            Append each of them into audioBuffer
        */
        int size = 0;
        int position = 0;
        int index[] = new int[channels.length];
        for (int i=0; i < channels.length;i++ ) { index[i] = 0;}

        while (size < channels[0].array().length) {
            for (int i = 0; i < channels.length; i++) {
                byte[] dst = new byte[sampleSizes[i] / 8];
                for (int j =0; j < sampleSizes[i] /8; j++) {
                    if (channels[i].hasRemaining()) {
                        dst[j] = channels[i].get(index[i]);
                        index[i]++;
                        if (i == 0)
                            size++;
                    }
                }
                audioBuffer.put(position, dst);
                position += sampleSize / 8;
            }
        }
    }


    private int secondsToByte(double seconds) {
        return (int) ceil(seconds * byteRate);
    }
    public double byteToSeconds(int bytes) {
        return (double) (bytes/(byteRate*1.0));
    }

    public void saveToFile(File file) throws IOException {

        byte[] audioData = audioBuffer.array();

        int audioSize = audioData.length;
        int fileSize = audioSize + 44;

        // The stream that writes the audio file to the disk
        DataOutputStream out = new DataOutputStream(new FileOutputStream(file));

        // Write Header
        out.writeBytes("RIFF");// 0-4 ChunkId always RIFF
        out.writeInt(Integer.reverseBytes(fileSize));// 5-8 ChunkSize always audio-length +header-length(44)
        out.writeBytes("WAVE");// 9-12 Format always WAVE
        out.writeBytes("fmt ");// 13-16 Subchunk1 ID always "fmt " with trailing whitespace
        out.writeInt(Integer.reverseBytes(16)); // 17-20 Subchunk1 Size always 16
        out.writeShort(Short.reverseBytes(audioFormat));// 21-22 Audio-Format 1 for PCM PulseAudio
        out.writeShort(Short.reverseBytes(channels));// 23-24 Num-Channels 1 for mono, 2 for stereo
        out.writeInt(Integer.reverseBytes(sampleRate));// 25-28 Sample-Rate
        out.writeInt(Integer.reverseBytes(byteRate));// 29-32 Byte Rate
        out.writeShort(Short.reverseBytes(blockAlign));// 33-34 Block Align
        out.writeShort(Short.reverseBytes(sampleSize));// 35-36 Bits-Per-Sample
        out.writeBytes("data");// 37-40 Subchunk2 ID always data
        out.writeInt(Integer.reverseBytes(audioSize));// 41-44 Subchunk 2 Size audio-length

        out.write(audioData);// append the merged data
        out.close();// close the stream properly
    }
}