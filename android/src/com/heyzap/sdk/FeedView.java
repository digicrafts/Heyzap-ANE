package com.heyzap.sdk;

import java.util.List;

import android.content.Context;
import android.os.Bundle;
import android.util.AttributeSet;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Filter;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.ListView;

public class FeedView extends ListView {
    protected FeedAdapter feedAdapter;
    private Class<?> clickIntentClass;
    private CharSequence filterConstraint;
    private boolean empty = true;
    private boolean whiteFeedletes;
    private Bundle clickExtra = null;

    private class ClickListener implements OnItemClickListener {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            
        }
    }
    

    public void setOnClickExtras(Bundle clickExtra) {
        this.clickExtra = clickExtra;
    }
    
    public void setWhiteFeedlettes(boolean whiteFeedlettes){
        this.whiteFeedletes = whiteFeedlettes;
    }
    
    public boolean isWhiteFeedlettes(){
        return whiteFeedletes;
    }
    
    public FeedView(Context context) {
        super(context);
        init();
    }

    public FeedView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public FeedView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init();
    }

    public FeedView setClickIntentClass(Class<?> clickIntentClass) {
        this.clickIntentClass = clickIntentClass;
        return this;
    }

    public void connectAdapter() {
        // Set up a feed adapter
        if(feedAdapter == null) {
            feedAdapter = new FeedAdapter(getContext()){
                @Override
                public void onNewPosition(int position){
                    onNewFeedAdapterPosition(position);
                }
            };
            feedAdapter.setFeedView(this);
        }
        setAdapter(feedAdapter);
    }
    
    protected void onNewFeedAdapterPosition(int position){};

    public void connectAdapter(List<Feedlette> items) {
        // Set up a feed adapter
        feedAdapter = new FeedAdapter(getContext(), items);
        setAdapter(feedAdapter);
    }


    public void connectFastAdapter(FastFeedAdapter adapter){
        this.setFastScrollEnabled(true);
        setAdapter(adapter);
    }
    public void connectFastAdapter(){
        if(feedAdapter == null){
            feedAdapter = new FastFeedAdapter(getContext());
        }
        connectFastAdapter((FastFeedAdapter) feedAdapter);
    }
    public void connectFastAdapter(List<Feedlette> items) {
        // Set up a feed adapter
        feedAdapter = new FastFeedAdapter(getContext(), items);
        connectFastAdapter((FastFeedAdapter) feedAdapter);
    }

    public void insert(Feedlette f, int index){
        empty = false;
        feedAdapter.insert(f, index);
    }

    public void add(Feedlette f) {
        empty = false;
        if(feedAdapter != null){
        	feedAdapter.add(f);
        }
        //setAdapter(feedAdapter);
    }

    public void remove(Feedlette f){
        if(feedAdapter != null){
        	feedAdapter.remove(f);
        }
    }
    
    public Feedlette lastFeedlette() {
        int count = feedAdapter.getCount();
        if(count == 0) {
            return null;
        } else {
            return feedAdapter.getItem(feedAdapter.getCount() - 1);
        }
    }
    
    // useful for feedviews with a semi-transparent item "hovering" at the top, e.g. http://cl.ly/image/2d2u0w1P1a3N
    public void addHeaderPadding(int dp) {
        LinearLayout wrapper = new LinearLayout(this.getContext());
        FrameLayout padding = new FrameLayout(this.getContext());
//        padding.setText("asdf asdf asdf asdf asdf");
        padding.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.FILL_PARENT, Utils.getScaledSize(this.getContext(), dp)));
        wrapper.addView(padding);
        addHeaderView(wrapper);
    }

    public boolean isEmpty(){
        return empty;
    }
    public int size(){
        return empty ? 0 : feedAdapter.getCount();
    }


    public void clear() {
        
        empty = true;
        if(feedAdapter != null){
            feedAdapter = null;
            this.connectAdapter();
            feedAdapter.clear();
        }
    }

    public void filterBy(CharSequence constraint) {
    	// Save the constraint for later refreshes
    	filterConstraint = constraint;
    	feedAdapter.getFilter().filter(filterConstraint);
    }
    
    public void filterBy(CharSequence constraint, Filter.FilterListener onComplete) {
    	// Save the constraint for later refreshes
    	filterConstraint = constraint;
    	feedAdapter.getFilter().filter(filterConstraint, onComplete);
    }
    
    public void removeIndex(int i) {
        feedAdapter.remove(feedAdapter.getItem(i));
    }

    public void refresh() {
        if(feedAdapter != null) {
        	// We have to manually refilter, ArrayAdapter<T> is written in
        	// a way that does not do this.
            if(filterConstraint != null){
                feedAdapter.getFilter().filter(filterConstraint);
            }
            feedAdapter.notifyDataSetChanged();
        }
    }

    private void init() {
        setItemsCanFocus(true);
        setScrollingCacheEnabled(true);
        setVerticalFadingEdgeEnabled(false);
        setOnItemClickListener(new ClickListener());
        setDividerHeight(0);
        setSelector(android.R.color.transparent);
    }

}