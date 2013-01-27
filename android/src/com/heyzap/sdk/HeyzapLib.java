package com.heyzap.sdk;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;
import android.util.Log;

import com.heyzap.http.RequestParams;
import com.heyzap.sdk.AdOverlay.AdState;

public class HeyzapLib {
	public static final String HEYZAP_PACKAGE = "com.heyzap.android";
	private static final String HEYZAP_INTENT_CLASS = ".CheckinForm";
	public static final String IMAGE_FILE_NAME = "hzSdkImage.png";
	public static final String FIRST_RUN_KEY = "HeyzapFirstRun";
	public static final String OVERLAY_PREF = "HeyzapLeaderboardOverlay";
	public static final String LAST_PB_NOTIF = "HeyzapPBNotif";

	public static final Handler handler = new Handler(Looper.getMainLooper());

	private static String packageName;
	private static String newLevel;

	private static WeakReference<BroadcastReceiver> playWithFriendsReceiverRef;

	/* Disable the Heyzap checkin prompt */
	public static int FLAG_NO_HEYZAP_INSTALL_SPLASH = 1 << 1;
	
	/* Disable the Heyzap install prompt notification */
	public static int FLAG_NO_NOTIFICATION = 1 << 23;
	
	/* Only show the Heyzap install notification to returning users */
	public static int FLAG_SUBTLE_NOTIFICATION = 1 << 24;
	
	private static int flags = 0;
	private static ActivityResultListener activityResultListener;

	static boolean ssoTestPassed = false;
	static Context applicationContext;

	private static ProgressDialog progressDialog;
	private static Object progressDialogLock = new Object();
	
	private static LevelRequestListener levelRequestListener;
	private static String pendingLevelId;
	private static boolean gameLaunchRegistered;
        private static boolean adsEnabled = false;
        static {
          Log.d("heyzap-sdk", "HeyzapLib loading (static)");
        }

	//
	// API methods exposed to developers
	//

	// Load the heyzap API, this call must be called first, this also:
	// - sends the android level "get heyzap" local notification
	// - shows the "checkin" popup over the game

	public static void load(final Context context, boolean showHeyzapInstallSplash) {
		applicationContext = context.getApplicationContext();
		packageName = context.getPackageName();

		HeyzapAnalytics.trackEvent(context, "heyzap-start");

		SDKRestClient.init(context);
		Logger.init(context);
		Utils.load(context);
		
		final String appName = Utils.getAppLabel(context);
		if (appName == null) return;
		
		final SharedPreferences overlayPrefs = context.getSharedPreferences(OVERLAY_PREF, 0);

		// Show overlay if they don't have heyzap and haven't seen the overlay
		// yet
		if (!Utils.heyzapIsInstalled(context) && !overlayPrefs.getBoolean(packageName, false) && showHeyzapInstallSplash) {
			((Activity) context).runOnUiThread(new Runnable() {
				@Override
				public void run() {
					final SharedPreferences.Editor overlayEditor = overlayPrefs.edit();
					overlayEditor.putBoolean(packageName, true);
					overlayEditor.commit();
					showFullOverlay(applicationContext);
				}
			});
		}

		if (context instanceof Activity) {
			Intent i = ((Activity) context).getIntent();
			if (i.hasExtra("level")) {
				pendingLevelId = ((Activity) context).getIntent().getStringExtra("level");
				Logger.log("received level id", pendingLevelId);
			}
		}

		if (!gameLaunchRegistered) {

			IntentFilter filter = new IntentFilter("com.heyzap.android.GAME_LAUNCHED");
			applicationContext.registerReceiver(new BroadcastReceiver() {
				@Override
				public void onReceive(Context context, Intent intent) {
					Logger.log("received broadcast", intent, intent.getStringExtra("level"));

					String packageName = intent.getStringExtra("package");
					if (packageName == null || !packageName.equals(context.getPackageName()))
						return;

					Logger.log("passed package test");

					String levelId = intent.getStringExtra("level");
					if (levelId == null)
						return;

					Logger.log("passed package and level tests");
					if (levelRequestListener != null) {
						levelRequestListener.onLevelRequested(levelId);
						pendingLevelId = null;
					} else {
						pendingLevelId = levelId;
					}
				}
			}, filter);
			gameLaunchRegistered = true;
		}
	}

