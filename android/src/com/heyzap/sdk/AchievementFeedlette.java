package com.heyzap.sdk;

import java.io.InputStream;

import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Parcel;
import android.os.Parcelable;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

public class AchievementFeedlette extends Feedlette {
    private Achievement achievement;
    private ViewHolder holder;
    
    private boolean downloadingAchievementImage = false;
    private Bitmap downloadedAchievementImage = null;

    public AchievementFeedlette(JSONObject jobj) {
        super(Rzap.layout("achievement_feedlette"));

        if (jobj != null) {
        	try {
        		this.achievement = new Achievement(jobj);
        	} catch (JSONException e) {
    			e.printStackTrace();
        	}
        }
    }

    public AchievementFeedlette(Achievement achievement) {
        super(Rzap.id("achievement_feedlette"));
        this.achievement = achievement;
    }

    public View render(View convertView, final Context context, FeedView feedView) {
        // Set up the view holder
        if(convertView == null) {
            convertView = super.render(convertView, context, feedView);
            
            holder = new ViewHolder();
            holder.achievementIconView = (ImageView) convertView.findViewById(Rzap.id("achievement_icon"));
            holder.achievementNameView = (TextView) convertView.findViewById(Rzap.id("achievement_name"));
            holder.achievementDescriptionView = (TextView) convertView.findViewById(Rzap.id("achievement_description"));
        	holder.achievementNewBadgeView = (ImageView) convertView.findViewById(Rzap.id("new_badge_icon"));;
            
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }
        
        holder.feedlette = this;
        holder.achievementIconView.setImageResource(Rzap.drawable("icon_default_badge"));

        // Fill in the views
        String iconUrl = achievement.getIconUrl();
        if (iconUrl != null && !iconUrl.equals("") && !iconUrl.equals("null")) {
            if (!downloadingAchievementImage) {
            	downloadingAchievementImage = true;
                new DownloadImageTask(new DownloadImageListener() {

                    @Override
                    public void onImageDownloaded(Bitmap bitmap) {
                    	downloadedAchievementImage = bitmap;
                        if (holder.feedlette == AchievementFeedlette.this) {
                            holder.achievementIconView.setImageBitmap(downloadedAchievementImage);
                        }
                    }
                }).execute(iconUrl);
            }
            if (downloadedAchievementImage != null) {
                holder.achievementIconView.setImageBitmap(downloadedAchievementImage);
            } else {
                holder.achievementIconView.setImageDrawable(null);
            }
        } else {
            holder.achievementIconView.setImageDrawable(null);
        }
        
        if (achievement.hasJustUnlocked()) {
        	holder.achievementNewBadgeView.setVisibility(View.VISIBLE);
        } else {
        	holder.achievementNewBadgeView.setVisibility(View.GONE);
        }
        
        if (achievement.hasUnlocked()) {
            holder.achievementNameView.setTextColor(0xff000000);
        } else {
            holder.achievementNameView.setTextColor(0xff999999);
        }
        
        holder.achievementNameView.setText(achievement.getName());
        holder.achievementDescriptionView.setText(achievement.getDescription());
        
        return convertView;
    }

    @Override
    public String toString() {
        return achievement.getName();
    }

    static class ViewHolder {
        ImageView achievementIconView;
        ImageView achievementNewBadgeView;
        TextView achievementNameView;
        TextView achievementDescriptionView;
        AchievementFeedlette feedlette;
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

class Achievement implements Parcelable {
    private String name;
    private String iconUrl;
    private String description;
    private boolean unlocked = false;
    private boolean justUnlocked = false;

    public Achievement(JSONObject jobj) throws JSONException {
        name = jobj.getString("name");
        
        if(jobj.has("image_url")) {
        	iconUrl = jobj.getString("image_url");
        }

        if(jobj.has("description")) {
        	description = jobj.getString("description");
        }

        if(jobj.has("unlocked")){
            unlocked = jobj.getBoolean("unlocked");
        }
        
        if(jobj.has("just_unlocked")){
        	justUnlocked = jobj.getBoolean("just_unlocked");
        }
    }

    public String getName() {
        return name;
    }

    public String getIconUrl() {
        return iconUrl;
    }

    public String getDescription() {
        return description;
    }

    public boolean hasUnlocked() {
        return unlocked;
    }

    public boolean hasJustUnlocked() {
        return justUnlocked;
    }

    // Parceling part
    public Achievement(Parcel in) {
        readFromParcel(in);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(name);
        dest.writeString(iconUrl);
        dest.writeString(description);
        dest.writeInt(justUnlocked ? 1 : 0);
        dest.writeInt(unlocked ? 1 : 0);
    }

    public void readFromParcel(Parcel in) {
        name = in.readString();
        iconUrl = in.readString();
        description = in.readString();
        justUnlocked = in.readInt() == 1 ? true : false;
        unlocked = in.readInt() == 1 ? true : false;
    }

    public static final Parcelable.Creator<Achievement> CREATOR = new Parcelable.Creator<Achievement>() {
        public Achievement createFromParcel(Parcel in) {
            return new Achievement(in);
        }

        public Achievement[] newArray(int size) {
            return new Achievement[size];
        }
    };
}