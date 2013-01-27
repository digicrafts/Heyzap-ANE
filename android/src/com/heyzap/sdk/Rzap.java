package com.heyzap.sdk;


public class Rzap {
    public static int attr(String id){ return getIdentifier("attr", id); }
    public static int drawable(String id){ return getIdentifier("drawable", id); }
    public static int layout(String id){ return getIdentifier("layout", id); }
    public static int anim(String id){ return getIdentifier("anim", id);}
    public static int id(String id){ return getIdentifier("id", id);}
    public static int getIdentifier(String type, String id){
        return HeyzapLib.applicationContext.getResources().getIdentifier(id, type, HeyzapLib.applicationContext.getPackageName());
    }
}
// sed -i 's/\([^.]\)R\.\([a-z]*\?\)\.\([a-zA-Z_]*\?\)/\1Rzap.\2("\3")/' src/**/*.java
