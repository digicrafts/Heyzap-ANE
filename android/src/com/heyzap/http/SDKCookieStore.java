/*
    Android Asynchronous Http Client
    Copyright (c) 2011 James Smith <james@loopj.com>
    http://loopj.com

    Licensed under the Apache License, Version 2.0 (the "License");
    you may not use this file except in compliance with the License.
    You may obtain a copy of the License at

        http://www.apache.org/licenses/LICENSE-2.0

    Unless required by applicable law or agreed to in writing, software
    distributed under the License is distributed on an "AS IS" BASIS,
    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
    See the License for the specific language governing permissions and
    limitations under the License.
*/

package com.heyzap.http;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.http.client.CookieStore;
import org.apache.http.cookie.Cookie;
import org.apache.http.impl.cookie.BasicClientCookie;

import android.content.Context;
import android.content.SharedPreferences;
import android.text.TextUtils;

import com.heyzap.sdk.Logger;

/**
 * A persistent cookie store which implements the Apache HttpClient
 * {@link CookieStore} interface. Cookies are stored and will persist on the
 * user's device between application sessions since they are serialized and
 * stored in {@link SharedPreferences}.
 * <p>
 * Instances of this class are designed to be used with
 * {@link AsyncHttpClient#setCookieStore}, but can also be used with a 
 * regular old apache HttpClient/HttpContext if you prefer.
 */
public class SDKCookieStore implements CookieStore {
    private static final String COOKIE_PREFS = "CookiePrefsFile";
    private static final String COOKIE_NAME_STORE = "names";
    private static final String COOKIE_NAME_PREFIX = "cookie_";
    private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("EEE, dd-MMM-yyyy hh:mm:ss z");
    
    private final ConcurrentHashMap<String, Cookie> cookies;
    private final SharedPreferences cookiePrefs;
    private Context context;
    
    /**
     * Construct a persistent cookie store.
     */
    public SDKCookieStore(Context context) {
        cookiePrefs = context.getSharedPreferences(COOKIE_PREFS, Context.MODE_WORLD_READABLE | Context.MODE_WORLD_WRITEABLE);
        cookies = new ConcurrentHashMap<String, Cookie>();
        this.context = context;

        addCookies(HeyzapCookies.getCookie(context));
    }

    @Override
    public void addCookie(Cookie cookie) {
        String name = cookie.getName();

        // Save cookie into local store, or remove if expired
        if(!cookie.isExpired(new Date())) {
            cookies.put(name, cookie);
        } else {
            cookies.remove(name);
        }
        
        HeyzapCookies.propagateCookies(context, getCookieString());
    }

    @Override
    public void clear() {
        // Clear cookies from local store
        cookies.clear();

        HeyzapCookies.propagateCookies(context, getCookieString());
    }

    @Override
    public boolean clearExpired(Date date) {
        boolean clearedAny = false;
        SharedPreferences.Editor prefsWriter = cookiePrefs.edit();

        for(ConcurrentHashMap.Entry<String, Cookie> entry : cookies.entrySet()) {
            String name = entry.getKey();
            Cookie cookie = entry.getValue();
            if(cookie.isExpired(date)) {
                // Clear cookies from local store
                cookies.remove(name);

                // Clear cookies from persistent store
                prefsWriter.remove(COOKIE_NAME_PREFIX + name);

                // We've cleared at least one
                clearedAny = true;
            }
        }

        // Update names in persistent store
        if(clearedAny) {
            prefsWriter.putString(COOKIE_NAME_STORE, TextUtils.join(",", cookies.keySet()));
        }
        prefsWriter.commit();

        return clearedAny;
    }

    @Override
    public List<Cookie> getCookies() {
        return new ArrayList<Cookie>(cookies.values());
    }
    
    
    public void addCookies(String cookiesString){
        List<BasicClientCookie> cookies = new ArrayList<BasicClientCookie>();
        String[] cookieStrings = cookiesString.split(";\\s+");
        for(String cookieString : cookieStrings){
            Logger.log("cookieString", cookieString);
            String[] cookieKeys = cookieString.split(";");
            if(cookieKeys.length > 0){
                String[] cookieKeyValue = cookieKeys[0].split("=");
                if(cookieKeyValue.length == 2){
                    BasicClientCookie cookie = new BasicClientCookie(cookieKeyValue[0], cookieKeyValue[1]);
                    cookie.setDomain("android.heyzap.com");
                    addCookie(cookie);
                }
            }
        }
    }
    
    public String getCookieString(){
        ArrayList<String> cookieStrings = new ArrayList<String>();
        
        for(Cookie cookie : cookies.values()){
            cookieStrings.add(getCookieString(cookie));
        }
        return TextUtils.join("; ", cookieStrings);
    }

    public String getCookieString(Cookie cookie){
        String cookieString = String.format("%s=%s", cookie.getName(), cookie.getValue());
        return cookieString;
    }
}