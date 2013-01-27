package com.heyzap.http;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.util.List;

import com.heyzap.sdk.Logger;

import android.content.Context;
import android.content.pm.PackageInfo;

public class HeyzapCookies {
    private static final String HEYZAP_COOKIE_FILENAME = "Usr.hz";
    private static String hzCookies = null;
    
    // Dont create me!
    private HeyzapCookies() {
        throw new AssertionError();
    }
    
    // Gets the cookie from this game's package directory. If that doesnt exist it checks all
    // other packages to see if they have one. If they do it copies it into this package dir for 
    // future lookup. If it can't find any then it creates one.
    protected static synchronized String getCookie(Context context) {
        // Try and find the local copy
        hzCookies = readCookies(context);
        Logger.log("cookies", hzCookies);
        if( hzCookies != null ) return hzCookies;
    
        try {
            // Look for other games copies
            hzCookies = lookForCookiesInOtherGames(context);
            if ( hzCookies == null ) {
                hzCookies = "";
            }
            writeCookies(context, hzCookies);
            return hzCookies;
        } catch (Exception ex1) {
            return hzCookies;
        }
    }
    
    // Looks through all the installed packages and checks to see if any have the cookies
    private static String lookForCookiesInOtherGames(Context context) {
        List<PackageInfo> packs = context.getPackageManager().getInstalledPackages(0);
        String cookies;
        for(PackageInfo pack : packs) {
            if( pack == null ) {
                continue;
            }
            cookies = readCookies(context, pack);
            if ( cookies != null ){
                return cookies;
            }
        }
        return null;
    }
    
    // Propagates the cookie throughout all games
    static void propagateCookies(Context context, String cookie) {
        List<PackageInfo> packs = context.getPackageManager().getInstalledPackages(0);
        String cookies;
        for(PackageInfo pack : packs) {
            if( pack == null ) {
                continue;
            }
            cookies = readCookies(context, pack);
            if ( cookies != null ){
                writeCookies(context, pack, cookie);
            }
        }
    }
    
    // Read a cookie from a file in the context package dir
    private static String readCookies(Context context){
        return readCookies(new File(context.getFilesDir(), HEYZAP_COOKIE_FILENAME));
    }
    
    // Read a cookie from a file in the packageinfo package dir
    private static String readCookies(Context context, PackageInfo pack){
        return readCookies(new File(getPackageFilesDir(context, pack), HEYZAP_COOKIE_FILENAME));
    }
    
    // Read a cookie from a file
    private static String readCookies(File file){
        FileInputStream stream = null;
        InputStreamReader inputStream = null;
        BufferedReader bufferedReader = null;
        try {
            stream = new FileInputStream(file);
            inputStream = new InputStreamReader(stream);
            bufferedReader = new BufferedReader(inputStream);
            String data = bufferedReader.readLine();
            if ( data == null ) {
                data = "";
            }
            return data;
        } catch (FileNotFoundException ex1) {
            //Not interested
            return null;
        } catch (Exception ex1) {
            //Blind catch - we dont want this to go anywhere user facing
            ex1.printStackTrace();
            return null;
        } finally {
            try { if (bufferedReader != null) {bufferedReader.close();} } catch(Exception ex2) {}
            try { if (inputStream != null) {inputStream.close();} } catch(Exception ex3) {}
            try { if (stream != null) {stream.close();} } catch(Exception ex4) {}
        }
    }
    
    // Write a cookie to a file
    private static void writeCookies(Context context, PackageInfo pack, String cookie) {
        Context packageContext = contextForPackageInfo(context, pack);
        if(packageContext != null) {
            writeCookies(packageContext, cookie);
        }
    }
    
    // Write a cookie to a file
    private static void writeCookies(Context context, String cookie) {
        if(cookie == null) return;
        FileOutputStream outputStream = null;
        try {
            outputStream = context.openFileOutput(HEYZAP_COOKIE_FILENAME, Context.MODE_WORLD_READABLE | Context.MODE_WORLD_WRITEABLE);
            outputStream.write(cookie.getBytes());
        } catch (Exception ex1) {
            //Blind catch - we dont want this to go anywhere user facing
            ex1.printStackTrace();
        } finally {
            try { if (outputStream != null) {outputStream.close();} } catch(Exception ex2) {}
        }
    }
    
    // Get the context for a provided package info
    private static Context contextForPackageInfo(Context context, PackageInfo pack) {
        try {
            return context.createPackageContext(pack.packageName, Context.CONTEXT_RESTRICTED);
        } catch (Exception ex1) {
            //Blind catch - we dont want this to go anywhere user facing
            ex1.printStackTrace();
            return null;
        }
    }
    
    // Get the files directory for a provided package info
    private static String getPackageFilesDir(Context context, PackageInfo pack) {
        return pack.applicationInfo.dataDir + "/files";
    }
}
