package com.example.capture.video.camera;

import android.content.Context;
import android.view.SurfaceView;
import android.view.TextureView;

import com.example.framework.framework.modules.channels.ChannelManager;
import com.example.framework.framework.modules.consumers.IVideoConsumer;
import com.example.framework.framework.modules.consumers.SurfaceViewConsumer;
import com.example.framework.framework.modules.consumers.TextureViewConsumer;
import com.example.framework.framework.modules.processors.IPreprocessor;



//        * VideoManager được thiết kế như một gói đóng gói cấp cao hơn của
//        * mô-đun video. Nó mở một loạt API ra thế giới bên ngoài,
//        * và làm cho hành vi của máy ảnh dễ dàng hơn nhiều bằng cách chứa một số
//        * các thủ tục logic của máy ảnh.
//        * Nó có thể được xem như một lớp tiện ích cụ thể để kiểm soát
//        * kênh video camera, được định nghĩa là một trong những triển khai
//        * của kênh video được thiết kế trong khuôn khổ.
//        * Duy trì một phiên bản CameraManager duy nhất trên toàn cầu là đủ.
//        * Mặc dù có thể tạo một phiên bản bất cứ khi nào nó cần
//        * Kiểm soát camera, hành vi như vậy khó có thể mang lại lợi ích.
//        * /
public class CameraManager {
    // CameraManager only controls camera channel
    private static final int CHANNEL_ID = ChannelManager.ChannelID.CAMERA; // default = 0

    private static final int DEFAULT_FACING = Constant.CAMERA_FACING_FRONT; // default = 0

    private CameraVideoChannel mCameraChannel;

//    / **
//            * Khởi tạo kênh video camera, tải tất cả
//      * tài nguyên cần thiết trong quá trình chụp ảnh bằng máy ảnh.
//            * @param context Bối cảnh Android
//      * Bộ xử lý tiền @param thường là phần triển khai
//      * của thư viện làm đẹp của bên thứ ba
//      * @param đối mặt phải là một trong Hằng số.CAMERA_FACING_FRONT
//      * và Constant.CAMERA_FACING_BACK
//      * @see com.example.capture.video.camera.Constant
//      * /
    public CameraManager(Context context, IPreprocessor preprocessor, int facing) {
        init(context, preprocessor, facing);
    }

    // context là sharkliveapplication
    // DEFAULT_FACING=0
    public CameraManager(Context context, IPreprocessor preprocessor) {
        init(context, preprocessor, DEFAULT_FACING);
    }


    // lấy từ class videoModule
    private void init(Context context, IPreprocessor preprocessor, int facing) {
        VideoModule videoModule = VideoModule.instance();
        if (!videoModule.hasInitialized()) {
            videoModule.init(context);
        }

        // Bộ tiền xử lý phải được đặt trước
        // kênh video bắt đầu
        videoModule.setPreprocessor(CHANNEL_ID, preprocessor);
        // * cái này quan trọng, phải thiết lập module.instance
        videoModule.startChannel(CHANNEL_ID);

        mCameraChannel = (CameraVideoChannel)
                videoModule.getVideoChannel(CHANNEL_ID);
        mCameraChannel.setFacing(facing);
    }

    public void enablePreprocessor(boolean enabled) {
        if (mCameraChannel != null) {
            mCameraChannel.enablePreProcess(enabled);
        }
    }

    public void setLocalPreview(TextureView textureView) {
        TextureViewConsumer consumer = new TextureViewConsumer();
        textureView.setSurfaceTextureListener(consumer);

        if (textureView.isAttachedToWindow()) {
            consumer.setDefault(textureView.getSurfaceTexture(),
                    textureView.getMeasuredWidth(),
                    textureView.getMeasuredHeight());
            consumer.connectChannel(CHANNEL_ID);
        }
    }


    public void setLocalPreview(SurfaceView surfaceView) {
        SurfaceViewConsumer consumer = new SurfaceViewConsumer(surfaceView);
        surfaceView.getHolder().addCallback(consumer);

        if (surfaceView.isAttachedToWindow()) {
            consumer.setDefault();
            consumer.connectChannel(CHANNEL_ID);
        }
    }

    public void setCameraStateListener(CameraVideoChannel.OnCameraStateListener listener) {
        if (mCameraChannel != null) {
            mCameraChannel.setCameraStateListener(listener);
        }
    }

    public void setFacing(int facing) {
        if (mCameraChannel != null) {
            mCameraChannel.setFacing(facing);
        }
    }

    public void setPictureSize(int width, int height) {
        if (mCameraChannel != null) {
            mCameraChannel.setPictureSize(width, height);
        }
    }


    public void attachOffScreenConsumer(IVideoConsumer consumer) {
        if (mCameraChannel != null) {
            mCameraChannel.connectConsumer(consumer, IVideoConsumer.TYPE_OFF_SCREEN);
        }
    }

    public void detachOffScreenConsumer(IVideoConsumer consumer) {
        if (mCameraChannel != null) {
            mCameraChannel.disconnectConsumer(consumer);
        }
    }

    public void setFrameRate(int frameRate) {
        if (mCameraChannel != null) {
            mCameraChannel.setIdealFrameRate(frameRate);
        }
    }

    public void startCapture() {
        if (mCameraChannel != null) {
            mCameraChannel.startCapture();
        }
    }

    public void stopCapture() {
        if (mCameraChannel != null) {
            mCameraChannel.stopCapture();
        }
    }

    public void switchCamera() {
        if (mCameraChannel != null) {
            mCameraChannel.switchCamera();
        }
    }

    public IPreprocessor getPreprocessor() {
        if (mCameraChannel != null) {
            return VideoModule.instance().getPreprocessor(CHANNEL_ID);
        }

        return null;
    }
}
