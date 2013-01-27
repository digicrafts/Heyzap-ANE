package com.heyzap.sdk;

import java.io.InputStream;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Rect;
import android.graphics.Shader;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.LayerDrawable;
import android.graphics.drawable.NinePatchDrawable;
import android.graphics.drawable.PaintDrawable;
import android.graphics.drawable.ShapeDrawable;
import android.graphics.drawable.StateListDrawable;
import android.graphics.drawable.shapes.RectShape;
import android.graphics.drawable.shapes.RoundRectShape;
import android.os.Build;
import android.os.Handler;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;

public class Drawables {
    public static final int DIALOG_BUTTON_BACKGROUND = 1;
    public static final int PRIMARY_BUTTON_BACKGROUND = 2;
    public static final int SECONDARY_BUTTON_BACKGROUND = 3;
    public static final int DIALOG_SPLASH_BACKGROUND = 4;
    
    
    
    public static void setBackgroundDrawable(final Context context, final View view, final int id){
        final Handler h = new Handler();
        new Thread(new Runnable(){
            @Override
            public void run(){
                final Drawable d = Drawables.getDrawable(context, id);
                h.post(new Runnable(){
                    @Override public void run(){
                        view.setBackgroundDrawable(d);
                    }
                });
            }
        }).start();
    }
    
    public static void setBackgroundDrawable(final View view, final String path){
        setBackgroundDrawable(view.getContext(), view, path, null);
    }
    
    public static void setBackgroundDrawable(final Context context, final View view, final String path, final Runnable callback){
        final Handler h = new Handler();
        new Thread(new Runnable(){
            @Override
            public void run(){
                final Drawable d = Drawables.getDrawable(context, path);
                h.post(new Runnable(){
                    @Override public void run(){
                        view.setBackgroundDrawable(d);
                        if(callback != null) callback.run();
                    }
                });
            }
        }).start();
    }
    

    public static void setImageDrawable(final Context context, final ImageView view, final int id){
        final Handler h = new Handler();
        new Thread(new Runnable(){
            @Override
            public void run(){
                final Drawable d = Drawables.getDrawable(context, id);
                h.post(new Runnable(){
                    @Override public void run(){
                        view.setImageDrawable(d);
                    }
                });
            }
        }).start();
    }
    
    public static void setImageDrawable(final ImageView view, String path){ 
        setImageDrawable(view.getContext(), view, path);
    }
     
    public static void setImageDrawable(final Context context, final ImageView view, final String path){
        Logger.log("setting image drawable", view, path);
        final Handler h = new Handler();
        new Thread(new Runnable(){
            @Override
            public void run(){
                final Drawable d = Drawables.getDrawable(context, path);
                h.post(new Runnable(){
                    @Override public void run(){
                        view.setImageDrawable(d);
                    }
                });
            }
        }).start();
    }
    
    private static Drawable getDrawable(Context context, int id){
        switch(id){
        case DIALOG_BUTTON_BACKGROUND:    return getDialogButtonBackground(context);
        case PRIMARY_BUTTON_BACKGROUND:   return getPrimaryButtonBackground(context);
        case SECONDARY_BUTTON_BACKGROUND: return getSecondaryButtonBackground(context);
        case DIALOG_SPLASH_BACKGROUND:    return getSplashDialogBackground(context);
        default:                          return null;
        }
    }
    
    public static Drawable getSplashDialogBackground(Context context){
        int r = Utils.dpToPx(context, 0);
        RoundRectShape backgroundRect = new RoundRectShape(new float[]{r,r,r,r,r,r,r,r},null, new float[]{r,r,r,r,r,r,r,r});
        ShapeDrawable backgroundDrawable = new ShapeDrawable(backgroundRect);
        backgroundDrawable.getPaint().setColor(Color.parseColor("#d1d1d1"));
        
        LayerDrawable layers = new LayerDrawable(new Drawable[]{backgroundDrawable});
        return layers;
    }
    
    public static Drawable getDialogButtonBackground(Context context){
        int r = Utils.dpToPx(context, 5);
        RoundRectShape backgroundRect = new RoundRectShape(new float[]{0,0,0,0,r,r,r,r},null, new float[]{r,r,r,r,r,r,r,r});
        ShapeDrawable backgroundDrawable = new ShapeDrawable(backgroundRect);
        backgroundDrawable.getPaint().setColor(Color.parseColor("#bdbebd"));
        return backgroundDrawable;
    }
    
    public static Drawable getPrimaryButtonBackground(Context context){
        StateListDrawable states = new StateListDrawable();
        Drawable pressed = getDrawable(null, "dialog_grn_btn_sel.png");
        states.addState(new int[]{android.R.attr.state_pressed}, pressed);
        states.addState(new int[]{android.R.attr.state_focused}, pressed);
        states.addState(new int[]{android.R.attr.state_enabled}, getDrawable(null, "dialog_grn_btn.png"));
        return states;
    }
    
