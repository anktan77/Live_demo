package com.example.capture.video.camera;

import android.content.Context;
import android.graphics.SurfaceTexture;
import android.opengl.EGLContext;

import com.example.framework.framework.modules.producers.VideoProducer;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public abstract class VideoCapture extends VideoProducer {
    public interface OnVideoCaptureStateListener {
        void onCameraFirstFrame();
    }

    /**
     * Common class for storing a frameRate range. Values should be multiplied by 1000.
     */
    static class FrameRateRange {
        int min;
        int max;

        FrameRateRange(int min, int max) {
            this.min = min;
            this.max = max;
        }
    }

    public enum CameraState {
        OPENING,
        CONFIGURING,
        STARTED,
        STOPPING,
        STOPPED;

        @Override
        public String toString() {
            switch (this) {
                case OPENING: return "opening";
                case CONFIGURING: return "configure";
                case STARTED: return "started";
                case STOPPING: return "stopping";
                case STOPPED: return "stopped";
                default: return "";
            }
        }
    }

    private static final String TAG = VideoCapture.class.getSimpleName();

    // Góc (0, 90, 180, 270) mà hình ảnh cần được xoay để hiển thị
    // hướng gốc của màn hình.
    int pCameraNativeOrientation;

    // In some occasions we need to invert the device rotation readings, see the
    // individual implementations.
    boolean pInvertDeviceOrientationReadings;

    VideoCaptureFormat pCaptureFormat;
    Context pContext;
    EGLContext pEGLContext;

    int pPreviewTextureId = -1;
    SurfaceTexture pPreviewSurfaceTexture;
    byte[] pYUVImage;

    boolean mNeedsPreview;
    int mPreviewWidth;
    int mPreviewHeight;

    int mCameraId;
    String mCamera2Id;
    int mFacing;

    boolean firstFrame;

    OnVideoCaptureStateListener stateListener;

    VideoCapture(Context context) {
        pContext = context;
    }


    public abstract boolean allocate(int width, int height, int frameRate, int facing);

    public abstract void startCaptureMaybeAsync(boolean needsPreview);

    public abstract void stopCaptureAndBlockUntilStopped();

    public abstract void deallocate(boolean disconnect);

    void deallocate() {
        deallocate(true);
    }

    public void setOnVideoCaptureStateListener(OnVideoCaptureStateListener listener) {
        stateListener = listener;
    }


    static FrameRateRange getClosestFrameRateRange(
            final List<FrameRateRange> frameRateRanges, int targetFrameRate) {
        return Collections.min(frameRateRanges, new Comparator<FrameRateRange>() {
            // Threshold and penalty weights if the upper bound is further away than
            // |MAX_FPS_DIFF_THRESHOLD| from requested.
            private static final int MAX_FPS_DIFF_THRESHOLD = 5000;
            private static final int MAX_FPS_LOW_DIFF_WEIGHT = 1;
            private static final int MAX_FPS_HIGH_DIFF_WEIGHT = 3;

            // Threshold and penalty weights if the lower bound is bigger than |MIN_FPS_THRESHOLD|.
            private static final int MIN_FPS_THRESHOLD = 8000;
            private static final int MIN_FPS_LOW_VALUE_WEIGHT = 1;
            private static final int MIN_FPS_HIGH_VALUE_WEIGHT = 4;

            // Use one weight for small |value| less than |threshold|, and another weight above.
            private int progressivePenalty(
                    int value, int threshold, int lowWeight, int highWeight) {
                return (value < threshold)
                        ? value * lowWeight
                        : threshold * lowWeight + (value - threshold) * highWeight;
            }

            int diff(FrameRateRange range) {
                final int minFpsError = progressivePenalty(range.min, MIN_FPS_THRESHOLD,
                        MIN_FPS_LOW_VALUE_WEIGHT, MIN_FPS_HIGH_VALUE_WEIGHT);
                final int maxFpsError = progressivePenalty(Math.abs(targetFrameRate - range.max),
                        MAX_FPS_DIFF_THRESHOLD, MAX_FPS_LOW_DIFF_WEIGHT, MAX_FPS_HIGH_DIFF_WEIGHT);
                return minFpsError + maxFpsError;
            }

            @Override
            public int compare(FrameRateRange range1, FrameRateRange range2) {
                return diff(range1) - diff(range2);
            }
        });
    }

    protected abstract int getNumberOfCameras();

    protected abstract void startPreview();

    void setSharedContext(EGLContext eglContext) {
        pEGLContext = eglContext;
    }

    void onFrameAvailable() {

        boolean mirrored = (mFacing == Constant.CAMERA_FACING_FRONT);

        VideoCaptureFrame frame = new VideoCaptureFrame(
                // Định dạng có thể bị thay đổi trong quá trình xử lý.
                // Tạo một bản sao của cấu hình định dạng để tránh
                // phiên bản định dạng ban đầu bị
                // sửa đổi bất ngờ.
                pCaptureFormat.copy(),
                pPreviewSurfaceTexture,
                pPreviewTextureId,
                pYUVImage,
                null,
                System.currentTimeMillis(),
                pCameraNativeOrientation,
                mirrored,
                firstFrame);

        pushVideoFrame(frame);

        if (firstFrame && stateListener != null) {
            stateListener.onCameraFirstFrame();
        }
        firstFrame = false;
    }
}
