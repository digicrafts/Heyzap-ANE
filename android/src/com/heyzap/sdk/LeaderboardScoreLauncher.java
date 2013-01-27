package com.heyzap.sdk;

import java.io.UnsupportedEncodingException;
import java.math.BigInteger;
import java.security.MessageDigest;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.drawable.Drawable;

import com.heyzap.http.RequestParams;

public class LeaderboardScoreLauncher {
    public static final String SCORES = "HeyzapLeaderboardsScores";
    public static final String DISPLAY_SCORES = "HeyzapLeaderboardsDisplayScores";
    public static final String ORDER = "HeyzapLeaderboardsLowestScoreFirst";

    // make the request to new_score and show modal dialog if personal best otherwise show subtle dialog
    public static void launchScoreDialog(final Context context, final String score, final String displayScore, final String levelId, final Drawable gameIcon,
            final String gamePackage, final boolean fromSdk, final boolean skipModalDialog) {
        final Float scoreF = Float.parseFloat(score);

        final SharedPreferences scoresPrefs = context.getSharedPreferences(SCORES, 0);
        final SharedPreferences displayScoresPrefs = context.getSharedPreferences(DISPLAY_SCORES, 0);
        final SharedPreferences orderPrefs = context.getSharedPreferences(ORDER, 0);

        if (scoresPrefs.contains(levelId) && orderPrefs.contains(levelId)) {
            // Determine which dialog to show without doing the request
            Float personalBest = scoresPrefs.getFloat(levelId, 0.0f);
            String personalBestDisplay = displayScoresPrefs.getString(levelId, "");
            boolean lowestScoreFirst = orderPrefs.getBoolean(levelId, false);
            boolean newBest;

            if (lowestScoreFirst) {
                newBest = scoreF < personalBest;
            } else {
                newBest = scoreF > personalBest;
            }

            if (newBest) {
                saveLeaderboardInfoOnPhone(context, scoreF, displayScore, levelId, null, true);
                personalBestDisplay = displayScore;
            }

            if (newBest && !skipModalDialog) {
                // Show full dialog immediately, and the dialog will make the request to save it and get leaderboard
                LeaderboardScoreDialogFull d = new LeaderboardScoreDialogFull(context, null, gamePackage, score, displayScore, levelId);
                d.setFromSdk(fromSdk);
                d.show();
            } else {
                // Show top dialog immediately, and make the request to save it in the background
                LeaderboardScoreDialogTop d = new LeaderboardScoreDialogTop(context, null, gamePackage, score, displayScore, levelId, personalBestDisplay,
                        gameIcon);
                if (newBest && skipModalDialog) {
                    d.setPersonalBest(true);
                }
                d.setFromSdk(fromSdk);
                d.show();
            }
        } else {
            // Immediately show personal best dialog (technically, their device may have a better score on the server, e.g. if they cleared their shared prefs,
            // but that should be rare).
            // The dialog will post this score in the background and fetch and save the true personal best.
            saveLeaderboardInfoOnPhone(context, scoreF, displayScore, levelId, null, true);
            
            if (skipModalDialog) {
                LeaderboardScoreDialogTop d = new LeaderboardScoreDialogTop(context, null, gamePackage, score, displayScore, levelId, displayScore, gameIcon);
                d.setPersonalBest(true);
                d.setFromSdk(fromSdk);
                d.show();
            } else {
                LeaderboardScoreDialogFull d = new LeaderboardScoreDialogFull(context, null, gamePackage, score, displayScore, levelId);
                d.setFromSdk(fromSdk);
                d.show();
            }
        }

    }

    public static void saveLeaderboardInfoOnPhone(Context context, Float score, String displayScore, String levelId, Boolean lowestScoreFirst, boolean commit) {
        final SharedPreferences scoresPrefs = context.getSharedPreferences(SCORES, 0);
        final SharedPreferences displayScoresPrefs = context.getSharedPreferences(DISPLAY_SCORES, 0);
        final SharedPreferences orderPrefs = context.getSharedPreferences(ORDER, 0);

        final SharedPreferences.Editor scoresEditor = scoresPrefs.edit();
        final SharedPreferences.Editor displayScoresEditor = displayScoresPrefs.edit();
        final SharedPreferences.Editor orderEditor = orderPrefs.edit();

        if (score != null && displayScore != null && levelId != null) {
            scoresEditor.putFloat(levelId, score);
            scoresEditor.commit();
            displayScoresEditor.putString(levelId, displayScore);
            displayScoresEditor.commit();
        }

        if (levelId != null && lowestScoreFirst != null) {
            orderEditor.putBoolean(levelId, lowestScoreFirst);
            orderEditor.commit();
        }
    }

    public static void removeLeaderboardInfoFromPhone(Context context) {
        final SharedPreferences scoresPrefs = context.getSharedPreferences(SCORES, 0);
        final SharedPreferences displayScoresPrefs = context.getSharedPreferences(DISPLAY_SCORES, 0);
        final SharedPreferences orderPrefs = context.getSharedPreferences(ORDER, 0);

        final SharedPreferences.Editor scoresEditor = scoresPrefs.edit();
        final SharedPreferences.Editor displayScoresEditor = displayScoresPrefs.edit();
        final SharedPreferences.Editor orderEditor = orderPrefs.edit();

        scoresEditor.clear();
        displayScoresEditor.clear();
        orderEditor.clear();

        scoresEditor.commit();
        displayScoresEditor.commit();
        orderEditor.commit();
    }

    public static RequestParams getNewScoreRequestParams(String score, String displayScore, String levelId) {
        RequestParams requestParams = new RequestParams();
        requestParams.put("score", score);
        requestParams.put("display_score", displayScore);
        requestParams.put("level", levelId);

        String key = "";
        StringBuilder b = new StringBuilder();
        b.append(score);
        b.append(displayScore);
        b.append(levelId);
        String value = b.toString();
        byte[] bytes = null;

        try {
            bytes = value.getBytes("UTF-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        try {
            MessageDigest m;
            m = MessageDigest.getInstance("MD5");
            m.update(bytes, 0, value.length());
            key = new BigInteger(1, m.digest()).toString(16);
        } catch (Exception e1) {
            e1.printStackTrace();
        }

        requestParams.put("key", key);
        return requestParams;
    }
}
