package com.heyzap.sdk;

import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.text.Html;
import android.text.Spanned;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.TextView;

import com.heyzap.http.RequestParams;

public class LeaderboardScoreDialogFull extends ClickableToast {
    private Context context;
    private FeedView feedView;
    private String gamePackage;
    private String score;
    private String displayScore;
    private String levelId;
    private boolean fromSdk;
    private View.OnClickListener showInGameOverlayOrLaunchLeaderboardActivity;
    private JSONObject response;
    FrameLayout feedFrame;
    TextView levelNameView;
    private long shownAt;

    public LeaderboardScoreDialogFull(Context context, JSONObject response, final String gamePackage, String score, String displayScore, final String levelId) {
        super(context);
        this.response = response;
        this.gamePackage = gamePackage;
        this.context = context;
        this.score = score;
        this.displayScore = displayScore;
        this.levelId = levelId;
        this.setContentView(Rzap.layout("leaderboard_score_dialog_full"));
        this.setFocusable(true);
        this.setFocusableInTouchMode(true);

        feedFrame = (FrameLayout) findViewById(Rzap.id("feed_frame"));
        levelNameView = (TextView) this.findViewById(Rzap.id("level_name"));
        
        TextView titleView = (TextView) this.findViewById(Rzap.id("title"));

        String prompt = String.format("Personal best: <font color='#a0d63d'>%s</font>!", displayScore);
        Spanned html = Html.fromHtml(prompt);

        titleView.setText(Html.fromHtml(prompt));
        
        if (html.length() >= 20) {
            titleView.setPadding(titleView.getPaddingLeft(), Utils.getScaledSize(context, 4), titleView.getPaddingRight(), titleView.getPaddingBottom());
            titleView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 15);
        }
        
        final Button viewFullButton = (Button) this.findViewById(Rzap.id("view_full_button"));

        // showInGameOverlayOrLaunchLeaderboardActivity = new View.OnClickListener() {
        //
        // @Override
        // public void onClick(View v) {
        // HeyzapLib.launchLeaderboardActivityOrShowInGameOverlay(LeaderboardScoreDialogFull.this.context, levelId, gamePackage, null);
        //
        // if(v == viewFullButton){
        // HeyzapAnalytics.trackEvent(getContext(), "score-overlay-clicked-full-view-full");
        // }else{
        // HeyzapAnalytics.trackEvent(getContext(), "score-overlay-clicked-full-save");
        // }
        // }
        // };
        
        final FrameLayout closeButton = (FrameLayout) findViewById(Rzap.id("close_wrapper"));
        closeButton.setOnClickListener(new OnClickListener() {
            
            @Override
            public void onClick(View arg0) {
                LeaderboardScoreDialogFull.this.hide();
            }
        });

        showInGameOverlayOrLaunchLeaderboardActivity = new View.OnClickListener() {

            @Override
            public void onClick(View v) {

                String displayName = null;
                try {
                    displayName = (String) v.getTag();
                } catch (Exception e) {
                }

                
                HeyzapLib.launchLeaderboardActivityOrShowInGameOverlay(LeaderboardScoreDialogFull.this.context, levelId, gamePackage, displayName, "full");

                if (v == viewFullButton) {
                    HeyzapAnalytics.trackEvent(getContext(), "score-overlay-clicked-full-view-full");
                } else {
                    HeyzapAnalytics.trackEvent(getContext(), "score-overlay-clicked-full-save");
                }
            }
        };

        if (response == null) {
            postScore();
        } else {
            populateUserFeedlettes(response);
        }

        viewFullButton.setOnClickListener(showInGameOverlayOrLaunchLeaderboardActivity);

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

