package com.murphy.avrecordsample.component.encoder.encoder;

import android.media.MediaCodec;
import android.media.MediaCodecInfo;
import android.media.MediaFormat;
import android.view.Surface;

import java.io.IOException;

public class VideoEncoder extends BaseEncoder {

    private static final String MIME_TYPE = "video/avc";
    private static final int FRAME_RATE = 30;               // 30fps
    private static final int IFRAME_INTERVAL = 5;           // 5 seconds between I-frames
    private Surface mInputSurface;
    public VideoEncoder(int width, int height, int bitRate, WrapMuxer muxer) throws IOException {
        super(muxer);
        MediaFormat format = MediaFormat.createVideoFormat(MIME_TYPE, width, height);
//        byte[] header_sps = {0, 0, 0, 1, 103, 100, 0, 31, -84, -76, 2, -128, 45, -56};
//        byte[] header_pps = {0, 0, 0, 1, 104, -18, 60, 97, 15, -1, -16, -121, -1, -8, 67, -1, -4, 33, -1, -2, 16, -1, -1, 8, 127, -1, -64};
//        format.setByteBuffer("csd-0", ByteBuffer.wrap(header_sps));
//        format.setByteBuffer("csd-1", ByteBuffer.wrap(header_pps));
        format.setInteger(MediaFormat.KEY_COLOR_FORMAT,
                MediaCodecInfo.CodecCapabilities.COLOR_FormatSurface);
        format.setInteger(MediaFormat.KEY_BIT_RATE, bitRate);
        format.setInteger(MediaFormat.KEY_FRAME_RATE, FRAME_RATE);
        format.setInteger(MediaFormat.KEY_I_FRAME_INTERVAL, IFRAME_INTERVAL);
        mEncoder = MediaCodec.createEncoderByType(MIME_TYPE);
        mEncoder.configure(format, null, null, MediaCodec.CONFIGURE_FLAG_ENCODE);
        mInputSurface = mEncoder.createInputSurface();
        mEncoder.start();
    }

    @Override
    public boolean isSurfaceInput() {
        return true;
    }

    public Surface getInputSurface() {
        return mInputSurface;
    }
}
