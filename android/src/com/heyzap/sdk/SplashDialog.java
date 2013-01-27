package com.heyzap.sdk;

import android.content.Context;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

public abstract class SplashDialog extends ClickableToast {

    protected LinearLayout dialogView;
    private View contentView;
    protected int side = 300;
    protected final float scale;

    public SplashDialog(Context context) {
        super(context);
        
        scale = getContext().getResources().getDisplayMetrics().density;
        
        dialogView = new LinearLayout(getContext());
        dialogView.setOrientation(LinearLayout.VERTICAL);
        
        int padding = Utils.dpToPx(context, 2);
        dialogView.setPadding(padding,padding,padding,padding);
        
        int margin = Utils.dpToPx(context, 10);
        this.setPadding(margin,margin,margin,margin);
        
        this.setOnKeyListener(new OnKeyListener(){
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                return false;
            }
        });
        
        addView(dialogView, new ViewGroup.LayoutParams(ViewGroup.LayoutParams.FILL_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
    }
    
    protected View buildDialogView() {

        
        FrameLayout dialogLayout = new FrameLayout(getContext());
        
        View dialogContents = buildDialogContentView();
        dialogContents.setPadding(1, 1, 1, 1);
  
        
        dialogLayout.addView(dialogContents);
        
        FrameLayout.LayoutParams imageViewLayout = new FrameLayout.LayoutParams(FrameLayout.LayoutParams.FILL_PARENT, FrameLayout.LayoutParams.FILL_PARENT);
        ImageView borderImage = new ImageView(getContext());
        borderImage.setAdjustViewBounds(true);
        Drawables.setBackgroundDrawable(borderImage, "dialog_border.png");
        dialogLayout.addView(borderImage, imageViewLayout);
        
        return dialogLayout;
    }
    
    abstract View buildDialogContentView();

    public WindowManager.LayoutParams getWmParams(){
        WindowManager.LayoutParams params = super.getWmParams();
        params.flags = WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL 
                     | WindowManager.LayoutParams.FLAG_WATCH_OUTSIDE_TOUCH 
                     | WindowManager.LayoutParams.FLAG_DIM_BEHIND;
        params.windowAnimations = android.R.style.Animation_Dialog;
        params.width = (int)(side * scale);
        params.height = (int)(side * scale);
        params.verticalMargin = 0.00f;
        params.dimAmount = .5f;
        
        return params;
    }
    
    @Override
    public boolean onTouchEvent(MotionEvent event){
        if(event.getAction() == MotionEvent.ACTION_OUTSIDE){
            this.hide();
        }
        return super.onTouchEvent(event);
    }

    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {
        if(event.getKeyCode() == KeyEvent.KEYCODE_BACK){
            this.hide();
            return true;
        }

        return super.dispatchKeyEvent(event);
    }

    public void setView(View contentView) {
        if(this.contentView != null) {
            dialogView.removeView(contentView);
        }
        
        dialogView.addView(contentView, 0);
        this.contentView = contentView;
    }
}