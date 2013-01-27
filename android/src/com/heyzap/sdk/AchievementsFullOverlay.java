package com.heyzap.sdk;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.text.Html;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

public class AchievementsFullOverlay extends LeaderboardFullOverlay {

    public AchievementsFullOverlay(Context context) {
        super(context);
        
        TextView bigText = (TextView) findViewById(Rzap.id("big_text"));
        
        String verb = Utils.heyzapIsInstalled(context) ? "Update" : "Install";
        bigText.setText(Html.fromHtml(String.format("<b>%s Heyzap to <br/><font color='#52aa00'>EARN ACHIEVEMENTS</font></b>", verb)));
        // failsafe for text cutoff:
        int width = context.getResources().getDisplayMetrics().widthPixels;
        if (width < 500) {
	        TextView pedestalText = (TextView) findViewById(Rzap.id("pedestal_text"));
	        pedestalText.setVisibility(View.GONE);
	        TextView controllerText = (TextView) findViewById(Rzap.id("controller_text"));
	        controllerText.setVisibility(View.GONE);
	        TextView friendsText = (TextView) findViewById(Rzap.id("friends_text"));
	        friendsText.setVisibility(View.GONE);

	        updateLayoutParams(context, "bubble_friends", 75);
	        updateLayoutParams(context, "bubble_pedestal", 81);
	        updateLayoutParams(context, "bubble_trophy", 78);
        }
    }
    
    public void launchMarket(){
        // Launch the android market and close this dialog
        String uri = String.format("market://details?id=%s&referrer=%s", HeyzapLib.HEYZAP_PACKAGE,
        HeyzapAnalytics.getAnalyticsReferrer(getContext(), "action=achievements"));

        HeyzapAnalytics.trackEvent(getContext(), String.format("achievements-heyzap-prompt-clicked"));
        Intent popup = new Intent(Intent.ACTION_VIEW, Uri.parse(uri));
        popup.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_MULTIPLE_TASK);
        getContext().startActivity(popup);
    }
    
    public void show(){
    	HeyzapAnalytics.trackEvent(getContext(), "achievements-heyzap-prompt-shown");
    	super.show();
    }

    
    private void updateLayoutParams(Context context, String id, int offset) {
        ImageView bubble = (ImageView) findViewById(Rzap.id(id));
        android.widget.RelativeLayout.LayoutParams params = (android.widget.RelativeLayout.LayoutParams) bubble.getLayoutParams();
        params.bottomMargin = Utils.getScaledSize(context, offset);
    }
}
