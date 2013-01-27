package com.heyzap.sdk;

import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import com.heyzap.http.RequestParams;
import com.heyzap.http.SDKCookieStore;
import com.heyzap.sdk.LeaderboardLevelFeedlette.LeaderboardLevelSelectListener;

public class LeaderboardDialog extends ClickableToast {
    private Context context;
    private FeedView feedView;
    private FeedView levelsFeedView;
    private LeaderboardLevelsDialog levelsDialog;
    private String gamePackage;
    private String displayScore;
    private View.OnClickListener showInGameOverlayOrLaunchLeaderboardActivity;
    FrameLayout feedFrame;
    TextView levelNameView;
    private long shownAt;
    private String levelId = "";
    
    public LeaderboardDialog(final Context context, final String gamePackage, final String levelId) {
        super(context);
        this.gamePackage = gamePackage;
        this.context = context;
        this.setContentView(Rzap.layout("leaderboard_score_dialog_full"));
        this.setFocusable(true);
        this.setFocusableInTouchMode(true);
        if (levelId == null) {
            this.levelId = "";
        } else {
            this.levelId = levelId;
        }

        feedFrame = (FrameLayout) findViewById(Rzap.id("feed_frame"));
        levelNameView = (TextView) this.findViewById(Rzap.id("level_name"));

        PackageManager pm = context.getPackageManager();
        ApplicationInfo info = null;
        try {
            info = pm.getApplicationInfo(gamePackage, 0);
        } catch (NameNotFoundException e) {
            e.printStackTrace();
        }

        ((TextView) this.findViewById(Rzap.id("title"))).setText(info != null ? pm.getApplicationLabel(info) : "Leaderboards");

        final FrameLayout closeButton = (FrameLayout) findViewById(Rzap.id("close_wrapper"));
        closeButton.setOnClickListener(new OnClickListener() {
            
            @Override
            public void onClick(View arg0) {
                LeaderboardDialog.this.hide();
            }
        });

        Button viewFullButton = (Button) this.findViewById(Rzap.id("view_full_button"));

        showInGameOverlayOrLaunchLeaderboardActivity = new View.OnClickListener() {
            
            @Override
            public void onClick(View v) {

                String displayName = null;
                try {
                    displayName = (String) v.getTag();
                } catch (Exception e) {
                }
                HeyzapLib.launchLeaderboardActivityOrShowInGameOverlay(LeaderboardDialog.this.context, LeaderboardDialog.this.levelId, gamePackage, displayName, "manual");

            }
        };

        levelsFeedView = new FeedView(context);
        levelsFeedView.setCacheColorHint(0);
        levelsDialog = new LeaderboardLevelsDialog(context);
        levelsDialog.setFeedView(levelsFeedView);

        requestLeaderboard(this.levelId);
        requestLevels();

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

        final FrameLayout showLevelsWrapper = (FrameLayout) this.findViewById(Rzap.id("show_levels_wrapper"));
        showLevelsWrapper.setVisibility(View.VISIBLE);
        final ImageView showLevels = (ImageView) this.findViewById(Rzap.id("show_levels"));
        showLevels.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                levelsDialog.show();
            }
        });
        showLevelsWrapper.post(new Runnable() {

            @Override
            public void run() {
                Utils.clickWrap(context, showLevelsWrapper, showLevels, 500);
            }
        });
    }

    public void requestLeaderboard(final String levelId) {
        Logger.log("bbb request level", levelId);
        this.levelId = levelId;
        levelNameView.setText("Loading...");

        findViewById(Rzap.id("spinner")).setVisibility(View.VISIBLE);
        findViewById(Rzap.id("feed_frame")).setVisibility(View.GONE);
        findViewById(Rzap.id("view_full_button")).setVisibility(View.GONE);
        findViewById(Rzap.id("feed_empty")).setVisibility(View.GONE);

        RequestParams requestParams = new RequestParams();
        requestParams.put("for_game_package", gamePackage);
        requestParams.put("from_sdk", "true");
        requestParams.put("limit", "100");
        if (!levelId.equals("")) {
            requestParams.put("level", levelId);
        }

        SDKRestClient.get(context, "/in_game_api/leaderboard/everyone", requestParams, new SDKResponseHandler() {

            @Override
            public void onSuccess(JSONObject response) {
                findViewById(Rzap.id("feed_frame")).setVisibility(View.VISIBLE);
                findViewById(Rzap.id("view_full_button")).setVisibility(View.VISIBLE);
                findViewById(Rzap.id("feed_empty")).setVisibility(View.GONE);
                findViewById(Rzap.id("spinner")).setVisibility(View.GONE);
                populateUserFeedlettes(response);
            }

            @Override
            public void onFailure(Throwable e) {

                levelNameView.setText("");
                ((TextView) findViewById(Rzap.id("empty_text"))).setText("Could not load scores.");
                ((Button) findViewById(Rzap.id("retry"))).setText("Try again");

                findViewById(Rzap.id("feed_frame")).setVisibility(View.GONE);
                findViewById(Rzap.id("view_full_button")).setVisibility(View.GONE);
                findViewById(Rzap.id("feed_empty")).setVisibility(View.VISIBLE);
                findViewById(Rzap.id("spinner")).setVisibility(View.GONE);
                findViewById(Rzap.id("retry")).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        requestLeaderboard(levelId);
                    }
                });
            }
        });

    }

    public void requestLevels() {
        final View feedFrameWrapper = levelsDialog.findViewById(Rzap.id("feed_frame_wrapper"));
        feedFrameWrapper.findViewById(Rzap.id("levels_spinner")).setVisibility(View.VISIBLE);
        feedFrameWrapper.findViewById(Rzap.id("levels_feed_frame")).setVisibility(View.GONE);
        feedFrameWrapper.findViewById(Rzap.id("levels_feed_empty")).setVisibility(View.GONE);

        RequestParams requestParams = new RequestParams();
        requestParams.put("for_game_package", gamePackage);
        requestParams.put("from_sdk", "true");
        requestParams.put("limit", "1000");

        SDKRestClient.get(context, "/in_game_api/leaderboard/levels", requestParams, new SDKResponseHandler() {

            @Override
            public void onSuccess(JSONObject response) {
                feedFrameWrapper.findViewById(Rzap.id("levels_spinner")).setVisibility(View.GONE);
                feedFrameWrapper.findViewById(Rzap.id("levels_feed_frame")).setVisibility(View.VISIBLE);
                populateLevelFeedlettes(response);
            }

            @Override
            public void onFailure(Throwable e) {

                feedFrameWrapper.findViewById(Rzap.id("levels_spinner")).setVisibility(View.GONE);
                feedFrameWrapper.findViewById(Rzap.id("levels_feed_frame")).setVisibility(View.VISIBLE);
                feedFrameWrapper.findViewById(Rzap.id("levels_feed_empty")).setVisibility(View.VISIBLE);
                feedFrameWrapper.findViewById(Rzap.id("levels_retry")).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        requestLevels();
                    }
                });

            }
        });

    }

    public void populateLevelFeedlettes(JSONObject response) {

        final List<Feedlette> fs = new ArrayList<Feedlette>();

        if (response.has("leaderboards")) {
            JSONArray stream;
            try {
                stream = response.getJSONArray("leaderboards");
                for (int i = 0; i < stream.length(); i++) {
                    JSONObject obj = stream.getJSONObject(i);
                    LeaderboardLevelFeedlette f = new LeaderboardLevelFeedlette(obj);

                    if (levelId.equals("") && i == 0) {
                        f.setSelected(true);
                    } else if (obj.optString("id", "").equals(levelId)) {
                        f.setSelected(true);
                    }

                    f.setLevelSelectListener(new LeaderboardLevelSelectListener() {

                        @Override
                        public void onSelect(String levelId, String levelName, String everyoneRank, String friendsRank, String displayScore) {
                            LeaderboardDialog.this.onLevelSelect(levelId, levelName);
                            LeaderboardDialog.this.levelsDialog.hide();
                        }
                    });

                    fs.add(f);
                }
                levelsFeedView.connectFastAdapter(fs);
            } catch (org.json.JSONException e) {
                e.printStackTrace();
            }
        }
    }

    protected void onLevelSelect(String levelId, String levelName) {
        requestLeaderboard(levelId);
    }

    public void populateUserFeedlettes(JSONObject response) {
        feedView = new FeedView(context);
        final List<Feedlette> fs = new ArrayList<Feedlette>();

        if (response.has("stream")) {
            JSONArray stream;
            try {
                stream = response.getJSONArray("stream");
                if (stream.length() == 0) {
                    feedFrame.removeAllViews();
                    ((TextView) findViewById(Rzap.id("empty_text"))).setText("There are no scores yet for this level. Go be the first!");
                    ((Button) findViewById(Rzap.id("retry"))).setVisibility(View.GONE);
                    findViewById(Rzap.id("feed_empty")).setVisibility(View.VISIBLE);
                } else {
                    for (int i = 0; i < stream.length(); i++) {
                        JSONObject obj = stream.getJSONObject(i);
                        LeaderboardUserFeedlette f = new LeaderboardUserFeedlette(obj);

                        f.setFromSdk(true);
                        f.setFeedletteClickListener(new View.OnClickListener() {

                            @Override
                            public void onClick(View v) {
                                LeaderboardDialog.this.hide();
                            }
                        });
                        f.setSaveButtonListener(showInGameOverlayOrLaunchLeaderboardActivity);
                        fs.add(f);
                    }
                    feedView.connectFastAdapter(fs);
                    feedFrame.removeAllViews();
                    feedFrame.addView(feedView);
                }
            } catch (org.json.JSONException e) {
                e.printStackTrace();
            }
        }

        try {
            JSONObject level = response.getJSONObject("level");
            levelNameView.setText(level.getString("name"));
        } catch (JSONException e) {
            e.printStackTrace();
        }

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