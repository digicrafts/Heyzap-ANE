package com.heyzap.sdk;

import android.content.Context;
import android.text.Html;
import android.view.Gravity;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

public class PreMarketCheckinDialog extends PreMarketDialog {
    private final String gameBannerBaseURL = "http://hzmedia-cdn.heyzap.com/mobile_game_banner_";

    protected String prefillMessage;

    public PreMarketCheckinDialog(final Context context, String packageName, String prefillMessage) {
        super(context, packageName);

        this.prefillMessage = prefillMessage;

        setView(buildDialogView());
    }

    @Override
    View buildInfoView() {
        RelativeLayout infoView = new RelativeLayout(getContext());
        Drawables.setBackgroundDrawable(infoView, "checkin_dialog_bg.png");
        
        ImageView circleLogoView = new ImageView(getContext());
        Drawables.setBackgroundDrawable(circleLogoView, "heyzap_circle.png");
        
        RelativeLayout.LayoutParams circleLogoLayout = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
        circleLogoLayout.addRule(RelativeLayout.ALIGN_PARENT_LEFT, -1);
        circleLogoLayout.addRule(RelativeLayout.CENTER_VERTICAL, -1);
        circleLogoLayout.setMargins((int)(10 * scale), 0, 0, 0);
        infoView.addView(circleLogoView, circleLogoLayout);
        
        RelativeLayout.LayoutParams textLayout = new RelativeLayout.LayoutParams((int)(188 * scale), RelativeLayout.LayoutParams.WRAP_CONTENT);
        textLayout.addRule(RelativeLayout.ALIGN_PARENT_RIGHT, -1);
        textLayout.setMargins(0, (int)(11 * scale), (int)(10 * scale), 0);
        TextView playTextView = new TextView(getContext());
        playTextView.setTextColor(0xFFFFFFFF);
        playTextView.setGravity(Gravity.CENTER_HORIZONTAL);
        playTextView.setTextSize(15.0f);
        playTextView.setText(Html.fromHtml("<b>Play games with friends</b>"));
        playTextView.setId(9001);
        
        infoView.addView(playTextView, textLayout);
        
        ImageView peopleImageView = new ImageView(getContext());
        Drawables.setImageDrawable(getContext(), peopleImageView, "dialog_users_ratings.png");
        
        RelativeLayout.LayoutParams peopleImageLayout = new RelativeLayout.LayoutParams((int)(188 * scale), (int)(35 * scale));
        peopleImageLayout.addRule(RelativeLayout.ALIGN_PARENT_RIGHT, -1);
        peopleImageLayout.addRule(RelativeLayout.BELOW, playTextView.getId());
        peopleImageLayout.setMargins(0, (int)(2 * scale), (int)(10 * scale), 0);
        
        infoView.addView(peopleImageView, peopleImageLayout);
        
        return infoView;
    }

    @Override
    View buildBannerView() {
        final float scale = getContext().getResources().getDisplayMetrics().density;
          
        RelativeLayout bannerView = new RelativeLayout(getContext());
        bannerView.setBackgroundColor(0xFF000000);
          
        // Banner
        RelativeLayout.LayoutParams bannerImageLayout = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.FILL_PARENT, (int)(134 * scale));
        ImageView bannerImageView = new ImageView(getContext());
        bannerImageView.setAdjustViewBounds(true);
        Drawables.setImageDrawable(getContext(), bannerImageView, "dialog_banner_default.jpg");
        
        bannerView.addView(bannerImageView, bannerImageLayout);
          
        DrawableManager drawableManager = new DrawableManager();      
        drawableManager.fetchDrawableOnThread(createBannerUrl(), bannerImageView);
          
        // White Highlight
        LinearLayout whiteView = new LinearLayout(getContext());
        whiteView.setBackgroundColor(0x40FFFFFF);
        whiteView.setId(9001);
        RelativeLayout.LayoutParams whitePixelLayout = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.FILL_PARENT, (int)(1 * scale));
        whitePixelLayout.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM, -1);
        bannerView.addView(whiteView, whitePixelLayout);
          
        // Checkin Text 
        RelativeLayout.LayoutParams  bannerTextLayout = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.FILL_PARENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
        bannerTextLayout.addRule(RelativeLayout.ABOVE, whiteView.getId());
          
        View bannerTextView = buildBannerTextView(Html.fromHtml("<b><font color='#FEBE1F'>Check in</font></b> to <b>" + this.gameName + "</b>"));        
        bannerView.addView(bannerTextView, bannerTextLayout);
          
        return bannerView;
    }

    @Override
    protected String getAdditionalAnalyticsParams() {
        String additionalParams = String.format("action=checkin&game_package=%s", packageName);

        if(prefillMessage != null) {
            additionalParams += String.format("&message=%s", prefillMessage);
        }

        return additionalParams;
    }

    @Override
    protected void fireInstallClickedAnalytics() {
        HeyzapAnalytics.trackEvent(getContext(), "install-button-clicked");
    }

    @Override
    protected void fireSkipClickedAnalytics() {
        HeyzapAnalytics.trackEvent(getContext(), "skip-button-clicked");
    }

    protected View buildBannerTextView(CharSequence text) {
        LinearLayout checkinTextLayout = new LinearLayout(getContext());
        checkinTextLayout.setOrientation(LinearLayout.HORIZONTAL);
        checkinTextLayout.setBackgroundColor(0xBF000000);
        checkinTextLayout.setPadding(10, 10, 10, 10);
        
        LinearLayout.LayoutParams checkinTextViewLayoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.FILL_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        
        TextView checkinTextView = new TextView(getContext());
        checkinTextView.setTextColor(0xFFFFFFFF);
        checkinTextView.setGravity(Gravity.CENTER_HORIZONTAL);
        checkinTextView.setTextSize(18.0f);
        checkinTextView.setText(text);
        checkinTextLayout.addView(checkinTextView, checkinTextViewLayoutParams);
  
        return checkinTextLayout;
    }
    
    private String createBannerUrl() {
        return this.gameBannerBaseURL + this.packageName;
    }
}
