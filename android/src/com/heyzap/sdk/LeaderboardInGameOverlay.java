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
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

public class LeaderboardInGameOverlay extends ClickableToast {
    ImageView people;
    View whiteBg;
    Button installButton;
    TextView bigText;
    private long shownAt;
    private String displayName = null;

    public LeaderboardInGameOverlay(Context context, final String source) {
        super(context);
        this.setContentView(Rzap.layout("leaderboard_in_game_overlay"));
        this.setFocusable(true);
        this.setFocusableInTouchMode(true);

        people = (ImageView) findViewById(Rzap.id("people"));
        whiteBg = (View) findViewById(Rzap.id("white_bg"));
        installButton = (Button) findViewById(Rzap.id("install_button"));
        bigText = (TextView) findViewById(Rzap.id("big_text"));

        bigText.setText(Html.fromHtml("<b>Install Heyzap to <font color='#52aa00'>SAVE SCORES</font></b> and rank against millions"));

        Button installButton = (Button) findViewById(Rzap.id("install_button"));
        
        HeyzapAnalytics.trackEvent(getContext(), String.format("score-in-game-overlay-shown-%s", source));


        OnClickListener marketListener = new OnClickListener() {

            @Override
            public void onClick(View v) {
                // Launch the android market and close this dialog

                String extraParams = "action=leaderboard";
                if (displayName != null) {
                    extraParams = extraParams + "&" + "display_name" + "=" + displayName;
                }

        		HeyzapAnalytics.trackEvent(getContext(), String.format("score-in-game-overlay-install-clicked-%s", source));
                String uri = String.format("market://details?id=%s&referrer=%s", HeyzapLib.HEYZAP_PACKAGE,
                        HeyzapAnalytics.getAnalyticsReferrer(getContext(), String.format("action=leaderboard-%s", source)));

                Intent popup = new Intent(Intent.ACTION_VIEW, Uri.parse(uri));
                popup.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_MULTIPLE_TASK);
                getContext().startActivity(popup);
                hide();
            };
        };
        installButton.setOnClickListener(marketListener);

        setOrientation(getResources().getConfiguration().orientation);
    }

    @Override
    public WindowManager.LayoutParams getWmParams() {
        WindowManager.LayoutParams params = super.getWmParams();
        params.gravity = Gravity.BOTTOM | Gravity.CENTER_HORIZONTAL;
        params.width = WindowManager.LayoutParams.FILL_PARENT;
        params.verticalMargin = 0.0f;
        params.horizontalMargin = 0.0f;
        params.flags &= ~WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN;
        params.flags &= ~WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE;
        params.flags |= WindowManager.LayoutParams.FLAG_WATCH_OUTSIDE_TOUCH;
        return params;
    }

    @Override
    public void onConfigurationChanged(Configuration config) {
        setOrientation(config.orientation);
    }

    protected void setOrientation(int orientation) {
        Integer bigTextTopMargin = null;
        Integer installButtonTopMargin = null;
        Integer peopleTopMargin = null;

        if (orientation == Configuration.ORIENTATION_LANDSCAPE) {
            bigTextTopMargin = 8;
            installButtonTopMargin = 6;
            peopleTopMargin = 9;
        } else if (orientation == Configuration.ORIENTATION_PORTRAIT) {
            bigTextTopMargin = 19;
            installButtonTopMargin = 11;
            peopleTopMargin = 12;
        }

        if (bigTextTopMargin != null && bigText != null) {
            RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) bigText.getLayoutParams();
            params.setMargins(params.leftMargin, Utils.getScaledSize(getContext(), bigTextTopMargin), params.rightMargin, params.bottomMargin);
            bigText.setLayoutParams(params);
        }

        if (installButtonTopMargin != null && installButton != null) {
            RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) installButton.getLayoutParams();
            params.setMargins(params.leftMargin, Utils.getScaledSize(getContext(), installButtonTopMargin), params.rightMargin, params.bottomMargin);
            installButton.setLayoutParams(params);
        }

        if (peopleTopMargin != null && people != null) {
            RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) people.getLayoutParams();
            params.setMargins(params.leftMargin, Utils.getScaledSize(getContext(), peopleTopMargin), params.rightMargin, params.bottomMargin);
            people.setLayoutParams(params);
        }
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

    public void setDisplayName(String displayName) {
        this.displayName  = displayName;
    }
}
