package com.example.live_demo.ui.components;

import android.content.Context;
import android.util.AttributeSet;
import android.view.TextureView;

import com.example.framework.framework.modules.consumers.TextureViewConsumer;

// camera cho prepair activity
public class CameraTextureView extends TextureView {
    public CameraTextureView(Context context) {
        super(context);
        setTextureViewConsumer();
    }

    public CameraTextureView(Context context, AttributeSet attrs) {
        super(context, attrs);
        setTextureViewConsumer();
    }

    // setSurfaceTextureListener
    // dùng để hiển thị lên màn hình những hình ảnh có độ
    // thay đổi khung hình liên tục ví dụ như, video, game
    private void setTextureViewConsumer() {
        setSurfaceTextureListener(new TextureViewConsumer());
    }
}
