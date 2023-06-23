package com.ravik;

import static java.lang.Math.ceil;
import static java.lang.Math.round;
import java.io.*;
import java.nio.ByteBuffer;
import java.nio.MappedByteBuffer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class AudioMerger {
    private short audioFormat = 1;
    private int sampleRate = 48000;
    private short sampleSize = 16;
    private short channels = 2;
    private short blockAlign = (short) (sampleSize * channels / 8);
    private int byteRate = sampleRate * sampleSize * channels / 8;
    private ByteBuffer audioBuffer;
    private ArrayList<MergeSound> sounds = new ArrayList<MergeSound>();
    private DataOutputStream out;
    StereoMap sMap;
    int audioSize;
    private final int HEADER_SIZE = 44;
    private File mediaFile;

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
        int fileSize = audioSize + HEADER_SIZE;

        // The stream that writes the audio file to the disk
        out = new DataOutputStream(new FileOutputStream(file));

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

    /**
     *  Code for Capture Server
     *  Below code creates a shell WAV File to capture audio data
    * */
    public void createFile(File file) throws IOException {

        // The stream that writes the audio file to the disk
        mediaFile = file;
        out = new DataOutputStream(new FileOutputStream(mediaFile));
        sMap = new StereoMap();

        // Write Header
        out.write(getWaveHeader(0).array());
    }

    public void writeMediaFrameToFile(MediaFrame f) {
        try {
            audioSize += sMap.add(f, out);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void closeFile() {
        try {
            out.write(getWaveHeader(audioSize).array(),0, HEADER_SIZE);
            out.flush();
            out.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private ByteBuffer getWaveHeader(int audioSize) throws IOException {
        int fileSize = audioSize + HEADER_SIZE;
        short audioFormat = 1;
        int sampleRate = 48000;
        short sampleSize = 16;
        short channels = 2;
        short blockAlign = (short) (sampleSize * channels / 8);
        int byteRate = sampleRate * sampleSize * channels / 8;
        byte[] bytes = new byte[HEADER_SIZE];
        ByteBuffer out = ByteBuffer.wrap(bytes, 0, bytes.length);

        out.put(Byte.parseByte("RIFF"));// 0-4 ChunkId always RIFF
        out.putInt(Integer.reverseBytes(fileSize));// 5-8 ChunkSize always audio-length +header-length(44)
        out.put(Byte.parseByte("WAVE"));// 9-12 Format always WAVE
        out.put(Byte.parseByte("fmt "));// 13-16 Subchunk1 ID always "fmt " with trailing whitespace
        out.putInt(Integer.reverseBytes(16)); // 17-20 Subchunk1 Size always 16
        out.putShort(Short.reverseBytes(audioFormat));// 21-22 Audio-Format 1 for PCM PulseAudio
        out.putShort(Short.reverseBytes(channels));// 23-24 Num-Channels 1 for mono, 2 for stereo
        out.putInt(Integer.reverseBytes(sampleRate));// 25-28 Sample-Rate
        out.putInt(Integer.reverseBytes(byteRate));// 29-32 Byte Rate
        out.putShort(Short.reverseBytes(blockAlign));// 33-34 Block Align
        out.putShort(Short.reverseBytes(sampleSize));// 35-36 Bits-Per-Sample
        out.put(Byte.parseByte("data"));// 37-40 Subchunk2 ID always data
        out.putInt(Integer.reverseBytes(audioSize));// 41-44 Subchunk 2 Size audio-length

        return out;
    }
}

class MediaFrame {
    int frameID;
    int streamID;
    byte[] data;

    MediaFrame(int a, int b, byte[] c) {
        frameID = a;
        streamID = b;
        data = c.clone();
    }

    public int getFrameID() { return frameID; }
    public int getStreamID() { return streamID;}
    public byte[] getData() { return data;}
}

class StereoMap {
    Map<Integer, byte[][]> sMap;
    int LastWrittenFrame;
    StereoMap() {
        sMap = new HashMap<Integer, byte[][]>();
    }
    public int add (MediaFrame f, OutputStream out) throws IOException {
        byte[][] buf = new byte[2][];
        buf[f.getStreamID() - 1] = f.getData();
        sMap.put(f.getFrameID(), buf );
        return write(out);
    }
    private int write(OutputStream out) throws IOException {
        int bytesWritten = 0;

        Iterator hmIterator = sMap.entrySet().iterator();
        while (hmIterator.hasNext()) {
            Map.Entry me = (Map.Entry)hmIterator.next();
            int frameID = (Integer) me.getKey();
            byte[][] buf = (byte[][])me.getValue();
            if ((buf != null) &&
                    (buf[0] != null) &&
                    (buf[1] != null)) {
                byte[] op = new byte[2];
                if (buf[0].length != buf[1].length) {
                    // log error mismatched frame size; For Frame ID = A Stream-0 length = x; Stream-1 length = y
                } else {
                    for (int i =0; i < buf[0].length; i++ ) {
                        op[0] = buf[0][i];
                        op[1] = buf[1][i];
                        out.write(op);
                        bytesWritten += 2;
                    }
                    LastWrittenFrame = frameID;
                    sMap.remove(frameID);
                }
            } else {
                // Check if we have missing frames
                // And remove missing frames from map
                if (LastWrittenFrame + 1 != frameID) {
                    sMap.remove(frameID);
                }
            }
        }
        return bytesWritten;
    }
}