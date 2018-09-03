package com.murphy.avrecordsample.component.encoder.encoder;

import android.media.MediaCodec;
import android.media.MediaFormat;
import android.media.MediaMuxer;

import java.io.IOException;
import java.nio.ByteBuffer;

public class WrapMuxer {

    private MediaMuxer mMuxer;
    private boolean mMuxerStarted = false;
    private int mMaxTracks;
    private int mCurrentTracks = 0;

    public WrapMuxer(String outputFile, int tracks) {
        try {
            mMuxer = new MediaMuxer(outputFile, MediaMuxer.OutputFormat.MUXER_OUTPUT_MPEG_4);
        } catch (IOException e) {
            e.printStackTrace();
        }

        mMaxTracks = tracks;
    }

    public synchronized int addTrack(MediaFormat format) {
        mCurrentTracks++;
        return mMuxer.addTrack(format);
    }

    public synchronized void start() {
        if (isLoadAllTrack() && !mMuxerStarted) {
            mMuxer.start();
            mMuxerStarted = true;
        }
    }

    private boolean isLoadAllTrack() {
        return mCurrentTracks == mMaxTracks;
    }

    public void writeSampleData(int trackIndex, ByteBuffer byteBuf, MediaCodec.BufferInfo bufferInfo) {
        if (mMuxerStarted && mMuxer != null) {

            mMuxer.writeSampleData(trackIndex, byteBuf, bufferInfo);
        }
    }

    public void release() {
        mCurrentTracks--;
        if (mCurrentTracks == 0) {
            if (mMuxer != null) {
                // TODO: stop() throws an exception if you haven't fed it any data.  Keep track
                //       of frames submitted, and don't call stop() if we haven't written anything.
                mMuxer.stop();
                mMuxer.release();
                mMuxer = null;
            }

            mMuxerStarted = false;
        }
    }
}
