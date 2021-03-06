package com.example.framework.framework.modules.channels;

import android.content.Context;
import android.opengl.EGLContext;
import android.opengl.EGLSurface;
import android.os.Handler;
import android.os.HandlerThread;
import android.util.Log;

import com.example.capture.video.camera.VideoCaptureFrame;
import com.example.framework.framework.helpers.gles.ProgramTexture2d;
import com.example.framework.framework.helpers.gles.ProgramTextureOES;
import com.example.framework.framework.helpers.gles.core.EglCore;
import com.example.framework.framework.modules.consumers.IVideoConsumer;
import com.example.framework.framework.modules.processors.IPreprocessor;
import com.example.framework.framework.modules.processors.RotateProcessor;
import com.example.framework.framework.modules.producers.IVideoProducer;

import java.util.ArrayList;
import java.util.List;

public class VideoChannel extends HandlerThread {
    private static final String TAG = VideoChannel.class.getSimpleName();

    private int mChannelId;
    private boolean mOffScreenMode;

    private IVideoProducer mProducer;
    private List<IVideoConsumer> mOnScreenConsumers = new ArrayList<>();
    private List<IVideoConsumer> mOffScreenConsumers = new ArrayList<>();
    private IPreprocessor mPreprocessor;

    /// Được sử dụng để xoay hình ảnh theo hướng bình thường theo
    // đến ma trận chuyển đổi kết cấu và có thể là bề mặt
    // quay nếu bề mặt không quay tự nhiên.
    private RotateProcessor mRotateProcessor;

    private Handler mHandler;

    private ChannelContext mContext;
    private EGLSurface mDummyEglSurface;

    public VideoChannel(Context context, int id) {
        super(ChannelManager.ChannelID.toString(id));
        mChannelId = id;
        mContext = new ChannelContext();
        mContext.setContext(context);
    }

    void setPreprocessor(IPreprocessor preprocessor) {
        mPreprocessor = preprocessor;
    }

    @Override
    public void run() {
        init();
        super.run();
        release();
    }

    private void init() {
        Log.i(TAG, "channel opengl init");
        initOpenGL();
        initPreprocessor();
        initRotateProcessor();
        onChannelContextCreated();
    }

    // Giai đoạn khởi tạo cho các lớp con
    protected void onChannelContextCreated() {

    }

    private void initOpenGL() {
        EglCore eglCore = new EglCore();
        mContext.setEglCore(eglCore);
        mDummyEglSurface = eglCore.createOffscreenSurface(1, 1);
        eglCore.makeCurrent(mDummyEglSurface);
        mContext.setProgram2D(new ProgramTexture2d());
        mContext.setProgramOES(new ProgramTextureOES());
    }

    private void initPreprocessor() {
        if (mPreprocessor != null) {
            mPreprocessor.initPreprocessor();
        }
    }

    private void initRotateProcessor() {
        mRotateProcessor = new RotateProcessor();
        mRotateProcessor.init(mContext);
    }

    private void release() {
        Log.i(TAG, "channel opengl release");
        releasePreprocessor();
        releaseRotateProcessor();
        releaseOpenGL();
    }

    private void releasePreprocessor() {
        if (mPreprocessor != null) {
            mPreprocessor.releasePreprocessor(getChannelContext());
            mPreprocessor = null;
        }
    }

    private void releaseRotateProcessor() {
        if (mRotateProcessor != null) {
            mRotateProcessor.release(mContext);
            mRotateProcessor = null;
        }
    }

    private void releaseOpenGL() {
        mContext.getProgram2D().release();
        mContext.getProgramOES().release();
        mContext.getEglCore().releaseSurface(mDummyEglSurface);
        mContext.getEglCore().release();
        mContext = null;
    }

    public ChannelContext getChannelContext() {
        return mContext;
    }

    public IPreprocessor getPreprocessor() {
        return mPreprocessor;
    }

    void startChannel() {
        if (isRunning()) {
            return;
        }
        start();
        mHandler = new Handler(getLooper());
    }

    public Handler getHandler() {
        checkThreadRunningState();
        return mHandler;
    }

    void stopChannel() {
        Log.i(TAG, "StopChannel");
        if (mProducer != null) {
            mProducer.disconnect();
            mProducer = null;
        }

        if (!mOffScreenConsumers.isEmpty()) {
            for (IVideoConsumer consumer : mOffScreenConsumers) {
                consumer.disconnectChannel(mChannelId);
            }
        }
        mOffScreenConsumers.clear();

        removeOnScreenConsumer();
        quit();
    }

