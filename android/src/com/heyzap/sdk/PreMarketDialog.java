package com.heyzap.sdk;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.util.HashMap;
import java.util.Map;

import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.Toast;

public abstract class PreMarketDialog extends SplashDialog {
    protected static final String LOG_TAG = "HeyzapSDK";
    protected String packageName;
    protected String gameName;

    public PreMarketDialog(final Context context, String packageName) {
        super(context);

        this.packageName = packageName;
        this.gameName = Utils.getAppLabel(context);
    }
    
    protected View buildDialogContentView() {
        LinearLayout dialogContents = new LinearLayout(getContext());
        dialogContents.setOrientation(LinearLayout.VERTICAL);
        dialogContents.setBackgroundColor(0xFFFFFFFF);
        
        try {
            dialogContents.addView(buildBannerView(), new RelativeLayout.LayoutParams(LinearLayout.LayoutParams.FILL_PARENT, (int)(134 * scale)));
            dialogContents.addView(buildInfoView(), new LinearLayout.LayoutParams(LinearLayout.LayoutParams.FILL_PARENT, (int)(82 * scale)));
            dialogContents.addView(buildActionBar(), new LinearLayout.LayoutParams(LinearLayout.LayoutParams.FILL_PARENT, (int)(57 * scale)));           
        }
        catch (Exception e) {
            e.printStackTrace();
            // handle it
        }
        
        return dialogContents;
    }
    
    abstract View buildBannerView();
    abstract View buildInfoView();

    protected View buildActionBar() {
        final float scale = getContext().getResources().getDisplayMetrics().density;
        RelativeLayout actionBar = new RelativeLayout(getContext());
        actionBar.setBackgroundColor(0x00);

        // Footer Image
        ImageView actionBarImageView = new ImageView(getContext());
        Drawables.setImageDrawable(getContext(), actionBarImageView, "dialog_action_bar.png");
        actionBarImageView.setAdjustViewBounds(true);
        actionBarImageView.setOnClickListener(new ActionOnClickListener(){});
        
        RelativeLayout.LayoutParams actionBarImageLayout = new RelativeLayout.LayoutParams(LinearLayout.LayoutParams.FILL_PARENT, LinearLayout.LayoutParams.FILL_PARENT);
        actionBarImageLayout.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM, -1);
        actionBar.addView(actionBarImageView, actionBarImageLayout);
        
        // Skip Area
        RelativeLayout.LayoutParams skipLayoutParams = new RelativeLayout.LayoutParams((int)(55 * scale), (int)(55 * scale));
        skipLayoutParams.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM, -1);
        skipLayoutParams.addRule(RelativeLayout.ALIGN_PARENT_RIGHT, -1);
        LinearLayout skipView = new LinearLayout(getContext());
        skipView.setBackgroundColor(0x00);
        skipView.setOnClickListener(new SkipOnClickListener(){});
        
        actionBar.addView(skipView, skipLayoutParams);
        
        return actionBar;
    }

    protected abstract String getAdditionalAnalyticsParams();
    protected abstract void fireSkipClickedAnalytics();
    protected abstract void fireInstallClickedAnalytics();

    public class SkipOnClickListener implements OnClickListener {
        @Override
        public void onClick(View arg0) {
            fireSkipClickedAnalytics();

            hide();
        }
    }

    public class ActionOnClickListener implements OnClickListener  {
        @Override
        public void onClick(View v) {
            // Check if the market is installed
            if(!Utils.marketInstalled(getContext())) {
                Toast.makeText(getContext(), "Sorry, the android market is not installed on your device.", Toast.LENGTH_LONG).show();
                return;
            }

            // Check if this android version is supported
            if(!Utils.androidVersionSupported()) {
                Toast.makeText(getContext(), "Sorry, your android version is not supported by Heyzap.", Toast.LENGTH_LONG).show();
                return;
            }

            fireInstallClickedAnalytics();

            // Launch the android market and close this dialog
            Utils.installHeyzap(getContext(), getAdditionalAnalyticsParams());
            hide();
        }
    }
    
    public class DrawableManager {
        private final Map<String, Drawable> drawableMap;

        public DrawableManager() {
            drawableMap = new HashMap<String, Drawable>();
        }

        public Drawable fetchDrawable(String urlString) {
            if (drawableMap.containsKey(urlString)) {
                return drawableMap.get(urlString);
            }

            Log.d(this.getClass().getSimpleName(), "image url:" + urlString);
            try {
                InputStream is = fetch(urlString);
                Drawable drawable = Drawable.createFromStream(is, "src");

                if (drawable != null) {
                    drawableMap.put(urlString, drawable);
                    Log.d(this.getClass().getSimpleName(), "got a thumbnail drawable: " + drawable.getBounds() + ", "
                            + drawable.getIntrinsicHeight() + "," + drawable.getIntrinsicWidth() + ", "
                            + drawable.getMinimumHeight() + "," + drawable.getMinimumWidth());
                } else {
                  Log.w(this.getClass().getSimpleName(), "could not get thumbnail");
                }

                return drawable;
            } catch (MalformedURLException e) {
                Log.e(this.getClass().getSimpleName(), "fetchDrawable failed", e);
                return null;
            } catch (IOException e) {
                Log.e(this.getClass().getSimpleName(), "fetchDrawable failed", e);
                return null;
            } catch (Exception e) {
                e.printStackTrace();
                return null;
            }
        }

        public void fetchDrawableOnThread(final String urlString, final ImageView imageView) {
            if (drawableMap.containsKey(urlString)) {
                imageView.setImageDrawable(drawableMap.get(urlString));
            }

            final Handler handler = new Handler() {
                @Override
                public void handleMessage(Message message) {
                    imageView.setImageDrawable((Drawable) message.obj);
                }
            };

            Thread thread = new Thread() {
                @Override
                public void run() {
                    Drawable drawable = fetchDrawable(urlString);
                    if (drawable != null) {
                        Message message = handler.obtainMessage(1, drawable);
                        handler.sendMessage(message);
                    }
                }
            };
            thread.start();
        }

        private InputStream fetch(String urlString) throws MalformedURLException, IOException {
            DefaultHttpClient httpClient = new DefaultHttpClient();
            HttpGet request = new HttpGet(urlString);
            HttpResponse response = httpClient.execute(request);
            return response.getEntity().getContent();
        }

    }
}
