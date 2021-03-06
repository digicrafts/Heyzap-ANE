package com.heyzap.sdk;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.concurrent.Executors;
import java.util.concurrent.ExecutorService;

import org.json.JSONObject;

import com.heyzap.http.JsonHttpResponseHandler;
import com.heyzap.http.RequestParams;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.net.Uri;
import android.os.Build;
import android.provider.Settings;
import android.util.Log;

class HeyzapAnalytics {
    public static final String LOG_TAG = "HeyzapSDK";
    private static final String HEYZAP_ANALYTICS_ID_PREF = "heyzap_button_analytics_id";
    private static final String HEYZAP_ENDPOINT = "http://android.heyzap.com/mobile/track_sdk_event";
	static final String HEYZAP_SDK_PLATFORM = "android";
	static final String HEYZAP_SDK_VERSION = "3.4.17";

    private static boolean loaded = false;
    private static String trackHash = "";


    public static synchronized void trackEvent(final Context context, final String eventType) {
        Log.d(LOG_TAG, "Tracking " + eventType + " event.");

        // Load the device id and any previous tracking hash
        if(!loaded) {
            init(context);
            loaded = true;
        }

        RequestParams params = new RequestParams();
        params.put("track_hash", trackHash);
        params.put("type", eventType);
        SDKRestClient.get(context, "/mobile/track_sdk_event", params);
    }

    public static String getAnalyticsReferrer(Context context) {
    	return getAnalyticsReferrer(context, null);
    }
    
    
    public static String getAnalyticsReferrer(Context context, String additionalParams) {
        String referrerTrackHash = getTrackHash(context);
        String referrer;
        if(referrerTrackHash != null) {
        	referrer = "utm_medium=device&utm_source=heyzap_track&utm_campaign=" + referrerTrackHash;
        } else {
            referrer = "utm_medium=device&utm_source=sdk&utm_campaign=" + context.getPackageName();
        }
        
        if (additionalParams != null ) referrer += "&" + additionalParams;
        
        return URLEncoder.encode(referrer);
    }

    private static void init(final Context context) {
        Utils.load(context);
        new Thread(new Runnable(){
            @Override
            public void run(){
                // Load up previous tracking hash
                String tempTrackHash = getTrackHash(context);
                if(tempTrackHash != null) {
                    trackHash = tempTrackHash;
                }
            }
        }).start();
    }

    private static void setTrackHash(Context context, String newTrackHash) {
        if(newTrackHash != null && !newTrackHash.trim().equals("") && !trackHash.equals(newTrackHash)) {
            trackHash = newTrackHash;

            SharedPreferences prefs = context.getSharedPreferences(HEYZAP_ANALYTICS_ID_PREF, Context.MODE_PRIVATE);
            Editor editor = prefs.edit();
            editor.putString(HEYZAP_ANALYTICS_ID_PREF, trackHash);
            editor.commit();
        }
    }

    private static String getTrackHash(Context context) {
        if(trackHash == null){
            final SharedPreferences prefs = context.getSharedPreferences(HEYZAP_ANALYTICS_ID_PREF, Context.MODE_PRIVATE);
            if(prefs != null) {
                trackHash = prefs.getString(HEYZAP_ANALYTICS_ID_PREF, null);
            }
        }
        
        if (trackHash != null && trackHash.trim().equals("")) return null;

        return trackHash;
    }

    private static String convertStreamToString(InputStream is) {
        BufferedReader reader = new BufferedReader(new InputStreamReader(is));
        StringBuilder sb = new StringBuilder();
        String line = null;
        try {
            while ((line = reader.readLine()) != null) {
                sb.append(line + "\n");
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                is.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return sb.toString();
    }
}
