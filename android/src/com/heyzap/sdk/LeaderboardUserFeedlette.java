package com.heyzap.sdk;

import java.io.InputStream;

import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Typeface;
import android.os.AsyncTask;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.TypedValue;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.TextView.OnEditorActionListener;

public class LeaderboardUserFeedlette extends Feedlette {
    private ViewHolder holder;
    private LeaderboardUserFeedletteType type;
    private OnClickListener saveButtonListener;

    String username = null;
    String displayName;
    String rank;
    Double score;
    String displayScore;
    String buttonCta = null;

    String picture;
    private boolean fromSdk;
    private View.OnClickListener feedletteClickListener = null;
    private boolean useSeveredProxy;
    private String levelId;
    private boolean downloadingUserImage = false;
    private Bitmap downloadedUserImage = null;

    public LeaderboardUserFeedlette(JSONObject jobj) throws JSONException {
        this(jobj, LeaderboardUserFeedletteType.NORMAL, null);
    }

    public LeaderboardUserFeedlette(JSONObject jobj, LeaderboardUserFeedletteType type, String levelId) throws JSONException {
        this(jobj, type, levelId, null);
    }

    public LeaderboardUserFeedlette(JSONObject jobj, LeaderboardUserFeedletteType type, String levelId, OnClickListener saveButtonListener)
            throws JSONException {
        super(Rzap.layout("leaderboard_user_feedlette"));
        this.type = type;
        this.saveButtonListener = saveButtonListener;
        this.levelId = levelId;

        if (jobj != null) {
            username = jobj.optString("username", null);
            displayName = jobj.optString("display_name", "");
            rank = jobj.optString("rank", "");
            score = jobj.optDouble("score", 0.0);
            displayScore = jobj.optString("display_score", "");

            if (jobj.has("button_cta")) {
                buttonCta = jobj.getString("button_cta");
            }

            picture = jobj.optString("picture", "");

            if (jobj.optBoolean("before_active", false)) {
                this.type = LeaderboardUserFeedletteType.BEFORE_ACTIVE;
            }

            if (jobj.optBoolean("active", false)) {
                this.type = LeaderboardUserFeedletteType.ACTIVE;
            }

            if (jobj.optBoolean("after_active", false)) {
                this.type = LeaderboardUserFeedletteType.AFTER_ACTIVE;
            }
        }
    }

    public enum LeaderboardUserFeedletteType {
        NORMAL, BEFORE_ACTIVE, AFTER_ACTIVE, ACTIVE, CHALLENGEABLE, CHALLENGED, TOP_HOVER
    }

    public void setType(LeaderboardUserFeedletteType type) {
        // set visibilities to a common base
        holder.container.setBackgroundDrawable(null);
        holder.separatorTop.setVisibility(View.VISIBLE);
        holder.separatorBottom.setVisibility(View.VISIBLE);
        holder.greenActionButton.setVisibility(View.GONE);
        holder.score.setTypeface(null, Typeface.NORMAL);
        holder.glowTop.setVisibility(View.GONE);
        holder.glowBottom.setVisibility(View.GONE);
        showGreenActionButton(false);
        showLightActionButton(false);
        holder.lightActionButtonIcon.setVisibility(View.GONE);

        switch (type) {

        case NORMAL:
            break;

        case BEFORE_ACTIVE:
            holder.glowTop.setVisibility(View.VISIBLE);
            break;

        case AFTER_ACTIVE:
            holder.glowBottom.setVisibility(View.VISIBLE);
            break;

        case ACTIVE:
            holder.container.setBackgroundResource(Rzap.drawable("leaderboard_highlight"));
            holder.separatorTop.setVisibility(View.GONE);
            holder.separatorBottom.setVisibility(View.GONE);

            showGreenActionButton(true);
            if (buttonCta != null) {
                holder.greenActionButton.setText(buttonCta);
            }
            holder.greenActionButton.setOnClickListener(saveButtonListener);
            holder.score.setTypeface(null, Typeface.BOLD);
            
            break;

        case CHALLENGEABLE:
            showLightActionButton(true);
            holder.lightActionButtonLabel.setText("Challenge");
            holder.lightActionButtonLabel.setTextColor(0xff548ec0);
            break;

        case CHALLENGED:
            showLightActionButton(true);
            holder.lightActionButtonLabel.setText("Challenged");
            holder.lightActionButtonLabel.setTextColor(0xffc0c0c0);
            holder.lightActionButtonIcon.setVisibility(View.VISIBLE);
            holder.lightActionButton.setClickable(false);
            break;

        case TOP_HOVER:
            ((LinearLayout) holder.wrapper.findViewById(Rzap.id("linear_layout"))).setBackgroundColor(0xebeff1f2);
            holder.separatorTop.setVisibility(View.GONE);
            holder.separatorBottom.setVisibility(View.GONE);

            break;
        }
        
        if (type == LeaderboardUserFeedletteType.ACTIVE && (this.username == null || this.username.equals("null"))) {
            holder.editUserName.setVisibility(View.VISIBLE);
            holder.userName.setVisibility(View.GONE);
            holder.score.setVisibility(View.GONE);
        } else {
            holder.editUserName.setVisibility(View.GONE);
            holder.userName.setVisibility(View.VISIBLE);
            holder.score.setVisibility(View.VISIBLE);
        }
        
        this.type = type;
    }

