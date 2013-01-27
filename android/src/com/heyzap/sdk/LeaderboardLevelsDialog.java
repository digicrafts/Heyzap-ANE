package com.heyzap.sdk;

import android.content.Context;
import android.graphics.Rect;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.FrameLayout;

public class LeaderboardLevelsDialog extends ClickableToast {
    private FeedView feedView;
    FrameLayout feedFrame;
    private OnHideListener onHideListener;

    public LeaderboardLevelsDialog(Context context) {
        super(context);
        this.setContentView(Rzap.layout("leaderboard_levels_dialog"));
        this.setFocusable(true);
        this.setFocusableInTouchMode(true);

        feedFrame = (FrameLayout) this.findViewById(Rzap.id("levels_feed_frame"));
    }

    @Override
    public WindowManager.LayoutParams getWmParams() {
        WindowManager.LayoutParams params = super.getWmParams();
        params.gravity = Gravity.CENTER;
        params.height = ViewGroup.LayoutParams.WRAP_CONTENT;
        params.width = ViewGroup.LayoutParams.WRAP_CONTENT;
//        params.verticalMargin = 0.0f;
        params.flags &= ~WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN;
        params.flags &= ~WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE;
        params.flags |= WindowManager.LayoutParams.FLAG_WATCH_OUTSIDE_TOUCH;
        return params;
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (event.getAction() == MotionEvent.ACTION_OUTSIDE) {
            this.hide();
            return false;
        } else {
            return super.onTouchEvent(event);
        }
    }

    @Override
    public void hide() {
        super.hide();
        if (onHideListener != null) {
            onHideListener.onHide();
        }
    }

    public void setFeedView(FeedView feedView) {
        this.feedView = feedView;
        feedFrame.removeAllViews();
        feedFrame.addView(feedView);
    }

    public void setOnHideListener(OnHideListener l) {
        onHideListener = l;
    }

    public interface OnHideListener {
        public void onHide();
    }

    @Override
    public void show() {
        super.show();
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            this.hide();
            return true;
        } else {
            return super.onKeyDown(keyCode, event);
        }
    }

}