    private void resetOpenGLSurface() {
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                makeDummySurfaceCurrent();
            }
        });
    }

    private void removeOnScreenConsumer() {
        if (mOnScreenConsumers != null) {
            mHandler.post(new Runnable() {
                @Override
                public void run() {
                    mOnScreenConsumers.clear();
                    // To remove on-screen consumer, we need
                    // to reset the GLSurface and maintain
                    // the OpenGL context properly.
                    makeDummySurfaceCurrent();
                }
            });
        }
    }

    public boolean isRunning() {
        return isAlive();
    }

    void connectProducer(IVideoProducer producer) {
        checkThreadRunningState();
        if (mProducer == null) {
            mProducer = producer;
        }
    }

    public void disconnectProducer() {
        checkThreadRunningState();
        mProducer = null;
    }

    /**
     * Attach a consumer to the channel
     * @param consumer consumer to be attached
     * @param type on-screen or off-screen
     * @see com.example.framework.framework.modules.consumers.IVideoConsumer
     */
    public void connectConsumer(final IVideoConsumer consumer, int type) {
        checkThreadRunningState();

        mHandler.post(() -> {
            if (type == IVideoConsumer.TYPE_ON_SCREEN) {
                if (!mOnScreenConsumers.contains(consumer)) {
                    Log.d(TAG, "On-screen consumer connected:" + consumer);
                    mOnScreenConsumers.add(consumer);
                }
            } else if (type == IVideoConsumer.TYPE_OFF_SCREEN) {
                if (!mOffScreenConsumers.contains(consumer)) {
                    Log.d(TAG, "Off-screen consumer connected:" + consumer);
                    mOffScreenConsumers.add(consumer);
                }
            }
        });
    }

    public void disconnectConsumer(IVideoConsumer consumer) {
        checkThreadRunningState();

        mHandler.post(() -> {
            if (mOnScreenConsumers.contains(consumer)) {
                mOnScreenConsumers.remove(consumer);
                Log.d(TAG, "On-screen consumer disconnected:" + consumer);
            } else {
                mOffScreenConsumers.remove(consumer);
                Log.d(TAG, "Off-screen consumer disconnected:" + consumer);
                if (mOnScreenConsumers.isEmpty() &&
                        mOffScreenConsumers.isEmpty()) {

                    resetOpenGLSurface();
                }
            }
        });
    }

    public void enablePreProcess(boolean enabled) {
        if (mPreprocessor != null) {
            mHandler.post(() -> mPreprocessor.enablePreProcess(enabled));
        }
    }

    public void pushVideoFrame(VideoCaptureFrame frame) {
        checkThreadRunningState();

        if (mPreprocessor != null) {
            frame = mPreprocessor.onPreProcessFrame(frame, getChannelContext());
            makeDummySurfaceCurrent();
        }

        if (mRotateProcessor != null) {
            // Xoay hình ảnh về trạng thái cuối cùng.
            // Thủ tục xoay vòng tiếp theo sẽ không
            // cần thiết cho tất cả người dùng.
            frame = mRotateProcessor.process(frame, getChannelContext());
            makeDummySurfaceCurrent();
        }

        if (mOnScreenConsumers.size() > 0) {

            mOnScreenConsumers.get(mOnScreenConsumers.size() - 1).onConsumeFrame(frame, mContext);
            makeDummySurfaceCurrent();
        }

        if (mOnScreenConsumers.size() > 0 || mOffScreenMode) {

            for (IVideoConsumer consumer : mOffScreenConsumers) {
                consumer.onConsumeFrame(frame, mContext);
                makeDummySurfaceCurrent();
            }
        }
    }

    private void makeDummySurfaceCurrent() {

        if (!mContext.isCurrent(mDummyEglSurface)) {
            mContext.makeCurrent(mDummyEglSurface);
        }
    }

    private void checkThreadRunningState() {
        if (!isAlive()) {
            throw new IllegalStateException("Kênh video không tồn tại");
        }
    }

    void enableOffscreenMode(boolean enabled) {
        mOffScreenMode = enabled;
    }

    public static class ChannelContext {
        private Context mContext;
        private EglCore mEglCore;
        private ProgramTexture2d mProgram2D;
        private ProgramTextureOES mProgramOES;

        public Context getContext() {
            return mContext;
        }

        public void setContext(Context context) {
            this.mContext = context;
        }

        public EglCore getEglCore() {
            return mEglCore;
        }

        private void setEglCore(EglCore mEglCore) {
            this.mEglCore = mEglCore;
        }

        public EGLContext getEglContext() {
            return getEglCore().getEGLContext();
        }

        public ProgramTexture2d getProgram2D() {
            return mProgram2D;
        }

        private void setProgram2D(ProgramTexture2d mFullFrameRectTexture2D) {
            this.mProgram2D = mFullFrameRectTexture2D;
        }

        public ProgramTextureOES getProgramOES() {
            return mProgramOES;
        }

        private void setProgramOES(ProgramTextureOES mTextureOES) {
            this.mProgramOES = mTextureOES;
        }

        public EGLSurface getCurrentSurface() {
            return mEglCore.getCurrentDrawingSurface();
        }

        public void makeCurrent(EGLSurface surface) {
            mEglCore.makeCurrent(surface);
        }

        public boolean isCurrent(EGLSurface surface) {
            return mEglCore.isCurrent(surface);
        }
    }
}
