package com.example.framework.framework.modules.consumers;

import com.example.capture.video.camera.VideoCaptureFrame;
import com.example.framework.framework.modules.channels.VideoChannel;

public interface IVideoConsumer {
    int TYPE_ON_SCREEN = 0;
    int TYPE_OFF_SCREEN = 1;

    void onConsumeFrame(VideoCaptureFrame frame, VideoChannel.ChannelContext context);
    void connectChannel(int channelId);
    void disconnectChannel(int channelId);

    /**
     * Tạo cơ hội cho các lớp con trả về mục tiêu bản vẽ
     * đối tượng. Đối tượng này chỉ có thể là Surface hoặc
     * SurfaceTexture.
     * @return
     */
    Object onGetDrawingTarget();

    int onMeasuredWidth();
    int onMeasuredHeight();
}
