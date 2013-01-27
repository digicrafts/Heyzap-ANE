package com.heyzap.sdk;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;

public class ClickThroughLayout extends FrameLayout {

    private View clickThroughElement;

    public ClickThroughLayout(Context context) {
        super(context);
        init(context);
    }
    public ClickThroughLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }
    public ClickThroughLayout(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init(context);
    }
    
    private void init(Context context){
        this.setClickable(true);
        this.setOnClickListener(new OnClickListener(){
            @Override
            public void onClick(View v){
                if(clickThroughElement != null){
                    clickThroughElement.performClick();
                }
            }
        });
    }
    
    public void setClickThroughElement(View v){
        clickThroughElement = v;

    }
    
    
    @Override
    public boolean onInterceptTouchEvent(MotionEvent event) {
        if(clickThroughElement != null){
            clickThroughElement.onTouchEvent(event);
            return true;
        }else{
            return super.onInterceptTouchEvent(event);
        }
    }
    
    
}
