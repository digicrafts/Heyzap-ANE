package com.heyzap.sdk;

import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONObject;

import com.heyzap.http.RequestParams;

import android.app.Activity;
import android.content.Context;
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.TextView;

public class AchievementDialogFull extends ClickableToast {
    private Context context;
    private FeedView feedView;
    private TextView titleView;
    private Button saveButton;
    FrameLayout feedFrame;
    FrameLayout loadingSpinner;
    private long shownAt;

    public AchievementDialogFull(final Context context, final boolean seeAllAchievements, JSONObject response) {
        super(context);
        this.context = context;
        this.setContentView(Rzap.layout("achievement_dialog_full"));
        this.setFocusable(true);
        this.setFocusableInTouchMode(true);

        feedFrame = (FrameLayout) findViewById(Rzap.id("feed_frame"));
        loadingSpinner = (FrameLayout) findViewById(Rzap.id("spinner"));

        saveButton = (Button) this.findViewById(Rzap.id("save_button"));
        titleView = (TextView) this.findViewById(Rzap.id("title"));
        
        if (seeAllAchievements) {
        	setTitle("Achievements");
        }
        
    	feedFrame.setVisibility(View.GONE);
    	loadingSpinner.setVisibility(View.VISIBLE);

    	saveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Launch the android market and close this dialog
        		new AchievementsFullOverlay(context).show();
            	AchievementDialogFull.this.hide();
            }
        });

        this.findViewById(Rzap.id("close_button")).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
            	AchievementDialogFull.this.hide();
            }
        });

        if (response != null) {
        	populateAchievementFeedlettes(response);
        } else {
        	retrieveData(seeAllAchievements);
        }
        
        // Use portrait width, regardless of orientation
        FrameLayout container = (FrameLayout) findViewById(Rzap.id("wrapper"));
        FrameLayout.LayoutParams p = (FrameLayout.LayoutParams) container.getLayoutParams();
        WindowManager wm = (WindowManager) getContext().getSystemService(Context.WINDOW_SERVICE);
        DisplayMetrics metrics = new DisplayMetrics();
        wm.getDefaultDisplay().getMetrics(metrics);
        p.width = Math.min(metrics.widthPixels, metrics.heightPixels);
        p.gravity = Gravity.CENTER;
        container.setLayoutParams(p);
    }
    
    public void setTitle(String value) {
        titleView.setText(value);
    }
    
    private void retrieveData(final boolean seeAllAchievements) {
        ((Activity) context).runOnUiThread(new Runnable() {
            @Override
            public void run() {
                feedFrame.setVisibility(View.GONE);
            	loadingSpinner.setVisibility(View.VISIBLE);
            	
                final RequestParams requestParams = new RequestParams();
                requestParams.put("game_context_package", context.getPackageName());
                
                SDKRestClient.post(context, "/in_game_api/achievements/get_achievements", requestParams, new SDKResponseHandler() {
                    @Override
                    public void onSuccess(JSONObject response) {
                        Logger.log("bbb get achievements success");
                        populateAchievementFeedlettes(response);
                    }

                    @Override
                    public void onFailure(Throwable e) {
                        Logger.log("bbb get achievements failure", e);
                    }
                });
            }
        });
    }

    public void populateAchievementFeedlettes(JSONObject response) {
        feedView = new FeedView(context);
    	loadingSpinner.setVisibility(View.GONE);
    	feedFrame.setVisibility(View.VISIBLE);
        feedFrame.removeAllViews();
    	
        final List<Feedlette> fs = new ArrayList<Feedlette>();

        if (response != null && response.has("achievements")) {
            JSONArray stream;
            try {
                stream = response.getJSONArray("achievements");
                for (int i = 0; i < stream.length(); i++) {
                    JSONObject obj = stream.getJSONObject(i);
                    fs.add(new AchievementFeedlette(obj));
                }
                feedView.connectFastAdapter(fs);
            } catch (org.json.JSONException e) {
                e.printStackTrace();
            }
        }

        feedFrame.addView(feedView);
    }

    @Override
    public WindowManager.LayoutParams getWmParams() {
        WindowManager.LayoutParams params = super.getWmParams();
        params.gravity = Gravity.CENTER;
        params.width = WindowManager.LayoutParams.WRAP_CONTENT;
        params.verticalMargin = 0.0f;
        params.horizontalMargin = 0.0f;
        params.flags &= ~WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN;
        params.flags &= ~WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE;
        params.flags |= WindowManager.LayoutParams.FLAG_WATCH_OUTSIDE_TOUCH;

        return params;
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
