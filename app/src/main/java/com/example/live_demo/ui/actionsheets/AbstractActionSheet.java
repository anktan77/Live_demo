package com.example.live_demo.ui.actionsheets;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.RelativeLayout;

import com.example.live_demo.utils.SharkLiveApplication;

public abstract class AbstractActionSheet extends RelativeLayout {
    //bảng hành động trừu tượng
    public AbstractActionSheet(Context context) {
        super(context);
    }

    public AbstractActionSheet(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public AbstractActionSheet(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public AbstractActionSheet(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }
    public interface AbsActionSheetListener {

    }
    public abstract void setActionSheetListener(AbsActionSheetListener listener);

    protected SharkLiveApplication application() {
        return (SharkLiveApplication) getContext().getApplicationContext();
    }
}