    public static Drawable getSecondaryButtonBackground(Context context){
        StateListDrawable states = new StateListDrawable();
        Drawable pressed = getDrawable(null, "dialog_btn_sel.png");
        states.addState(new int[]{android.R.attr.state_pressed}, pressed);
        states.addState(new int[]{android.R.attr.state_focused}, pressed);
        states.addState(new int[]{android.R.attr.state_enabled}, getDrawable(null, "dialog_btn.png"));
        return states;
    }
    
    public static Drawable getFacebookButtonBackground(Context context){
        StateListDrawable states = new StateListDrawable();
        Drawable down = getDrawable(context, "button_fb_down.9.png");
        Drawable up = getDrawable(context, "button_fb_up.9.png");
        states.addState(new int[]{android.R.attr.state_pressed}, down);
        states.addState(new int[]{android.R.attr.state_focused}, down);
        states.addState(new int[]{android.R.attr.state_enabled}, up);
        return states;
    }
    
    public static Drawable getHeyzapButtonBackground(Context context){
        StateListDrawable states = new StateListDrawable();
        Drawable down = getDrawable(context, "getheyzap_down.9.png");
        Drawable up = getDrawable(context, "getheyzap_up.9.png");
        states.addState(new int[]{android.R.attr.state_pressed}, down);
        states.addState(new int[]{android.R.attr.state_focused}, down);
        states.addState(new int[]{android.R.attr.state_enabled}, up);
        return states;
    }
    
    public static Drawable getSettingsBackground(Context context){
        StateListDrawable states = new StateListDrawable();
        Drawable down = getDrawable(context, "cog_down.png");
        Drawable up = getDrawable(context, "cog_up.png");
        states.addState(new int[]{android.R.attr.state_pressed}, down);
        states.addState(new int[]{android.R.attr.state_focused}, down);
        states.addState(new int[]{android.R.attr.state_enabled}, up);
        return states;
    }
    
    public static Drawable getTogleSlider(Context context){
        StateListDrawable states = new StateListDrawable();
        Drawable down = getDrawable(context, "toggle_slider_down.png");
        Drawable up = getDrawable(context, "toggle_slider_up.png");
        states.addState(new int[]{android.R.attr.state_pressed}, down);
        states.addState(new int[]{android.R.attr.state_focused}, down);
        states.addState(new int[]{android.R.attr.state_enabled}, up);
        return states;
    }
    
    public static Drawable getSmallFacebookButtonBackground(Context context){
        StateListDrawable states = new StateListDrawable();
        Drawable down = getDrawable(context, "fb_login_down.png");
        Drawable up = getDrawable(context, "fb_login_up.png");
        states.addState(new int[]{android.R.attr.state_pressed}, down);
        states.addState(new int[]{android.R.attr.state_focused}, down);
        states.addState(new int[]{android.R.attr.state_enabled}, up);
        return states;
    }
    

    
    public static Bitmap getBitmap(Context context, String path){
        InputStream stream = Utils.class.getClassLoader().getResourceAsStream("res/drawable/hz_" + path);
        BitmapFactory.Options opts = new BitmapFactory.Options();
        opts.inScaled = false;
        opts.inDensity = 240;
        opts.inTargetDensity = context.getResources().getDisplayMetrics().densityDpi;
        opts.inScreenDensity = context.getResources().getDisplayMetrics().densityDpi;

        Bitmap bmp = BitmapFactory.decodeStream(stream, null, opts);
        return bmp;
    }
    
    public static Drawable getDrawable(Context context, String path){
        Bitmap bmp = getBitmap(context, path);
        if(bmp == null){
            return null;
        }
        
        Drawable drawable = null;
        byte[] chunk = bmp.getNinePatchChunk();
        if(chunk != null){
            drawable = new NinePatchDrawable(bmp, chunk, NinePatchChunk.deserialize(chunk).mPaddings, path);
        }else{
            drawable = new BitmapDrawable(bmp);
        }
        try{
            if(Integer.parseInt(Build.VERSION.SDK) >= 4){
                new DrawableDensitySetter().setDensity(drawable, context.getResources().getDisplayMetrics().densityDpi, context.getResources());
            }
        }catch(Exception e){}//unnecessary

        return drawable;
    }
    
    public static class DrawableDensitySetter{
        // to make sure setDensity isn't loaded into 1.5's jvm
        public void setDensity(Drawable drawable, int density, Resources resources){
            if(drawable instanceof BitmapDrawable){
                ((BitmapDrawable) drawable).setTargetDensity(density);
            }
            if(drawable instanceof NinePatchDrawable){
                ((NinePatchDrawable) drawable).setTargetDensity(density);
            }
        }
    }
}
