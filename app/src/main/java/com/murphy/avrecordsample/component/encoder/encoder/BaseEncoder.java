package com.murphy.avrecordsample.component.encoder.encoder;

import android.media.MediaCodec;
import android.media.MediaFormat;
import android.util.Log;

import java.nio.ByteBuffer;

public abstract class BaseEncoder implements Runnable {
    protected MediaCodec mEncoder;
    protected MediaCodec.BufferInfo mBufferInfo;
    private int mTrackIndex = -1;
    protected WrapMuxer mMuxer;

    protected final Object mSync = new Object();
    private volatile boolean mIsEndOfStream = false;
    private volatile boolean mIsCapture = false;

    public BaseEncoder(WrapMuxer muxer) {
        mMuxer = muxer;
        mBufferInfo = new MediaCodec.BufferInfo();

    }

    public boolean isCapture() {
        return mIsCapture;
    }


    private void processWait() {
        synchronized (mSync) {
            try {
                mSync.wait();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    private void processNotify() {
        synchronized (mSync) {
            mSync.notify();
        }
    }


    public void frameAvailable() {
        synchronized (mSync) {
            synchronized (mSync) {
                Log.i("lock_thread", this.getClass().getName() + "notifyAll_frameAvailable");
                mSync.notifyAll();
            }
        }
    }

    private void drainEncoder(boolean endOfStream) {
        final int TIMEOUT_USEC = 10000;
        String className = this.getClass().getName();
        if (endOfStream) {
            if (isSurfaceInput()) {
                mEncoder.signalEndOfInputStream();
                Log.i("lock_thread", "video_end");
            }
        }

        while (true) {
            int outputBufferId = mEncoder.dequeueOutputBuffer(mBufferInfo, TIMEOUT_USEC);
            if (outputBufferId == MediaCodec.INFO_TRY_AGAIN_LATER) {
                if (!endOfStream) {
                    break;
                }
            } else if (outputBufferId == MediaCodec.INFO_OUTPUT_FORMAT_CHANGED) {
                MediaFormat newFormat = mEncoder.getOutputFormat();
                mTrackIndex = mMuxer.addTrack(newFormat);
                mMuxer.start();
            } else if (outputBufferId < 0) {
            } else {
                ByteBuffer outputBuffer = mEncoder.getOutputBuffer(outputBufferId);
                if (mBufferInfo.size != 0 && outputBuffer != null) {
                    outputBuffer.position(mBufferInfo.offset);
                    outputBuffer.limit(mBufferInfo.offset + mBufferInfo.size);
                    mBufferInfo.presentationTimeUs = getPTSUs();
                    mMuxer.writeSampleData(mTrackIndex, outputBuffer, mBufferInfo);
                    prevOutputPTSUs = mBufferInfo.presentationTimeUs;
                }
                mEncoder.releaseOutputBuffer(outputBufferId, false);
                if ((mBufferInfo.flags & MediaCodec.BUFFER_FLAG_END_OF_STREAM) != 0) {
                    Log.i("lock_thread", Thread.currentThread().getName() + "_thread_end");
                    break;
                }
            }
        }
    }

    @Override
    public void run() {
        processWait();
        while (mIsCapture) {


            drainEncoder(false);

            processWait();

            if (mIsEndOfStream) {

                drainEncoder(true);

                release();
                releaseMuxer();
            }
        }
    }

    public abstract boolean isSurfaceInput();

    public void release() {
        Log.i("lock_thread", Thread.currentThread().getName() + "release");
        releaseEncoder();
    }

    public MediaCodec getCodec() {
        return mEncoder;
    }

    public void startRecord() {
        new Thread(this, this.getClass().getName()).start();
        synchronized (mSync) {
            mIsEndOfStream = false;
            mIsCapture = true;
            mSync.notifyAll();
        }
    }

    public void stopRecord() {
        synchronized (mSync) {
            mIsEndOfStream = true;
            mIsCapture = false;
            Log.i("lock_thread", "notifyAll_stop");
            mSync.notifyAll();
        }
    }

    private void releaseEncoder() {
        if (mEncoder != null) {
            mEncoder.stop();
            mEncoder.release();
            mEncoder = null;
        }
    }

    public void releaseMuxer() {
        if (mMuxer != null) {
            mMuxer.release();
        }
    }

    /**
     * previous presentationTimeUs for writing
     */
    private long prevOutputPTSUs = 0;

    /**
     * get next encoding presentationTimeUs
     *
     * @return
     */
    protected long getPTSUs() {
        long result = System.nanoTime() / 1000L;
        // presentationTimeUs should be monotonic
        // otherwise muxer fail to write
        if (result < prevOutputPTSUs)
            result = (prevOutputPTSUs - result) + result;
        return result;
    }
}
