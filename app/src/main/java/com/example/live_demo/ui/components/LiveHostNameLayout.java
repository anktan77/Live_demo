package com.example.live_demo.ui.components;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.Gravity;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.appcompat.widget.AppCompatImageView;
import androidx.appcompat.widget.AppCompatTextView;
import androidx.core.graphics.drawable.RoundedBitmapDrawable;
import androidx.core.graphics.drawable.RoundedBitmapDrawableFactory;

import com.example.live_demo.R;
import com.example.live_demo.vlive.Config;
import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;

import java.io.IOException;



public class LiveHostNameLayout extends RelativeLayout {
    private static final int IMAGE_VIEW_ID = 1 << 4;

    private int mMaxWidth;
    private int mHeight;
    private AppCompatImageView mIconImageView;
    private AppCompatTextView mNameTextView;

    public LiveHostNameLayout(Context context) {
        super(context);
    }

    public LiveHostNameLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public LiveHostNameLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public void init(boolean lightMode) {
        mMaxWidth = getResources().getDimensionPixelSize(R.dimen.live_name_pad_max_width);
        mHeight = getResources().getDimensionPixelSize(R.dimen.live_name_pad_height);

        if (lightMode) {
            setBackgroundResource(R.drawable.round_scalable_gray_transparent_bg);
        } else {
            setBackgroundResource(R.drawable.round_scalable_gray_bg);
        }

        LayoutParams params;
        // set icon cho khung name
        mIconImageView = new AppCompatImageView(getContext());
        mIconImageView.setId(IMAGE_VIEW_ID);
        addView(mIconImageView);
        int iconPadding = getResources().getDimensionPixelSize(R.dimen.live_name_pad_icon_padding);
        params = (LayoutParams) mIconImageView.getLayoutParams();
        int iconSize = mHeight - iconPadding * 2;
        params.width = iconSize;
        params.height = iconSize;
        params.leftMargin = iconPadding;
        params.addRule(RelativeLayout.ALIGN_PARENT_START, RelativeLayout.TRUE);
        params.addRule(RelativeLayout.CENTER_VERTICAL, RelativeLayout.TRUE);
        mIconImageView.setLayoutParams(params);

        // set name cho khung name
        mNameTextView = new AppCompatTextView(getContext());
        addView(mNameTextView);
        params = (LayoutParams) mNameTextView.getLayoutParams();
        params.addRule(RelativeLayout.END_OF, IMAGE_VIEW_ID);
        params.addRule(RelativeLayout.ALIGN_PARENT_END, RelativeLayout.TRUE);
        params.leftMargin = mHeight / 5;
        params.rightMargin = params.leftMargin;
        params.width = LayoutParams.MATCH_PARENT;
        params.height = LayoutParams.MATCH_PARENT;
        mNameTextView.setLayoutParams(params);

        int textSize = getResources().getDimensionPixelSize(R.dimen.text_size_small);
        mNameTextView.setTextSize(TypedValue.COMPLEX_UNIT_PX, textSize);
        if (lightMode) {
            mNameTextView.setTextColor(Color.BLACK);
        } else {
            mNameTextView.setTextColor(Color.WHITE);
        }
        // set cho name nó chạy ngang
        mNameTextView.setSingleLine(true);
        mNameTextView.setFocusable(true);
        mNameTextView.setFocusableInTouchMode(true);
        mNameTextView.setSelected(true);
        mNameTextView.setEllipsize(TextUtils.TruncateAt.MARQUEE);
        mNameTextView.setMarqueeRepeatLimit(-1);
        mNameTextView.setTextAlignment(TextView.TEXT_ALIGNMENT_GRAVITY);
        mNameTextView.setGravity(Gravity.CENTER);
    }

    public void init() {
        init(false);
    }

    //  onMeasure tùy chỉnh layout phù hợp với khung hình
    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        setMeasuredDimension(mMaxWidth, mHeight);
        int widthSpec = MeasureSpec.makeMeasureSpec(mMaxWidth, MeasureSpec.EXACTLY);
        int heightSpec = MeasureSpec.makeMeasureSpec(mHeight, MeasureSpec.EXACTLY);
        super.onMeasure(widthSpec, heightSpec);
    }

    // set name từ singlehostactivity or multi activity
    public void setName(String name) {
        mNameTextView.setText(name);
    }

    // set drawble từ singlehostactivity or multi activity
    public void setIconNotUrl(Drawable drawable) {
        mIconImageView.setImageDrawable(drawable);
   }

    public void setIconUrl(String imageUrl) {
        Picasso.with(getContext()).load(imageUrl).into(mIconImageView, new Callback() {
            @Override
            public void onSuccess() {
                Bitmap imageBitmap = ((BitmapDrawable) mIconImageView.getDrawable()).getBitmap();
                RoundedBitmapDrawable drawable =
                        RoundedBitmapDrawableFactory.create(getResources(), imageBitmap);
                drawable.setCircular(true);
                mIconImageView.setImageDrawable(drawable);
            }

            @Override
            public void onError() {

            }
        });
    }

    /**
     * For development only, test fake user icon
     * @param name
     */
    public void setIconResource(String name) {
        RoundedBitmapDrawable drawable = null;
        try {
            drawable = RoundedBitmapDrawableFactory.create(getResources(),
                    getResources().getAssets().open(name));
            drawable.setCircular(true);
        } catch (IOException e) {
            e.printStackTrace();
        }
        mIconImageView.setImageDrawable(drawable);
    }
}