    public void postScore() {
        // we need to submit the score and get the leaderboard

        HeyzapAnalytics.trackEvent(getContext(), "score-post-started");
        levelNameView.setText("Loading...");

        findViewById(Rzap.id("spinner")).setVisibility(View.VISIBLE);
        findViewById(Rzap.id("feed_frame")).setVisibility(View.GONE);
        findViewById(Rzap.id("view_full_button")).setVisibility(View.GONE);
        findViewById(Rzap.id("feed_empty")).setVisibility(View.GONE);

        RequestParams requestParams = LeaderboardScoreLauncher.getNewScoreRequestParams(score, displayScore, levelId);

        SDKRestClient.post(context, "/in_game_api/leaderboard/new_score", requestParams, new SDKResponseHandler() {

            @Override
            public void onSuccess(JSONObject response) {

                HeyzapAnalytics.trackEvent(getContext(), "score-post-success");
                String bestScore;
                try {
                    bestScore = response.getString("best_score");
                    String bestDisplayScore = response.getString("best_display_score");
                    JSONObject level = response.getJSONObject("level");
                    String levelId = level.getString("id");
                    boolean lowestScoreFirst = level.getBoolean("lowest_score_first");
                    LeaderboardScoreLauncher.saveLeaderboardInfoOnPhone(LeaderboardScoreDialogFull.this.context, Float.parseFloat(bestScore), bestDisplayScore,
                            levelId, lowestScoreFirst, true);

                } catch (org.json.JSONException e) {
                    e.printStackTrace();
                }

                findViewById(Rzap.id("feed_frame")).setVisibility(View.VISIBLE);
                findViewById(Rzap.id("view_full_button")).setVisibility(View.VISIBLE);
                findViewById(Rzap.id("feed_empty")).setVisibility(View.GONE);
                findViewById(Rzap.id("spinner")).setVisibility(View.GONE);
                populateUserFeedlettes(response);
            }

            @Override
            public void onFailure(Throwable e) {
                HeyzapAnalytics.trackEvent(getContext(), "score-post-failure");
                findViewById(Rzap.id("feed_frame")).setVisibility(View.GONE);
                findViewById(Rzap.id("view_full_button")).setVisibility(View.GONE);
                findViewById(Rzap.id("feed_empty")).setVisibility(View.VISIBLE);
                findViewById(Rzap.id("spinner")).setVisibility(View.GONE);
                findViewById(Rzap.id("retry")).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        postScore();
                    }
                });
            }
        });

    }

    public void populateUserFeedlettes(JSONObject response) {
        feedView = new FeedView(context);
        final List<Feedlette> fs = new ArrayList<Feedlette>();
        int youIndex = 4;

        if (response.has("stream")) {
            JSONArray stream;
            try {
                stream = response.getJSONArray("stream");
                for (int i = 0; i < stream.length(); i++) {
                    JSONObject obj = stream.getJSONObject(i);
                    LeaderboardUserFeedlette f = new LeaderboardUserFeedlette(obj);

                    if (obj.optBoolean("active", false)) {
                        youIndex = i;
                    }

                    f.setFromSdk(true);
                    f.setFeedletteClickListener(new View.OnClickListener() {

                        @Override
                        public void onClick(View v) {
                            LeaderboardScoreDialogFull.this.hide();
                        }
                    });
                    f.setSaveButtonListener(showInGameOverlayOrLaunchLeaderboardActivity);

                    fs.add(f);
                }
                feedView.connectFastAdapter(fs);
            } catch (org.json.JSONException e) {
                e.printStackTrace();
            }
        }

        feedView.setSelectionFromTop(youIndex, Utils.getScaledSize(context, 40));
        feedFrame.addView(feedView);

        try {
            JSONObject level = response.getJSONObject("level");
            levelNameView.setText(level.getString("name"));
        } catch (JSONException e) {
            e.printStackTrace();
        }

    }

    public void setFromSdk(boolean fromSdk) {
        this.fromSdk = fromSdk;
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
        HeyzapAnalytics.trackEvent(getContext(), "score-overlay-shown-full");
        shownAt = System.currentTimeMillis();
        
        super.show();
    }
    
    @Override
    public void hide() {
        super.hide();
        HeyzapLib.sendPBNotification(context, displayScore);
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
