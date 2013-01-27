package com.heyzap.sdk;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;

public class FeedAdapter extends ArrayAdapter<Feedlette> {
    private HashMap<Object, Integer> seen;
    private FeedView feedView;

    public FeedAdapter(Context context) {
        this(context, new ArrayList<Feedlette>());
    }

    public FeedAdapter(Context context, List<Feedlette> items) {
        super(context, 0, items);

        this.seen = new HashMap<Object, Integer>();

        for(Feedlette f : items){
            trackItemClass(f);
        }
    }
    
    public void setFeedView(FeedView feedView){
        this.feedView = feedView;
    }

    private void trackItemClass(Feedlette f) {
        if(!seen.containsKey(f.getClass())) {
            seen.put(f.getClass(), seen.size());
        }
    }
    
    @Override
    public void add(Feedlette f){
        trackItemClass(f);
        super.add(f);
    }
    
    @Override
    public void insert(Feedlette f, int idx){
        trackItemClass(f);
        super.insert(f,idx);
    }

    @Override
    public int getViewTypeCount(){
        return Math.max(10, seen.size());
    }

    @Override
    public int getItemViewType(int position){
        return seen.get(getItem(position).getClass());
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        onNewPosition(position);
        return super.getItem(position).render(convertView, this.getContext(), this.feedView);
    }
    
    protected void onNewPosition(int position){}

    public List<Feedlette> getItems(){
        List<Feedlette> fl = new ArrayList<Feedlette>();
        for(int i = 0; i < super.getCount(); ++i)
        	fl.add(super.getItem(i));
        return fl;
    }
}