    public View render(View convertView, Context context, FeedView webFeedView) {
        this.context = context;
        if (convertView == null) {
            convertView = getInflater(context).inflate(layout, null);

            holder = new ViewHolder();

            holder.wrapper = (FrameLayout) convertView.findViewById(Rzap.id("wrapper"));
            holder.linearLayout = (LinearLayout) convertView.findViewById(Rzap.id("linear_layout"));
            holder.container = (RelativeLayout) convertView.findViewById(Rzap.id("container"));
            holder.glowTop = (View) convertView.findViewById(Rzap.id("glow_top"));
            holder.glowBottom = (View) convertView.findViewById(Rzap.id("glow_bottom"));
            holder.separatorTop = (View) convertView.findViewById(Rzap.id("separator_top"));
            holder.separatorBottom = (View) convertView.findViewById(Rzap.id("separator_bottom"));
            holder.userThumb = (ImageView) convertView.findViewById(Rzap.id("user_thumb"));
            holder.userName = (TextView) convertView.findViewById(Rzap.id("user_name"));
            holder.editUserName = (EditText) convertView.findViewById(Rzap.id("edit_user_name"));
            holder.score = (TextView) convertView.findViewById(Rzap.id("score"));
            holder.rank = (TextView) convertView.findViewById(Rzap.id("rank"));
            holder.buttonWrapper = (LinearLayout) convertView.findViewById(Rzap.id("button_wrapper"));
            holder.greenActionButton = (Button) convertView.findViewById(Rzap.id("green_action_button"));
            holder.lightActionButton = (LinearLayout) convertView.findViewById(Rzap.id("light_action_button"));
            holder.lightActionButtonWrapper = (FrameLayout) convertView.findViewById(Rzap.id("light_action_wrapper"));
            holder.lightActionButtonLabel = (TextView) holder.lightActionButton.findViewById(Rzap.id("label"));
            holder.lightActionButtonIcon = (ImageView) holder.lightActionButton.findViewById(Rzap.id("icon"));

            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        holder.feedlette = this;
        holder.userThumb.setBackgroundResource(Rzap.drawable("icon_default_people"));

        if (picture != null && !picture.equals("") && !picture.equals("null")) {
            if (!downloadingUserImage) {
                downloadingUserImage = true;
                new DownloadImageTask(new DownloadImageListener() {

                    @Override
                    public void onImageDownloaded(Bitmap bitmap) {
                        downloadedUserImage = bitmap;
                        if (holder.feedlette == LeaderboardUserFeedlette.this) {
                            holder.userThumb.setImageBitmap(downloadedUserImage);
                        }
                    }
                }).execute(picture);
            }
            if (downloadedUserImage != null) {
                holder.userThumb.setImageBitmap(downloadedUserImage);
            } else {
                holder.userThumb.setImageDrawable(null);
            }
        } else {
            holder.userThumb.setImageDrawable(null);
        }

        holder.userName.setText(displayName);
        holder.score.setText(displayScore);
        holder.rank.setText(rank);
        
        holder.editUserName.addTextChangedListener(new TextWatcher() {
            
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }
            
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }
            
            @Override
            public void afterTextChanged(Editable s) {
                holder.greenActionButton.setTag(s.toString());
            }
        });