	public static void enableAds(final Context context) {
		if (adsEnabled) {
			return;
		}
		applicationContext = context.getApplicationContext();
		
		adsEnabled = true;
		
		final SharedPreferences prefs = context.getSharedPreferences(FIRST_RUN_KEY, 0);
		final boolean firstRun = !prefs.getBoolean("ran_once", false);
		
		// If this is our first run then we need to get a list of all the
		// installed packages on the system
		if (firstRun) {
			List<PackageInfo> packages = context.getPackageManager()
					.getInstalledPackages(0);
			
			final List<String> packageNames = new ArrayList<String>();
			
			for (PackageInfo packageInfo : packages) {
				packageNames.add(packageInfo.packageName);
			}
			
			addInitialPackages(context, packageNames);
		}
		
		fetchAd(context);					

	}

	public static Context getApplicationContext() {
		return applicationContext;
	}

	// Show the checkin form for this game, or show the "get heyzap" dialog
	public static void checkin(final Context context) {
		checkin(context, null);
	}

	public static void checkin(final Context context, final String prefillMessage) {
		applicationContext = context.getApplicationContext();
		((Activity) context).runOnUiThread(new Runnable() {
			@Override
			public void run() {
				rawCheckin(context, prefillMessage);
			}
		});
	}

	public static void showFullOverlay(final Context context) {
	    applicationContext = context.getApplicationContext();
	    new LeaderboardFullOverlay(context).show();
	}

	public static void showInGameOverlay(final Context context, final String displayName, final String source) {
		((Activity) context).runOnUiThread(new Runnable(){
			@Override
			public void run(){
				applicationContext = context.getApplicationContext();
				LeaderboardInGameOverlay overlay = new LeaderboardInGameOverlay(context, source);
				overlay.setDisplayName(displayName);
				overlay.show();				
			}
		});
	}

	public static void showLeaderboards(final Context context) {
		showLeaderboards(context, null);
	}

