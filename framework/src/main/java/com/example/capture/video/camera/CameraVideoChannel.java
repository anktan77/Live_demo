package com.example.capture.video.camera;

import android.content.Context;

import com.example.framework.framework.modules.channels.ChannelManager;
import com.example.framework.framework.modules.channels.VideoChannel;
import com.example.framework.framework.helpers.gles.core.EglCore;

public class CameraVideoChannel extends VideoChannel implements VideoCapture.OnVideoCaptureStateListener {
    public interface OnCameraStateListener {
        void onFrameFrame();
    }

    private static final String TAG = CameraVideoChannel.class.getSimpleName();

    private static final int WIDTH = 1920;
    private static final int HEIGHT = 1080;
    private static final int FRAME_RATE = 24;
    private static final int FACING = Constant.CAMERA_FACING_FRONT;

    private VideoCapture mVideoCapture;
    private volatile boolean mCapturedStarted;

    private int mWidth = WIDTH;
    private int mHeight = HEIGHT;
    private int mFrameRate = FRAME_RATE;
    private int mFacing = FACING;

    private OnCameraStateListener mListener;

    public CameraVideoChannel(Context context, int id) {
        super(context, id);
    }

    @Override
    protected void onChannelContextCreated() {
        // chỗ này làm đầu tiên vì lớp cha VideoChannel chạy đa luồng, suy ra lớp con cũng chạy đa luồng
        // lấy video cameravideo2
        mVideoCapture = VideoCaptureFactory.createVideoCapture(getChannelContext().getContext());
        mVideoCapture.setOnVideoCaptureStateListener(this);
    }

    public void setCameraStateListener(OnCameraStateListener listener) {
        mListener = listener;
    }


    public void setFacing(int facing) {
        mFacing = facing;
    }


    public void setPictureSize(int width, int height) {
        mWidth = width;
        mHeight = height;
    }

    public void setIdealFrameRate(int frameRate) {
        mFrameRate = frameRate;
    }

    // Handler xử lý hàng đợi
    // CAMERA == 0
    public void startCapture() {
        if (isRunning()) {
            getHandler().post(() -> {
                if (!mCapturedStarted) {
                    // tạo kênh
                    mVideoCapture.connectChannel(ChannelManager.ChannelID.CAMERA);
                    // chia sẻ sử dùng EGL framework
                    mVideoCapture.setSharedContext(getChannelContext().getEglCore().getEGLContext());
                    //set chiều dại rộng
                    mVideoCapture.allocate(mWidth, mHeight, mFrameRate, mFacing);
                    // bắt đầu thiết lập cameravideo2
                    mVideoCapture.startCaptureMaybeAsync(false);
                    mCapturedStarted = true;
                }
            });
        }
    }

    public void switchCamera() {
        if (isRunning() && mCapturedStarted) {
            getHandler().post(() -> {
                mVideoCapture.deallocate();
                switchCameraFacing();
                mVideoCapture.allocate(mWidth, mHeight, mFrameRate, mFacing);
                mVideoCapture.startCaptureMaybeAsync(false);
            });
        }
    }

    // mFacing giá trị nhận biết camera trước hay sau
    private void switchCameraFacing() {
        if (mFacing == Constant.CAMERA_FACING_FRONT) {
            mFacing = Constant.CAMERA_FACING_BACK;
        } else if (mFacing == Constant.CAMERA_FACING_BACK) {
            mFacing = Constant.CAMERA_FACING_FRONT;
        }
    }

    public void stopCapture() {
        if (isRunning()) {
            getHandler().post(() -> {
                if (mCapturedStarted) {
                    mCapturedStarted = false;
                    mVideoCapture.deallocate();
                }
            });
        }
    }

    public boolean hasCaptureStarted() {
        return mCapturedStarted;
    }

    @Override
    public void onCameraFirstFrame() {
        if (mListener != null) {
            mListener.onFrameFrame();
        }
    }
}
