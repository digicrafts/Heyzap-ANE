package com.heyzap.sdk;

import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.net.Uri;
import android.text.Html;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

public class LeaderboardFullOverlay extends ClickableToast {
    ImageView people;
    View whiteBg;
    private long shownAt;

    public LeaderboardFullOverlay(Context context) {
        super(context);
        this.setContentView(Rzap.layout("leaderboard_full_overlay"));
        this.setFocusable(true);
        this.setFocusableInTouchMode(true);

        TextView bigText = (TextView) findViewById(Rzap.id("big_text"));
        people = (ImageView) findViewById(Rzap.id("people"));
        whiteBg = (View) findViewById(Rzap.id("white_bg"));

        bigText.setText(Html.fromHtml("<b>Play and <font color='#52aa00'>rank</font></b> <br /> against millions of players"));

        Button rejectButton = (Button) findViewById(Rzap.id("reject_button"));
        rejectButton.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                LeaderboardFullOverlay.this.hide();
            }
        });

        trackIfNotSubclassed(String.format("score-full-overlay-shown-first"));

        Button installButton = (Button) findViewById(Rzap.id("install_button"));

        
        OnClickListener marketListener = new OnClickListener() {

            @Override
            public void onClick(View v) {
            	launchMarket();
                hide();
            };
        };
        installButton.setOnClickListener(marketListener);

        setOrientation(getResources().getConfiguration().orientation);
    }

    @Override
    public WindowManager.LayoutParams getWmParams() {
        WindowManager.LayoutParams params = super.getWmParams();
        params.width = WindowManager.LayoutParams.FILL_PARENT;
        params.gravity = Gravity.BOTTOM | Gravity.CENTER_HORIZONTAL;
        params.verticalMargin = 0.0f;
        params.flags &= ~WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN;
        params.flags &= ~WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE;
        params.flags |= WindowManager.LayoutParams.FLAG_WATCH_OUTSIDE_TOUCH;
        return params;
    }

    @Override
    public void onConfigurationChanged(Configuration config) {
        setOrientation(config.orientation);
    }

    public void setOrientation(int orientation) {
        android.view.ViewGroup.LayoutParams params = null;
        if (whiteBg != null) {
            params = whiteBg.getLayoutParams();
        }
        if (orientation == Configuration.ORIENTATION_LANDSCAPE) {
            if (people != null) {
                people.setVisibility(View.GONE);
            }
            if (params != null) {
                params.height = Utils.getScaledSize(getContext(), 206 - 64);
            }
        } else if (orientation == Configuration.ORIENTATION_PORTRAIT) {
            if (people != null) {
                people.setVisibility(View.VISIBLE);
            }
            if (params != null) {
                params.height = Utils.getScaledSize(getContext(), 206);
            }
        }
        if (whiteBg != null && params != null) {
            whiteBg.setLayoutParams(params);
        }
    }
    
    public void trackIfNotSubclassed(String event){
    	if(this.getClass().equals(LeaderboardFullOverlay.class)){
    		HeyzapAnalytics.trackEvent(getContext(), event);
    	}
    }
    
    public void launchMarket(){
        // Launch the android market and close this dialog
        String uri = String.format("market://details?id=%s&referrer=%s", HeyzapLib.HEYZAP_PACKAGE,
        HeyzapAnalytics.getAnalyticsReferrer(getContext(), "action=leaderboard-first"));

        HeyzapAnalytics.trackEvent(getContext(), String.format("score-full-overlay-clicked-first"));
        Intent popup = new Intent(Intent.ACTION_VIEW, Uri.parse(uri));
        popup.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_MULTIPLE_TASK);
        getContext().startActivity(popup);
    }

    @Override
    public void show() {
        shownAt = System.currentTimeMillis();
        super.show();
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (event.getAction() == MotionEvent.ACTION_OUTSIDE && System.currentTimeMillis() > shownAt + 1000) {
            this.hide();
            return false;
        } else {
            return super.onTouchEvent(event);
        }
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
