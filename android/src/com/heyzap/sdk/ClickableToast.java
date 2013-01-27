package com.heyzap.sdk;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

import android.app.Application;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Configuration;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.PixelFormat;
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Surface;
import android.view.View;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.PopupWindow;

class ClickableToast extends FrameLayout {
    protected WindowManager windowManager;
    private static DismissToastBroadcastReceiver dismissReceiver = new DismissToastBroadcastReceiver();
    private Integer windowAnimation;
    
    private View container;

    public ClickableToast(Context context) {
        super(context);
        init();
    }
    
    public void init(){
        dismissReceiver.setToast(this);
        this.setBackgroundColor(Color.TRANSPARENT);
        this.windowManager = (WindowManager) this.getContext().getSystemService(Application.WINDOW_SERVICE);
    }
    
    public void onAttachedToWindow(){
        WindowManager.LayoutParams params = getWmParams();
        try{
            this.windowManager.updateViewLayout(this, params);
        }catch(RuntimeException e){}
    }
    
    @Override
    public void onDraw(Canvas canvas){
        canvas.rotate(180);
        super.onDraw(canvas);
        canvas.rotate(180);
    }
    
    public void show(){
        WindowManager.LayoutParams params = getWmParams();
        this.windowManager.addView(this, params);
        getContext().registerReceiver(dismissReceiver, new IntentFilter(Intent.ACTION_CLOSE_SYSTEM_DIALOGS));
    }
    
    public void show(final int duration){
        this.show();
        this.postDelayed(new Runnable(){
            public void run(){
                hide();
            }
        }, duration);
    }
    
    public WindowManager.LayoutParams getWmParams(){
        WindowManager.LayoutParams params = new WindowManager.LayoutParams();
        params.height = WindowManager.LayoutParams.WRAP_CONTENT;
        params.width = WindowManager.LayoutParams.WRAP_CONTENT;
        params.alpha = 1;
        params.format = PixelFormat.RGBA_8888;
        params.gravity = Gravity.CENTER;
        params.verticalMargin = isNarrow() ? 0.05f : 0.07f;
        params.flags =  WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL | WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE;
        params.type = WindowManager.LayoutParams.LAST_SYSTEM_WINDOW + 30;
        return params;
    }
    
    public boolean isNarrow(){
        DisplayMetrics displayMetrics = getContext().getResources().getDisplayMetrics();
        return displayMetrics.heightPixels > displayMetrics.widthPixels;
    }
    
    public boolean isVertical(){
        int rotation = this.windowManager.getDefaultDisplay().getOrientation();
        return rotation == Surface.ROTATION_0 
            || rotation == Surface.ROTATION_180;
    }
    
    public int getSlideDownAnimation(Context ctx){
        if(windowAnimation != null){
            return windowAnimation;
        }
        
        PopupWindow w = new PopupWindow(ctx);
        try {
            Field mIsDropDown = PopupWindow.class.getDeclaredField("mIsDropdown");
            mIsDropDown.setAccessible(true);
            mIsDropDown.setBoolean(w, true);

            Field mAnimationStyle = PopupWindow.class.getDeclaredField("mAnimationStyle");
            mAnimationStyle.setAccessible(true);
            mAnimationStyle.setInt(w, -1);

            Field mAboveAnchor = PopupWindow.class.getDeclaredField("mAboveAnchor");
            mAboveAnchor.setAccessible(true);
            mAboveAnchor.setBoolean(w, false);
            
            Method computeAnimationResource = PopupWindow.class.getDeclaredMethod("computeAnimationResource");
            computeAnimationResource.setAccessible(true);
            windowAnimation = (Integer) computeAnimationResource.invoke(w);
        } catch (Exception e){}
        
        if(windowAnimation == null){
            windowAnimation = android.R.style.Animation_Toast;
        }
        
        return windowAnimation;
    }
    
    
    public static void show(Context ctx, View v, int duration){
        ClickableToast toast = new ClickableToast(ctx);
        toast.addView(v);
        toast.show(duration);
    }
    public void hide() {
        try{
            this.windowManager.removeView(this);
        }catch(RuntimeException e){} // this happens when the view has already been removed
        try{
            getContext().unregisterReceiver(dismissReceiver);
        }catch(RuntimeException e){} //I know of no scenario where this would happen
    }
    
    public static class DismissToastBroadcastReceiver extends BroadcastReceiver{
        private ClickableToast toast;
        
        public void setToast(ClickableToast toast){
            this.toast = toast;
        }
        
        @Override
        public void onReceive(Context context, Intent intent) {
            if(toast != null){
                toast.hide();
            }
        }
    };
    
    public void setContentView(int layoutId) {
        this.removeAllViews();

        LayoutInflater inflater = (LayoutInflater) getContext().getSystemService(Application.LAYOUT_INFLATER_SERVICE);
        container = inflater.inflate(layoutId, null);
        this.addView(container);
    }
    

}
