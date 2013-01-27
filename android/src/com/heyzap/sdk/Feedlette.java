package com.heyzap.sdk;

import java.util.HashMap;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;

public class Feedlette implements Comparable<Feedlette> {
    protected static LayoutInflater inflater;

    protected int layout;
    protected Bundle extras;
    protected OnClickListener onClickListener;
    protected Class clickIntentClass;
    protected Context context;
    public String displayName = "";
    private boolean isFirst;
    
    

    public Feedlette() {
        init();
    }
    
    public Feedlette(int layout) {
        this.layout = layout;
        init();
    }

    public void setContext(Context context){
//    	if (!(context instanceof HeyzapActivity)) {
//    		throw new UnsupportedOperationException("Please only give feedlettes HeyzapActivity for a context, kthxbai");
//    	}
        this.context = context;
    }
    
    public Context getContext(){
        return this.context;
    }
    
    private void init() {
        this.extras = new Bundle();
    }

    public Bundle getExtras() {
        return extras;
    }
    
    public void setClickIntentClass(Class clickIntentClass){
        this.clickIntentClass = clickIntentClass;
    }
    
    public Class getClickIntentClass() {
        return this.clickIntentClass;
    }
    
    public void setLayout(int layout){
        this.layout = layout;
    }

    public void setOnClickListener(OnClickListener l) {
        this.onClickListener = l;
    }

    public OnClickListener getOnClickListener() {
        return this.onClickListener;
    }

    protected LayoutInflater getInflater(Context context) {
        return (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

    }

    public View render(View convertView, Context context, FeedView webFeedView) {
        this.context = context;
        if(convertView == null) {
            convertView = getInflater(context).inflate(layout, null);
        }

        return convertView;
    }
    
    public void setFirst(boolean isFirst){
        this.isFirst = isFirst;
    }
    
    public boolean isFirst(){
        return this.isFirst;
    }

    public int compareTo(Feedlette another) {
        return 0;
    }

    public HashMap<String, String> clickAnalyticsParams() {
        HashMap<String, String> params = new HashMap<String, String>();
//        params.put("feedletteClass", Utils.getClassName(this));
        return params;
    }
}