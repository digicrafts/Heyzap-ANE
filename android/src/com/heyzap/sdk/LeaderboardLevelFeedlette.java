package com.heyzap.sdk;

import java.text.DecimalFormat;

import org.json.JSONObject;

import android.content.Context;
import android.text.Html;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

public class LeaderboardLevelFeedlette extends Feedlette {
    public static LeaderboardLevelFeedlette selectedFeedlette = null;
    public static View selectedIcon = null;
    String levelName;
    String everyoneRank;
    String friendsRank;
    int everyoneCount;
    int friendsCount;

    String displayScore;
    String levelId;
    boolean selected = false;

    LeaderboardLevelSelectListener levelSelectListener;
    private String everyoneCountText;

    public interface LeaderboardLevelSelectListener {
        public void onSelect(String levelId, String levelName, String everyoneRank, String friendsRank, String displayScore);
    }

    public LeaderboardLevelFeedlette(JSONObject jobj) {
        super(Rzap.layout("leaderboard_level_feedlette"));

        this.levelName = jobj.optString("name", "");
        this.everyoneRank = jobj.optString("everyone_rank", "");
        this.everyoneCount = jobj.optInt("everyone_count", 0);
        DecimalFormat formatter = new DecimalFormat("#,###");
        this.everyoneCountText = formatter.format((double) everyoneCount);

        this.friendsRank = jobj.optString("friends_rank", "");
        this.friendsCount = jobj.optInt("friends_count", 0);

        this.displayScore = jobj.optString("display_score", "");
        this.levelId = jobj.optString("id", "");
    }

    public View render(View convertView, Context context, FeedView webFeedView) {
        final ViewHolder holder;
        if (convertView == null) {
            convertView = getInflater(context).inflate(layout, null);

            holder = new ViewHolder();

            holder.wrapper = (FrameLayout) convertView.findViewById(Rzap.id("wrapper"));
            holder.container = (RelativeLayout) convertView.findViewById(Rzap.id("container"));
            holder.levelName = (TextView) convertView.findViewById(Rzap.id("level_name"));
            holder.scoreText = (TextView) convertView.findViewById(Rzap.id("score_text"));
            holder.selectedIcon = (ImageView) convertView.findViewById(Rzap.id("icon"));

            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        holder.levelName.setText(levelName);

        if (selected) {
            holder.selectedIcon.setVisibility(View.VISIBLE);
            selectedIcon = holder.selectedIcon;
        } else {
            holder.selectedIcon.setVisibility(View.GONE);
        }

        if (this.displayScore != null && !this.displayScore.equals("")) {
            if (friendsCount > 1) {
                holder.scoreText
                        .setText(Html
                                .fromHtml("<font color='#55a406'>" + displayScore + "</font>" + "<font color='#808080'>" + " - #" + friendsRank + " of " + friendsCount + " friends" + "</font>"));
            } else {
                holder.scoreText.setText(Html.fromHtml("<font color='#55a406'>" + displayScore + "</font>"));
            }
        } else if (everyoneCount > 0) {
            String plural = everyoneCount == 1 ? "player" : "players";
            holder.scoreText.setText(Html.fromHtml("<font color='#808080'>" + everyoneCountText + " " + plural + "</font>"));
        } else {
            holder.scoreText.setText(Html.fromHtml("<font color='#808080'>No scores</font>"));
        }

        convertView.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                if (selectedIcon != null) {
                    selectedIcon.setVisibility(View.GONE);
                }
                selectedIcon = (ImageView) v.findViewById(Rzap.id("icon"));
                selectedIcon.setVisibility(View.VISIBLE);

                if (selectedFeedlette != null) {
                    selectedFeedlette.selected = false;
                }
                selectedFeedlette = LeaderboardLevelFeedlette.this;
                selectedFeedlette.selected = true;

                v.postDelayed(new Runnable() {

                    @Override
                    public void run() {
                        LeaderboardLevelFeedlette.this.onLevelSelect();
                    }
                }, 150);
            }
        });

        return convertView;
    }

    protected void onLevelSelect() {
        if (levelSelectListener != null) {
            levelSelectListener.onSelect(levelId, (levelName + " Leaderboard"), everyoneRank, friendsRank, displayScore);
        }
    }

    static class ViewHolder {
        FrameLayout wrapper;
        RelativeLayout container;
        TextView levelName;
        TextView scoreText;
        ImageView selectedIcon;
    }

    public LeaderboardLevelSelectListener getLevelSelectListener() {
        return levelSelectListener;
    }

    public void setLevelSelectListener(LeaderboardLevelSelectListener levelSelectListener) {
        this.levelSelectListener = levelSelectListener;
    }

    public boolean isSelected() {
        return selected;
    }

    public void setSelected(boolean selected) {
        this.selected = selected;
        if (selected) {
            selectedFeedlette = this;
        }
    }

}