	public static void showLeaderboards(final Context context, final String levelId) {
		((Activity) context).runOnUiThread(new Runnable(){
			@Override
			public void run(){
				applicationContext = context.getApplicationContext();
				if (Utils.hasHeyzapLeaderboards(context)) {
					Intent i = new Intent(Intent.ACTION_MAIN);
					i.setAction(HEYZAP_PACKAGE);
					i.putExtra("game_context_package", context.getPackageName());
					if (levelId != null) {
						i.putExtra("level", levelId);
					}
					i.addCategory(Intent.CATEGORY_LAUNCHER);
					i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_MULTIPLE_TASK);
					i.setComponent(new ComponentName(HEYZAP_PACKAGE, HEYZAP_PACKAGE + ".activity.Leaderboards"));

					context.startActivity(i);
				} else {
					new LeaderboardDialog(context, context.getPackageName(), levelId).show();
				}
		
			}
		});
	}

	public static String getLevel(Activity context) {
		if (context == null)
			return null;

		Intent intent = context.getIntent();
		if (intent == null)
			return null;

		return intent.getStringExtra("levelId");
	}

	public static void setLevelRequestListener(LevelRequestListener listener) {
		Logger.log("setLevelRequestListener called", pendingLevelId);
		levelRequestListener = listener;
		if (pendingLevelId != null) {
			levelRequestListener.onLevelRequested(pendingLevelId);
			pendingLevelId = null;
		}
	}

	public static void submitScore(final Context context, final String score, final String displayScore, final String levelId) {
		submitScore(context, score, displayScore, levelId, false);
	}

	public static void submitScore(final Context context, final String score, final String displayScore, final String levelId, final boolean skipModalDialog) {
		applicationContext = context.getApplicationContext();

		HeyzapAnalytics.trackEvent(context, "score-received");

		if (Utils.hasHeyzapLeaderboards(context)) {
			Intent i = new Intent();
			i.setAction("com.heyzap.android.LeaderboardsReceiver");
			i.putExtra("leaderboard_action", "show_score_overlay");
			i.putExtra("score", score);
			i.putExtra("display_score", displayScore);
			i.putExtra("level", levelId);
			i.putExtra("game_context_package", context.getPackageName());
			i.setFlags(32); // Intent.FLAG_INCLUDE_STOPPED_PACKAGES);
			if (skipModalDialog) {
				i.putExtra("skip_modal_dialog", true);
			}
			context.sendBroadcast(i);
		} else {
			((Activity) context).runOnUiThread(new Runnable() {

				@Override
				public void run() {
					Drawable gameIcon = null;
					try {
						gameIcon = applicationContext.getPackageManager().getApplicationIcon(packageName);
					} catch (NameNotFoundException e) {
						e.printStackTrace();
					}
					LeaderboardScoreLauncher.launchScoreDialog(context, score, displayScore, levelId, gameIcon, context.getPackageName(), true, skipModalDialog);
				}
			});
		}
	}

	public static void launchLeaderboardActivityOrShowInGameOverlay(Context context, String levelId, String gamePackage, String displayName, String source) {
		if (Utils.hasHeyzapLeaderboards(context)) {
			// i.setAction("com.heyzap.android.activity.Leaderboards");
			// i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

			// context.startActivity(i);

			Intent i = new Intent(Intent.ACTION_MAIN);
			i.setAction(HEYZAP_PACKAGE);
			i.putExtra("level", levelId);
			i.putExtra("game_context_package", gamePackage);
			i.addCategory(Intent.CATEGORY_LAUNCHER);
			i.putExtra("packageName", context.getPackageName());
			i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_MULTIPLE_TASK);
			i.setComponent(new ComponentName(HEYZAP_PACKAGE, HEYZAP_PACKAGE + ".activity.Leaderboards"));

			context.startActivity(i);

		} else {
			showInGameOverlay(context, displayName, source);
		}
	}
	
    /**
     * Post achievement. Show Achievement dialog on success.
     * 
     * @param context
     * @param achievementIds
     */
	public static void unlockAchievement(final Context context, final List<String> achievementIds){
		unlockAchievement(context, TextUtils.join(",", achievementIds));
	}

    /**
     * Post achievement. Show Achievement dialog on success.
     * 
     * @param context
     * @param achievementIds
     *     A comma-separated list of achievement ids
     */
    public static void unlockAchievement(final Context context, final String achievementIds) {
        applicationContext = context.getApplicationContext();
        final String gameContextPackage = context.getPackageName();

        if (Utils.hasHeyzapAchievements(context)) {
            Intent i = new Intent();
            i.setAction("com.heyzap.android.LeaderboardsReceiver");
            i.putExtra("leaderboard_action", "unlock_achievements");
            i.putExtra("unlock_achievements", achievementIds);
            i.putExtra("game_context_package", gameContextPackage);
            i.setFlags(32); //Intent.FLAG_INCLUDE_STOPPED_PACKAGES);
            context.sendBroadcast(i);
        } else {
	        ((Activity) context).runOnUiThread(new Runnable() {
	            @Override
	            public void run() {
	                applicationContext = context.getApplicationContext();
	        
	                // param "game_package" is automatically added:
	                RequestParams requestParams = new RequestParams();
	                requestParams.put("achievement_ids", achievementIds);
	                requestParams.put("game_context_package", gameContextPackage);
	                requestParams.put("key", Utils.md5Hex(achievementIds + gameContextPackage));
	                
	                SDKRestClient.post(context, "/in_game_api/achievements/unlock", requestParams, new SDKResponseHandler() {
	                    @Override
	                    public void onSuccess(JSONObject response) {
	                        
	                        if (response.has("achievements")) {
	                            JSONArray stream;
	                            try {
	                                stream = response.getJSONArray("achievements");
	                                int unlockedAchievements = stream.length();
	                            	
	                                if (unlockedAchievements > 0) {
	                                    AchievementDialogFull dialog = new AchievementDialogFull(context, false, response);
	                                    dialog.setTitle("New Achievement Unlocked!");
	                                    HeyzapAnalytics.trackEvent(context, "achievement-dialog-unlocked-shown");
	                                    dialog.show();
	                                }
	                            } catch (org.json.JSONException e) {
	                                e.printStackTrace();
	                            }
	                        }
	                    }
	        
	                    @Override
	                    public void onFailure(Throwable e) {
	                    }
	                });
	            }
	        });
        }
    }
    
    /**
     * View all achievements.
     * 
     * @param context
     * @param includeLocked
     */
    public static void showAchievements(final Context context) {
        applicationContext = context.getApplicationContext();
        String gameContextPackage = context.getPackageName();

        // param "game_package" is automatically added:
        final RequestParams requestParams = new RequestParams();
        
        // only show unlocked achievements?
//        if (!viewAllAchievements) {
//        	requestParams.put("unlocked", "true");
//        }
        requestParams.put("game_context_package", gameContextPackage);

        // if they have the Heyzap app with a version that includes Achievements
        if (Utils.hasHeyzapAchievements(context)) {
            Intent i = new Intent();
            i.setAction("com.heyzap.android.LeaderboardsReceiver");
            i.putExtra("leaderboard_action", "show_achievements");
            i.putExtra("game_context_package", gameContextPackage);
            i.setFlags(32); //Intent.FLAG_INCLUDE_STOPPED_PACKAGES);
            context.sendBroadcast(i);
        } else {
        	if(context instanceof Activity){
        		((Activity) context).runOnUiThread(new Runnable(){
        			@Override
        			public void run(){
        				AchievementDialogFull dialog = new AchievementDialogFull(context, true, null);
        				HeyzapAnalytics.trackEvent(context, "achievement-dialog-all-shown");
        				dialog.show();        				
        			}
        		});
        	}
        }
    }

	public static void clearScorePrefs(Context context) {
		LeaderboardScoreLauncher.removeLeaderboardInfoFromPhone(context);
		final SharedPreferences overlayPrefs = context.getSharedPreferences(OVERLAY_PREF, 0);
		final SharedPreferences.Editor overlayEditor = overlayPrefs.edit();
		overlayEditor.clear();
		overlayEditor.commit();
	}

	// Check if Heyzap is supported on this device
	public static boolean isSupported(Context context) {
		return Utils.marketInstalled(context) && Utils.androidVersionSupported();
	}

	// Set sdk flags to do stuff like disable the phone notification
	public static void setFlags(int newFlags) {
		flags = newFlags;
	}
	
	public static int getFlags(){
		return flags;
	}

	//
	// PRIVATE: Not exposed to game developers
	//
	static void rawCheckin(final Context context, final String prefillMessage) {
		packageName = context.getPackageName();
		applicationContext = context.getApplicationContext();
		Log.v(HeyzapAnalytics.LOG_TAG, "checkin-called");

		if (Utils.packageIsInstalled(HEYZAP_PACKAGE, context)) {
			launchCheckinForm(context, prefillMessage);
		} else {
			// Show the "pre-market" dialog
			HeyzapAnalytics.trackEvent(context, "checkin-button-clicked");
			new PreMarketCheckinDialog(context.getApplicationContext(), packageName, prefillMessage).show();
		}
	}

	static boolean subtleNotifications() {
		return (FLAG_SUBTLE_NOTIFICATION & flags) > 0;
	}

	static void broadcastEnableSDK(Context context) {
		// Tell the heyzap app this is an SDK game, so the popup does not show
		// up
		Intent broadcast = new Intent("com.heyzap.android.enableSDK");
		broadcast.putExtra("packageName", context.getPackageName());
		context.sendBroadcast(broadcast);
	}

	static void sendPBNotification(final Context context, String displayScore) {
		if((flags & FLAG_NO_NOTIFICATION) > 0) return;
		final String appName = Utils.getAppLabel(context);
		if (appName == null) {
			return;
		}

		if (Utils.packageIsInstalled(HEYZAP_PACKAGE, context) || !Utils.marketInstalled(context) || !Utils.androidVersionSupported()) {
			return;
		}

		long now = System.nanoTime() / 1000000000;
		long last = context.getSharedPreferences(LAST_PB_NOTIF, 0).getLong("seconds", 0l);

		// only once every 24 hours
		if (last == 0 || (now - last) < 60 * 60 * 24) {
			// save the time
			Editor editor = context.getSharedPreferences(LAST_PB_NOTIF, 0).edit();
			editor.putLong("seconds", now);
			editor.commit();

			// actually create the notification
			HeyzapNotification.sendPB(context, appName, displayScore);
		}

	}

	static void sendNotification(final Context context) {
		if ((FLAG_NO_NOTIFICATION & flags) > 0)
			return;
		final String appName = Utils.getAppLabel(context);
		if (appName == null)
			return;

		// Send the "get heyzap" android local notification, unless they
		// already have heyzap or their phone doesn't support Heyzap
		if (!Utils.packageIsInstalled(HEYZAP_PACKAGE, context) && Utils.marketInstalled(context) && Utils.androidVersionSupported()) {
			try {
				Date today = new Date();
				if ((FLAG_SUBTLE_NOTIFICATION & flags) > 0) {
					SharedPreferences prefs = context.getSharedPreferences(FIRST_RUN_KEY, 0);
					long firstRun = prefs.getLong("firstRunAt", 0);
					if (firstRun == 0) {
						Editor editor = prefs.edit();
						editor.putLong("firstRunAt", today.getTime());
						editor.commit();
						return;
					} else if (Utils.daysBetween(today, new Date(firstRun)) < 1) {
						return;
					}
				}

				Date lastNotification = new Date(context.getSharedPreferences(FIRST_RUN_KEY, 0).getLong("notificationLastShown", 0l));
				int numberOfNotifications = context.getSharedPreferences(FIRST_RUN_KEY, 0).getInt("numberNotificationsShown", 0);
				switch (numberOfNotifications) {
				case 0:
					HeyzapNotification.send(context, appName);
					break;
				case 1:
					if (Utils.daysBetween(lastNotification, today) >= 5) {
						HeyzapNotification.send(context, appName);
					}
					break;
				case 2:
					if (Utils.daysBetween(lastNotification, today) >= 14) {
						HeyzapNotification.send(context, appName);
					}
					break;
				default:
					return;
				}

				// Store when we last showed the notification to avoid over
				// notification
				Editor editor = context.getSharedPreferences(FIRST_RUN_KEY, 0).edit();
				editor.putInt("numberNotificationsShown", numberOfNotifications + 1);
				editor.putLong("notificationLastShown", today.getTime());
				editor.commit();
			} catch (Exception e) {
				Log.d(HeyzapAnalytics.LOG_TAG, "Exception while sending notification");
				e.printStackTrace();
			}
		}
	}

	static void launchCheckinForm(final Context context, String prefillMessage) {
		String packageName = HEYZAP_PACKAGE;

		Intent popup = new Intent(Intent.ACTION_MAIN);
		popup.putExtra("message", prefillMessage);
		popup.setAction(packageName);
		popup.addCategory(Intent.CATEGORY_LAUNCHER);
		popup.putExtra("packageName", context.getPackageName());
		popup.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_MULTIPLE_TASK);
		popup.setComponent(new ComponentName(packageName, HEYZAP_PACKAGE + HEYZAP_INTENT_CLASS));

		context.startActivity(popup);
	}

	static interface ActivityResultListener {
		public void onActivityResult(int requestCode, int resultCode, Intent data);
	}

	public static interface LevelRequestListener {
		public void onLevelRequested(String levelId);
	}

	public static void showAd(final Context context) {
        applicationContext = context.getApplicationContext();
        if(!adsEnabled){
          throw new RuntimeException("enableAds() must be called before showAd()");
        }
		
		if( !(context instanceof Activity) )
		{
			Log.e("heyzap-heyzap-sdk-ads", "showAd not passed Activity as context! Unable to display ad. ERROR.");
			return;
		}
		
		final Activity activity = (Activity) context;

		Log.d("heyzap-sdk-ads", "showAd called with state " + adState);

		// Only show an ad if we're actually online (no point if we have no
		// connectivity.. they can't install it)
		if (Utils.isOnline(context)) {
			if (adState == AdState.LOADING) {
				activity.runOnUiThread(new Runnable() {
					@Override
					public void run() {
						progressDialog = ProgressDialog.show(context, "", "Loading..", true);
						handler.postDelayed(new Runnable() {
							final ProgressDialog currentDialog = progressDialog;

							@Override
							public void run() {
								synchronized (currentDialog) {
									if (progressDialog != null && currentDialog == progressDialog && currentDialog.isShowing()) {
										try{
											currentDialog.dismiss();
										}catch(IllegalArgumentException e){
											e.printStackTrace();
										}
										progressDialog = null;
									}
								}
							}
						}, 2000);
					}
				});
			} else if (adState == AdState.LOADED) {

				activity.runOnUiThread(new Runnable() {
					@Override
					public void run() {
						if (adState == AdState.LOADED) {
							currentOverlay.setImpressionId(currentImpressionId);
                            currentOverlay.setContext(context);
							currentOverlay.show();
						} else {
							Log.e("heyzap-sdk-ads", "Did not set ad to shown because adstate was: " + adState);
						}
					}
				});
			} else {
				// Somehow adstate is none (probably an error)
				fetchAd(context);
			}
		}
	}

	static volatile AdState adState = AdState.NONE;
	static volatile AdOverlay currentOverlay = null;
	static volatile String currentImpressionId = null;

	public static void fetchAd(final Context context) {
		final Activity activity = (Activity) context;
		
		// Only do this if we haven't already got an ad preloading or loading
		if (adState != AdState.NONE){
			return;
		}


		setAdStateToLoading();

		activity.runOnUiThread(new Runnable(){
			@Override
			public void run(){
				currentOverlay = new AdOverlay(context);
				SDKRestClient.get(context, "http://ads.heyzap.com/in_game_api/ads/fetch_ad", new SDKResponseHandler() {
					@Override
					public void onSuccess(final JSONObject response) {
						if (adState != AdState.LOADING) {
							Log.e("heyzap-sdk-ads", "dropped response because ad state was: " + adState);
							return;
						}
						
						try {
							if (response.isNull("ad_html")) {
								// Go no ad to show
								setAdStateToNone();
								return;
							}

							// Get the ad to display
							// from the server
							String adHTML = (String) response.get("ad_html");
							String strategy = (String) response.get("ad_strategy");
							String promotedGamePackage = ((String) response.get("promoted_game_package"));
							currentImpressionId = (String) response.get("impression_id");

							currentOverlay.update(strategy, promotedGamePackage, adHTML);
							currentOverlay.setClickUrl(response.optString("click_url"));

							if(safeDismissProgressDialog()){
								currentOverlay.show();
							}

							setAdStateToLoaded();

						} catch (JSONException e) {
							e.printStackTrace();

							if (adState == AdState.LOADING) {
								safeDismissProgressDialog();
								setAdStateToNone();
							}
						}
					}

					@Override
					public void onFailure(final Throwable e) {
						e.printStackTrace();
						if (adState == AdState.LOADING) {
							safeDismissProgressDialog();
							setAdStateToNone();
						}
					}
				});
			}
		});
	}
	

	static boolean safeDismissProgressDialog(){
		synchronized (progressDialogLock) {
			if (progressDialog != null) {
				try{
					progressDialog.dismiss();
				}catch(IllegalArgumentException e){
					e.printStackTrace();
				}
				progressDialog = null;
				return true;
			}
		}
		return false;
	}


	static void addInitialPackages(final Context context, final List<String> packageNames) {
		RequestParams reqParameters = new RequestParams();

		String packages = "";

		if (packageNames.size() > 0) {
			for (String packageName : packageNames) {
				packages += packageName + ",";
			}

			packages = packages.substring(0, packages.length() - 1);
		}

		reqParameters.put("packages", packages);

		final RequestParams finalParams = reqParameters;

		new Thread(new Runnable() {
			@Override
			public void run() {
				SDKRestClient.post(context, "http://ads.heyzap.com/in_game_api/ads/add_initial_packages", finalParams);
			}
		}).start();
	}

	static void registerInstall(final Context context, final String packageName) {
		Log.v("heyzap-sdk-ads", "registerInstall called with package name: " + packageName);

		new Thread(new Runnable() {

			@Override
			public void run() {
				RequestParams reqParameters = new RequestParams();

				reqParameters.put("for_game_package", packageName);
				reqParameters.put("platform", "android");

				SDKRestClient.post(context, "http://ads.heyzap.com/in_game_api/v1/register_new_game_install", reqParameters);

			}
		}).start();
	}

	static long lastClicked = 0;

	static void logAdClickAndGoToMarket(final Context context, final String promotedGamePackage, final String strategy, final String impressionId, final String clickUrl) {
		// Just check we haven't received this event twice in close proximity
		if (System.currentTimeMillis() - lastClicked > 1000) {
			(new Thread(new Runnable() {
				@Override
				public void run() {
					RequestParams params = new RequestParams();
					params.put("ad_strategy", strategy);
					params.put("promoted_game_package", promotedGamePackage);
					params.put("impression_id", impressionId);

					SDKRestClient.post(context, "http://ads.heyzap.com/in_game_api/ads/register_click", params);
				}
			})).start();

			closeAd(context);

			// Before we trundle off to the market, set the new ad fetching in
			// the
			// background
			fetchAd(context);
			
			String clickUrlLocal = clickUrl;
			if(clickUrlLocal == null){
				clickUrlLocal = "market://details?id=" + promotedGamePackage + "&referrer=utm_source%3Dheyzap%26utm_medium%3Dmobile%26utm_campaign%3D" + "heyzap_ad_network";
			}
			Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(clickUrlLocal));

			intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);

			final ProgressDialog marketSpinner = ProgressDialog.show(context, "", "Loading..", true);
			handler.postDelayed(new Runnable() {
				@Override
				public void run() {
					try{
					marketSpinner.dismiss();
					}catch(IllegalArgumentException e){
						e.printStackTrace();
					}
				}
			}, 2000);
			
			context.startActivity(intent);
			
			lastClicked = System.currentTimeMillis();
		}
	}
	
    static void closeAd(Context context) {
        closeAd(context, true);
    }

    // hideOverlay will be false when this gets called from AdOverlay.hide(),
    // e.g. when closing the ad with the back button.
    static void closeAd(Context context, boolean hideOverlay) {
        if (hideOverlay) {
            currentOverlay.hide();
        }
        currentOverlay = null;

        Log.e("ad-sdk", "closeAd, setting to none");
        setAdStateToNone();

        fetchAd(context);
    }

	/*
	 * This set of methods does the transitions from ad states to other ad
	 * states.. and gives us error information if there's an illegal transition
	 */
	static void setAdStateToLoaded() {
		if (adState != AdState.LOADING) {
			Log.e("heyzap-sdk-ads", "Inconsistent transition in setAdStateToLoaded from state " + adState);
		}

		Log.d("heyzap-sdk-ads", "transitioning from " + adState + " to LOADED");
		adState = AdState.LOADED;
	}

	static void setAdStateToLoading() {
		if (adState != AdState.NONE) {
			Log.e("heyzap-sdk-ads", "Inconsistent transition in setAdStateToLoading from state " + adState);
		}

		Log.d("heyzap-sdk-ads", "transitioning from " + adState + " to LOADING");
		adState = AdState.LOADING;
	}

	static void setAdStateToNone() {
		if (adState != AdState.LOADING || adState != AdState.LOADED) {
			Log.e("heyzap-sdk-ads", "Inconsistent transition in setAdStateToNone from state " + adState);
		}

		Log.d("heyzap-sdk-ads", "transitioning from " + adState + " to NONE");
		adState = AdState.NONE;
	}
}
