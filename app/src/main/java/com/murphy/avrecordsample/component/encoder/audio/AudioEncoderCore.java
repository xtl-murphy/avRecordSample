package com.murphy.avrecordsample.component.encoder.audio;

import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaCodec;
import android.media.MediaCodecInfo;
import android.media.MediaFormat;
import android.media.MediaMuxer;
import android.media.MediaRecorder;
import android.os.Environment;
import android.util.Log;

import com.murphy.avrecordsample.component.encoder.encoder.AudioEncoder;

import java.io.IOException;
import java.nio.ByteBuffer;

public class AudioEncoderCore {


    private int mBufferSize = 0;
    private static final int SAMPLE_RATE = 44100;

    private AudioEncoder mAudioEncoder;
    private int mAudioInputBufferIndex;

    private AudioRecord mAudioRecord;

    private volatile boolean mIsRecording = false;

    public AudioEncoderCore(AudioEncoder audioEncoder) {
        mAudioEncoder = audioEncoder;
    }
    long audioAbsolutePtsUs;


    public void startRecord() {
        mAudioEncoder.startRecord();
        new AudioThread().start();
    }

    public void stopRecord() {
        mIsRecording = false;
    }


    private void addADTStoPacket(byte[] packet, int packetLen) {
        int profile = 2;  //AAC LC
        int freqIdx = 4;  //44.1KHz
        int chanCfg = 2;  //CPE
        packet[0] = (byte) 0xFF;
        packet[1] = (byte) 0xF9;
        packet[2] = (byte) (((profile - 1) << 6) + (freqIdx << 2) + (chanCfg >> 2));
        packet[3] = (byte) (((chanCfg & 3) << 6) + (packetLen >> 11));
        packet[4] = (byte) ((packetLen & 0x7FF) >> 3);
        packet[5] = (byte) (((packetLen & 7) << 5) + 0x1F);
        packet[6] = (byte) 0xFC;
    }

    private class AudioThread extends Thread {
        @Override
        public void run() {
            mIsRecording = true;
            mBufferSize = AudioRecord.getMinBufferSize(SAMPLE_RATE, AudioFormat.CHANNEL_IN_DEFAULT, AudioFormat.ENCODING_PCM_16BIT);
            mAudioRecord = new AudioRecord(MediaRecorder.AudioSource.MIC, SAMPLE_RATE, AudioFormat.CHANNEL_IN_DEFAULT, AudioFormat.ENCODING_PCM_16BIT, mBufferSize);
            mAudioRecord.startRecording();
            int readSize;
            byte[] tempBuffer = new byte[mBufferSize];
            while (mIsRecording) {
                readSize = mAudioRecord.read(tempBuffer, 0, mBufferSize);
                if (readSize == AudioRecord.ERROR_INVALID_OPERATION || readSize == AudioRecord.ERROR_BAD_VALUE) {
                    continue;
                }
                if (readSize > 0) {
                    mAudioInputBufferIndex = mAudioEncoder.getCodec().dequeueInputBuffer(-1);
                    if (mAudioInputBufferIndex >= 0) {
                        ByteBuffer inputBuffer = mAudioEncoder.getCodec().getInputBuffer(mAudioInputBufferIndex);
                        if (inputBuffer != null) {
                            inputBuffer.put(tempBuffer);
                            audioAbsolutePtsUs = (System.nanoTime()) / 1000L;
                            mAudioEncoder.getCodec().queueInputBuffer(mAudioInputBufferIndex, 0, mBufferSize, audioAbsolutePtsUs, 0);
                        }
                    }
                    mAudioEncoder.frameAvailable();
                }
            }

            mAudioRecord.stop();
            mAudioRecord.release();
            mAudioRecord = null;

            mAudioInputBufferIndex = mAudioEncoder.getCodec().dequeueInputBuffer(-1);
            if (mAudioInputBufferIndex >= 0) {
                Log.i("lock_thread", "audio_end");
                audioAbsolutePtsUs = (System.nanoTime()) / 1000L;
                mAudioEncoder.getCodec().queueInputBuffer(mAudioInputBufferIndex, 0, 0, audioAbsolutePtsUs, MediaCodec.BUFFER_FLAG_END_OF_STREAM);
            }
            mAudioEncoder.stopRecord();
        }
    }
}
