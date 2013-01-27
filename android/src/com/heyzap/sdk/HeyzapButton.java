package com.heyzap.sdk;

import android.content.Context;
import android.graphics.Color;
import android.util.AttributeSet;
import android.view.View;
import android.widget.ImageButton;

public class HeyzapButton extends ImageButton {
    private String checkinMessage = "";
    private int checkinType = 0;
    private Context context;

    public HeyzapButton(Context context) {
        super(context);
        init(context, null);
    }

    public HeyzapButton(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs);
    }

    public HeyzapButton(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init(context, attrs);
    }

    private void init(final Context context, final AttributeSet attrs) {
        this.context = context;
        // Hide the heyzap button if we are on a really old phone
        if(!Utils.androidVersionSupported()){
            setVisibility(View.INVISIBLE);
            return;
        }
        
        // Load in the checkin message
        if(attrs != null) {
            String checkinMessageAttr = attrs.getAttributeValue(null, "checkinMessage");
            if(checkinMessageAttr != null && checkinMessage != null) {
                checkinMessage = checkinMessageAttr;
            }
        }

        this.setImageResource(Rzap.drawable("sdk_checkin"));
        setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View arg0) {
                HeyzapLib.checkin(getContext(), checkinMessage);
            }
        });
        
        setBackgroundColor(Color.TRANSPARENT);
        setAdjustViewBounds(true);
        drawableStateChanged();
        HeyzapLib.broadcastEnableSDK(context);
    }

    public void setCheckinMessage(String message) {
        this.checkinMessage = message;
    }
    
    public String getCheckinMessage(){
        return this.checkinMessage;
    }
}