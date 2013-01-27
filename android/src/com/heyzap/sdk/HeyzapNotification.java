package com.heyzap.sdk;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.util.Log;

class HeyzapNotification {
    private static final int NOTIFICATION_ID = 100101001;

    public static void send(Context context, String appName){
        Context applicationContext = context.getApplicationContext();
        
        String contentTitle = "Get more from " + (HeyzapLib.subtleNotifications() ? "your games" : appName);
        String contentText = "Install Heyzap to share with your friends!"; //lies
        
        int icon = android.R.drawable.btn_star_big_on;
        if(!HeyzapLib.subtleNotifications()){
            icon = context.getApplicationInfo().icon;
        }
        
        CharSequence tickerText = contentTitle + "\n" + contentText;
        long when = System.currentTimeMillis();

        Notification notification = new Notification(icon, tickerText, when);
        notification.flags |= Notification.FLAG_AUTO_CANCEL;

        String uri = "market://details?id="+HeyzapLib.HEYZAP_PACKAGE +"&referrer=" + HeyzapAnalytics.getAnalyticsReferrer(context, "notification=true");
        Intent marketIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(uri));
        PendingIntent contentIntent = PendingIntent.getActivity(context, 0, marketIntent, 0);
        
        notification.setLatestEventInfo(applicationContext, contentTitle, contentText, contentIntent);
        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.notify(NOTIFICATION_ID, notification);
        
        HeyzapAnalytics.trackEvent(context, "notification-sent");       
   }
    
    public static void sendPB(Context context, String appName, String displayScore) {
        Context applicationContext = context.getApplicationContext();
        
        String contentTitle = "Personal best: " + displayScore + "!";
        String contentText = "Install Heyzap to save your scores!"; //lies
        
        int icon = android.R.drawable.btn_star_big_on;
        if(!HeyzapLib.subtleNotifications()){
            icon = context.getApplicationInfo().icon;
        }
        
        CharSequence tickerText = contentTitle + "\n" + contentText;
        long when = System.currentTimeMillis();

        Notification notification = new Notification(icon, tickerText, when);
        notification.flags |= Notification.FLAG_AUTO_CANCEL;

        String uri = "market://details?id="+HeyzapLib.HEYZAP_PACKAGE +"&referrer=" + HeyzapAnalytics.getAnalyticsReferrer(context, "notification_pb=true");
        Intent marketIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(uri));
        PendingIntent contentIntent = PendingIntent.getActivity(context, 0, marketIntent, 0);
        
        notification.setLatestEventInfo(applicationContext, contentTitle, contentText, contentIntent);
        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.notify(NOTIFICATION_ID, notification);
        
        HeyzapAnalytics.trackEvent(context, "notification-sent-pb");       
    }
}