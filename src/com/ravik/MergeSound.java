package com.ravik;

import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.ByteBuffer;

public class MergeSound {

    private short audioFormat;
    private int sampleRate;
    private short sampleSize;
    private short channels;

    private ByteBuffer buffer;

    public MergeSound(File file) throws IOException {

        DataInputStream in = new DataInputStream(new FileInputStream(file));
        byte[] sound = new byte[in.available() - 44];

        // read header data
        in.skipNBytes(20);
        audioFormat = Short.reverseBytes(in.readShort());
        channels = Short.reverseBytes(in.readShort());
        sampleRate = Integer.reverseBytes(in.readInt());
        in.skipNBytes(6);
        sampleSize = Short.reverseBytes(in.readShort());
        in.skipNBytes(8);// make sure to cut the full header of else there will be strange noise

        in.read(sound);
        buffer = ByteBuffer.wrap(sound);
    }

    public ByteBuffer getBuffer() {
        return buffer;
    }

    public short getAudioFormat() {
        return audioFormat;
    }

    public void setAudioFormat(short audioFormat) {
        this.audioFormat = audioFormat;
    }

    public int getSampleRate() {
        return sampleRate;
    }

    public void setSampleRate(int sampleRate) {
        this.sampleRate = sampleRate;
    }

    public short getSampleSize() {
        return sampleSize;
    }

    public void setSampleSize(short sampleSize) {
        this.sampleSize = sampleSize;
    }

    public short getChannels() {
        return channels;
    }

    public void setChannels(short channels) {
        this.channels = channels;
    }

}
