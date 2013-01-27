package com.heyzap.sdk;

import android.content.Context;
import android.util.Log;

public class Logger {
    static boolean ONLY_TEMP_LOG = false;
    static boolean ENABLED = true;

    public static void init(final Context context){
    	new Thread(new Runnable(){
    		@Override
    		public void run(){
    			ENABLED = ENABLED || Utils.packageIsInstalled("com.example.android.snake", context);
    		}
    	}).start();
    }
    
    public static void debug(Object s) {
        if(!ENABLED) return;
        if(s != null){
            debug(s.toString());
        } else {
            debug("NULL");
        }
    }

    public static void debug(String s){
        if(!ENABLED) return;
        if(s != null) {
            Log.d("HeyzapSDK", s);
        } else {
            debug("NULL");
        }
    }

    public static void warn(String s) {
      Log.w("HeyzapSDK", s);
    }

    public static void trace(Object s){
        if(!ENABLED) return;
        StringBuilder builder = new StringBuilder();
        builder.append(String.format("Stack Trace: %s\n", String.valueOf(s)));
        StackTraceElement[] trace = new RuntimeException().getStackTrace();
        for(int i=1; i<trace.length; i++){
            StackTraceElement el = trace[i];
            builder.append(String.format("\t%s:%d in %s.%s\n", el.getFileName(), el.getLineNumber(), el.getClassName(), el.getMethodName()));
        }
        Logger.log(builder.toString());
    }

    public static void log(Object s){ debug(s); }
    public static void log(String s){ debug(s); }
    
    public static void t(Object...os){
        StringBuilder builder = new StringBuilder();
        for(int i=0; i<os.length; i++){
            builder.append(String.valueOf(os[i]));
            
            if((i+1) < os.length){
                builder.append(", ");
            }
        }
        log(builder.toString());
    }
    
    public static void log(Object... os){
        if(!ENABLED) return;
        if(ONLY_TEMP_LOG) return;
        
        if(os == null){
            log("null arguments");
            return;
        }
        StringBuilder builder = new StringBuilder();
        for(int i=0; i<os.length; i++){
            builder.append(String.valueOf(os[i]));
            
            if((i+1) < os.length){
                builder.append(", ");
            }
        }
        log(builder.toString());
    }
    public static void trace(){ trace(""); }
}