        holder.buttonWrapper.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
            }
        });

        setType(this.type);
        convertView.setOnClickListener(null);
        return convertView;
    }

    public void showGreenActionButton(boolean show) {
        holder.greenActionButton.setVisibility(show ? View.VISIBLE : View.GONE);
        if (show) {
            Utils.clickWrap(context, holder.buttonWrapper, holder.greenActionButton, 500);
            holder.greenActionButton.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View arg0) {
                    Logger.log("Immad", "share");
                }
            });
        }
    }

    public void showLightActionButton(boolean show) {
        holder.lightActionButtonWrapper.setVisibility(show ? View.VISIBLE : View.GONE);
        if (show) {
            holder.lightActionButton.setOnClickListener(new OnClickListener() {

                @Override
                public void onClick(View v) {
                    LeaderboardUserFeedlette.this.setType(LeaderboardUserFeedletteType.CHALLENGED);
                }
            });
            Utils.clickWrap(context, holder.buttonWrapper, holder.lightActionButton, 500);
        }
    }

    static class ViewHolder {
        FrameLayout wrapper;
        LinearLayout linearLayout;
        RelativeLayout container;
        View glowTop;
        View glowBottom;
        View separatorTop;
        View separatorBottom;
        ImageView userThumb;
        TextView userName;
        EditText editUserName;
        TextView score;
        TextView rank;
        LinearLayout buttonWrapper;
        Button greenActionButton;
        LinearLayout lightActionButton;
        FrameLayout lightActionButtonWrapper;
        TextView lightActionButtonLabel;
        ImageView lightActionButtonIcon;
        LeaderboardUserFeedlette feedlette;
    }

    public OnClickListener getSaveButtonListener() {
        return saveButtonListener;
    }

    public void setSaveButtonListener(OnClickListener saveButtonListener) {
        this.saveButtonListener = saveButtonListener;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public String getRank() {
        return rank;
    }

    public void setRank(String rank) {
        this.rank = rank;
    }

    public Double getScore() {
        return score;
    }

    public void setScore(Double score) {
        this.score = score;
    }

    public String getDisplayScore() {
        return displayScore;
    }

    public void seDisplaytScore(String displayScore) {
        this.displayScore = displayScore;
    }

    public String getPicture() {
        return picture;
    }

    public void setPicture(String picture) {
        this.picture = picture;
    }

    public LeaderboardUserFeedletteType getType() {
        return type;
    }

    public void setFromSdk(boolean fromSdk) {
        this.fromSdk = fromSdk;
    }

    public void setFeedletteClickListener(View.OnClickListener feedletteClickListener) {
        this.feedletteClickListener = feedletteClickListener;
    }

    public void setUseSeveredProxy(boolean b) {
        this.useSeveredProxy = b;
    }

    // for setting ImageView to a URL. From http://developer.aiwgame.com/imageview-show-image-from-url-on-android-4-0.html.
    private class DownloadImageTask extends AsyncTask<String, Void, Bitmap> {
        DownloadImageListener listener;

        public DownloadImageTask(DownloadImageListener listener) {
            this.listener = listener;
        }

        protected Bitmap doInBackground(String... urls) {
            String urldisplay = urls[0];
            Bitmap mIcon11 = null;
            try {
                InputStream in = new java.net.URL(urldisplay).openStream();
                mIcon11 = BitmapFactory.decodeStream(in);
            } catch (Exception e) {
                Log.e("Error: " + urldisplay, e.getMessage());
                e.printStackTrace();
            }
            return mIcon11;
        }

        protected void onPostExecute(Bitmap result) {
            if (listener != null) {
                listener.onImageDownloaded(result);
            }
        }
    }

    private interface DownloadImageListener {
        public void onImageDownloaded(Bitmap bitmap);
    }

}