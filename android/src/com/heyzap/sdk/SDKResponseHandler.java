package com.heyzap.sdk;

import java.util.HashMap;
import java.util.Map;

import org.apache.http.client.HttpResponseException;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.Message;
import android.text.format.Time;
import android.widget.Toast;

import com.heyzap.http.RequestParams;

public class SDKResponseHandler extends com.heyzap.http.JsonHttpResponseHandler {
    private static final int RESPONSE_CACHE = 23;
    public static Time serverTimeNow = null;
    
    static {
        serverTimeNow = new Time();
        serverTimeNow.setToNow();
    }
    
    private ProgressDialog loadingDialog;
    private RequestParams params;
    private String url;
    private boolean notifyOnError = true;
    private boolean parseJSON = true;
    private static Toast toast;

    @Override
    public Object parseResponse(String responseBody) throws JSONException{
        if(!parseJSON) return null;
        
        Object response = super.parseResponse(responseBody);
        if(response instanceof JSONObject){
            JSONObject jsonResponse = (JSONObject) response;
            if(jsonResponse.has("display_error_message")){
                this.showErrorMessage(jsonResponse.getString("display_error_message"));
            }
        }
        return response;
    }
    
    @Override
    protected void handleSuccessMessage(String responseBody) {
        Logger.log(responseBody);
        try {
            Object jsonResponse = parseResponse(responseBody);
            if(jsonResponse instanceof JSONObject) {
                onSuccess((JSONObject)jsonResponse);
            } else if(jsonResponse instanceof JSONArray) {
                onSuccess((JSONArray)jsonResponse);
            } else {
                throw new JSONException("Unexpected type " + jsonResponse.getClass().getName());
            }
        } catch(JSONException e) {
            onFailure(e);
        }
    }
    
    @Override
    public void handleMessage(Message message){
        try{
            super.handleMessage(message);
        }catch(Throwable e){
            String paramString = "null";
            if(params != null){
                paramString = params.toString();
            }

            final Map<String,String> metaData = new HashMap<String,String>();
            metaData.put("http_client_url", url);
            metaData.put("http_client_params", paramString);
            
            Logger.log("bbb error in handle message", e);
            e.printStackTrace();

            showErrorMessage("Something went wrong. Please try again later.");
        }
    }

    @Override
    public void sendSuccessMessage(String responseBody) {
        super.sendSuccessMessage(responseBody);
    }

    @Override
    public void onFinish(){
        // Kill any loading dialogs for this request
        if(loadingDialog != null){
            try{
                loadingDialog.dismiss();
            } catch(Throwable e){}
        }
    }

    @Override
    public void onFailure(Throwable e){
        // Show a "no internet connection" toast on errors
        if(notifyOnError){
        	if(e instanceof HttpResponseException) {
        		if (((HttpResponseException) e).getStatusCode() >= 400){
	                showErrorMessage("Heyzap is having a problem. Please try again later");
        		} else {
	                showErrorMessage("No internet connection");
            	}
        	} else {
                showErrorMessage("No internet connection");
        	}
            e.printStackTrace();

        }
    }

    public SDKResponseHandler setLoadingText(Context context, String loadingText){
        try {
            loadingDialog = ProgressDialog.show(context, "", loadingText, true, true);
        } catch(Throwable e) {}

        return this;
    }

    public SDKResponseHandler setNotifyOnError(boolean notifyOnError){
        this.notifyOnError = notifyOnError;

        return this;
    }

    public void setParseJSON(boolean parseJSON){
        this.parseJSON = parseJSON;
    }
    
    public boolean getParseJSON(){
        return this.parseJSON;
    }
    
    public void setExtraData(String url, RequestParams params){
        this.params = params;
        this.url = url;
    }
    
    protected void showErrorMessage(String errorText){
        if(notifyOnError) {
            try {
                if(toast != null) {
                    toast.cancel();
                }
                toast = Toast.makeText(HeyzapLib.getApplicationContext(), errorText, Toast.LENGTH_LONG);
                toast.show();
            } catch(Throwable error){
                error.printStackTrace();
            }
        }
    }

    public boolean suppressSpinner() {return false;}
}
