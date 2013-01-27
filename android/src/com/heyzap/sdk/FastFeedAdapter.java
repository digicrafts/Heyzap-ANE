package com.heyzap.sdk;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import android.content.Context;
import android.widget.SectionIndexer;

public class FastFeedAdapter extends FeedAdapter implements SectionIndexer {
    public FastFeedAdapter(Context context, List<Feedlette> items) {
        super(context, items);
        init();
    }
    public FastFeedAdapter(Context context){
        super(context);
        init();
    }
    private List<Character> sections;
    private List<Integer> sectionIndicies;
    
    public void init(){
        sections = new ArrayList<Character>();
        sectionIndicies = new ArrayList<Integer>();
    }
    
    @Override
    public int getPositionForSection(int section) {
        if(section >= sectionIndicies.size()) return 0;
        return sectionIndicies.get(section);
    }
    @Override
    public int getSectionForPosition(int position) {
        int idx = Collections.binarySearch(sectionIndicies, position);
        if(idx < 0){
            idx = -idx - 1;
        }
        return idx;
    }
    @Override
    public Object[] getSections() {
        sections = new ArrayList<Character>();
        sectionIndicies = new ArrayList<Integer>();
        Character oldC = null;
        List<Feedlette> fs = this.getItems();
        for(int i=0; i<fs.size(); i++){
            Feedlette f = fs.get(i);
            if(f.displayName != null && f.displayName.length() > 0){
                char c = f.displayName.charAt(0);
                if(oldC != null && !oldC.equals(c)){
                    sections.add(c);
                    sectionIndicies.add(i);
                    oldC = c;
                }
            }
        }
        return sections.toArray();
    }

}
