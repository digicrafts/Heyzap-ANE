package com.heyzap.sdk;

import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;

import android.content.Context;

import com.heyzap.http.AsyncHttpClient;
import com.heyzap.http.AsyncHttpResponseHandler;
import com.heyzap.http.RequestParams;
import com.heyzap.http.SDKCookieStore;

class SDKRestClient {
    private static AsyncHttpClient client;
    private static ThreadPoolExecutor cacheThreadPool;
    private static SDKCookieStore cookieStore;
    private static final String USER_AGENT = "Heyzap Android Client";
    public static final String DOMAIN = "heyzap.com";
    public static final String BASE_URL = "http://" + DOMAIN;
    private static final String BASE_ENDPOINT = "/in_game_api/sdk/";

    private static final int RESPONSE_CACHE = 23;
	public static final int API_STATUS_NOT_LOGGED_IN = 504;

    // Static initialization
    static {
        cacheThreadPool = (ThreadPoolExecutor)Executors.newFixedThreadPool(1);

        client = new AsyncHttpClient();
        client.setThreadPool((ThreadPoolExecutor)Executors.newFixedThreadPool(1));
        
    }

    public static synchronized void init(Context context){
        if(cookieStore == null){
            cookieStore = new SDKCookieStore(context);
            client.setCookieStore(cookieStore);
        }
    }

    // GET requests
    public static void get(Context context, String url, AsyncHttpResponseHandler responseHandler) {
        get(context, url, null, responseHandler);
    }

    public static void get(Context context, String url, RequestParams params) {
        get(context, url, params, null);
    }

    public static void get(final Context context, final String url, final RequestParams params, final AsyncHttpResponseHandler responseHandler) {
        init(context);
        RequestParams augmentedParams = augmentParams(params, context);
        Logger.log("params", augmentedParams);
        client.get(context, getAbsoluteUrl(url), augmentedParams, responseHandler);        
    }
    
    // POST requests
    public static void post(Context context, String url, AsyncHttpResponseHandler responseHandler) {
        post(context, url, null, responseHandler);
    }

    public static void post(Context context, String url, RequestParams params) {
        post(context, url, params, null);
    }

    public static void post(final Context context, final String url, final RequestParams params, final AsyncHttpResponseHandler responseHandler) {
        init(context);
        RequestParams augmentedParams = augmentParams(params, context);
        Logger.log("params", augmentedParams);
        String finalUrl = getAbsoluteUrl(url);
        Logger.log("using url", finalUrl);
        client.post(context, finalUrl, augmentedParams, responseHandler);
    }


    // Cleanup
    public static void cancelRequests(Context context) {
        client.cancelRequests(context, true);
    }

    private static String getAbsoluteUrl(String relativeUrl) {
        if(relativeUrl != null && relativeUrl.startsWith("/")){
            return BASE_URL + relativeUrl;
        }else if(relativeUrl != null && relativeUrl.startsWith("http://")){
            return relativeUrl;
        }else{
            return BASE_URL + BASE_ENDPOINT + relativeUrl;
        }
    }

    public static RequestParams augmentParams(RequestParams params, Context context) {
        if(params == null) {
            params = new RequestParams();
        }
        
        for (Map.Entry<String, String> entry : Utils.extraParams(context).entrySet())
        {
            params.put(entry.getKey(), entry.getValue());
        }

        return params;
    }
}
