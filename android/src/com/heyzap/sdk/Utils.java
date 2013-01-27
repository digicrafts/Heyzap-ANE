package com.heyzap.sdk;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.pm.ResolveInfo;
import android.content.pm.Signature;
import android.graphics.Bitmap;
import android.graphics.Rect;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Build;
import android.os.Handler;
import android.provider.Settings;
import android.util.Log;
import android.view.TouchDelegate;
import android.view.View;

class Utils {
	public static final String HEYZAP_PACKAGE = "com.heyzap.android";
	private static final int LEADERBOARD_VERSION = 4006006;
	private static final int ACHIEVEMENTS_VERSION = 4012001;
	public static final int GAME_DETAILS_PACKAGE_VERSION = 4012002; // first version to support sending game_package in Intent extras to GameDetails

	private static float density = -1;
	static ExecutorService requestThread = Executors.newSingleThreadExecutor();
	private static String deviceId = "unknown";
	private static String packageName = "unknown";
	private static HashMap<String, String> extraParams;

	public static String capitalize(String s) {
		return s.substring(0, 1).toUpperCase() + s.substring(1);
	}

	public static String truncate(String s, int limit) {
		if (s.length() > limit) {
			s = s.substring(0, limit) + "...";
		}
		return s;
	}

	public static String getAppLabel(Context context) {
		CharSequence label = null;
		try {
			label = context.getPackageManager().getApplicationLabel(context.getPackageManager().getApplicationInfo(context.getPackageName(), 0));
		} catch (NameNotFoundException e) {
		}
		return label == null ? null : label.toString();
	}

	public static int daysBetween(Date d1, Date d2) {
		return Math.abs((int) ((d1.getTime() - d2.getTime()) / (60 * 60 * 24 * 1000)));
	}

	// Ensure the android market is installed
	public static boolean marketInstalled(Context context) {
		try {
			Intent marketIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=com.heyzap.android"));
			List<ResolveInfo> resolveInfo = context.getPackageManager().queryIntentActivities(marketIntent, PackageManager.MATCH_DEFAULT_ONLY);
			if (resolveInfo.isEmpty()) {
				return false;
			}
		} catch (Exception e) {
			return false;
		}

		return true;
	}

	static void load(Context context) {
		getDeviceId(context); // precache
	}

	public static String getPackageName(Context context) {
		if (packageName.equals("unknown") && context != null) {
			packageName = context.getPackageName();
		}
		return packageName;
	}

	public static String getDeviceId(Context context) {
		if (deviceId.equals("unknown") && context != null) {
			// Load up the device id
			String product = Build.PRODUCT;
			String androidId = Settings.Secure.getString(context.getContentResolver(), Settings.Secure.ANDROID_ID);
			if (product != null && androidId != null) {
				deviceId = product + "_" + androidId;
			}
		}
		return deviceId;
	}

	// Check if the android version is high enough for the heyzap app
	public static boolean androidVersionSupported() {
		try {
			if (Integer.parseInt(Build.VERSION.SDK) < 7) {
				return false;
			}
		} catch (Exception e) {
			return false;
		}

		return true;
	}

	static void post(final String endpoint, final String postData, final ResponseHandler handler) {
		Handler uiHandlerTemp = null;
		try {
			uiHandlerTemp = new Handler();
		} catch (RuntimeException e) {
			// ignore
		}
		final Handler uiHandler = uiHandlerTemp;
		requestThread.execute(new Runnable() {
			@Override
			public void run() {
				Throwable error = null;
				String response = null;
				try {
					// Open the connection
					URL url = new URL(endpoint);
					HttpURLConnection conn = (HttpURLConnection) url.openConnection();
					try {
						// Set up the connection
						conn.setDoOutput(true);

						// Write the post data to the stream
						OutputStreamWriter out = new OutputStreamWriter(conn.getOutputStream());
						out.write(postData);
						out.flush();
						out.close();

						// Get the response
						response = convertStreamToString(conn.getInputStream()).trim();
						if (handler != null)
							handler.onSuccess(response);
					} catch (IOException e) {
						// Ignore any file stream issues
						if (handler != null)
							handler.onFailure(e);
					} finally {
						conn.disconnect();
					}
				} catch (IOException e) {
					// Ignore any connection failure when trying to open the
					// connection
					if (handler != null)
						handler.onFailure(e);
				} catch (UnsupportedOperationException e) {
					// Ignore any url building issues
					if (handler != null)
						handler.onFailure(e);
				}
				final Throwable fError = error;
				final String fResponse = response;
				Runnable post = new Runnable() {
					@Override
					public void run() {

					}
				};
			}
		});

	}

	private static String convertStreamToString(InputStream is) {
		BufferedReader reader = new BufferedReader(new InputStreamReader(is));
		StringBuilder sb = new StringBuilder();
		String line = null;
		try {
			while ((line = reader.readLine()) != null) {
				sb.append(line + "\n");
			}
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			try {
				is.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return sb.toString();
	}

	static HashMap<String, String> extraParams(Context context) {
		if (extraParams == null) {
			extraParams = new HashMap<String, String>();
			extraParams.put("sdk_version", HeyzapAnalytics.HEYZAP_SDK_VERSION);
			extraParams.put("sdk_platform", HeyzapAnalytics.HEYZAP_SDK_PLATFORM);
			extraParams.put("android_version", Build.VERSION.SDK);
			extraParams.put("game_package", getPackageName(context));
			extraParams.put("device_id", getDeviceId(context));
		}
		return extraParams;
	}

	public static int dpToPx(Context context, int dp) {
		density = density > 0 ? density : context.getResources().getDisplayMetrics().density;
		return (int) (dp * density + 0.5f);
	}

	public static String getSignatureHash(Context context) {
		String hash = "";
		try {
			Signature[] signatures = context.getPackageManager().getPackageInfo(context.getPackageName(), PackageManager.GET_SIGNATURES).signatures;
			byte[] sig = signatures[0].toByteArray();
			hash = Base64.encodeToString(MessageDigest.getInstance("SHA-1").digest(sig), Base64.NO_WRAP);
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		} catch (NameNotFoundException e) {
			e.printStackTrace();
		}
		return hash;
	}

	public static String getAbsolutePath(Context context, String fileName) {
		return String.format("%s/files/%s", context.getCacheDir().getAbsolutePath(), fileName);
	}

	public static String saveBitmapToLocalFile(Context context, String fileName, Bitmap image) {
		try {
			if (fileName == null)
				throw new Exception("No filename.");
			if (image == null)
				throw new Exception("No image.");

			String absolutePath = getAbsolutePath(context, fileName);

			ByteArrayOutputStream bs = new ByteArrayOutputStream();
			image.compress(Bitmap.CompressFormat.PNG, 0, bs);

			FileOutputStream outputStream = context.openFileOutput(fileName, Context.MODE_WORLD_READABLE | Context.MODE_WORLD_WRITEABLE);
			if (outputStream != null) {
				outputStream.write(bs.toByteArray());
			} else {
				throw new Exception("Unable to open output file stream.");
			}

			return absolutePath;
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}

	public static int getScaledSize(Context context, int baseSize) {
		return getScaledSize(context, (float) baseSize);
	}

	public static int getScaledSize(Context context, float baseSize) {
		if (density <= 0) {
			density = context.getResources().getDisplayMetrics().density;
		}

		return (int) (density * baseSize);
	}

	public static int getInverseScaledSize(Context context, int pixels) {
		return getInverseScaledSize(context, (float) pixels);
	}

	public static int getInverseScaledSize(Context context, float pixels) {
		if (density <= 0) {
			density = context.getResources().getDisplayMetrics().density;
		}

		return (int) (pixels / density);
	}

	// wrapper is a view. inner is its only child, usually a button. inner has
	// click/touch listeners. this will make all clicks/touches on wrapper get
	// passed through to inner. even pressed states will work!
	public static void clickWrap(final Context context, final View wrapper, final View inner, final int extraPaddingDpTop, final int extraPaddingDpRight, final int extraPaddingDpBottom, final int extraPaddingDpLeft) {
		if (wrapper != null) {
			wrapper.post(new Runnable() {
				public void run() {
					final Rect r = new Rect();
					wrapper.getHitRect(r);
					r.top -= Utils.getScaledSize(context, extraPaddingDpTop);
					r.right += Utils.getScaledSize(context, extraPaddingDpRight);
					r.bottom += Utils.getScaledSize(context, extraPaddingDpBottom);
					r.left -= Utils.getScaledSize(context, extraPaddingDpLeft);
					wrapper.setTouchDelegate(new TouchDelegate(r, inner));
				}
			});
		}
	}

	public static void clickWrap(Context context, View wrapper, View inner) {
		clickWrap(context, wrapper, inner, 0);
	}

	public static void clickWrap(Context context, View wrapper, View inner, int extraPaddingDp) {
		clickWrap(context, wrapper, inner, extraPaddingDp, extraPaddingDp, extraPaddingDp, extraPaddingDp);
	}

	static interface ResponseHandler {
		public void onSuccess(String response);

		public void onFailure(Throwable e);
	}

	static boolean heyzapIsInstalled(Context context) {
		return packageIsInstalled(HEYZAP_PACKAGE, context);
	}

	static int heyzapVersion(Context context) {
		PackageInfo pInfo;
		try {
			pInfo = context.getPackageManager().getPackageInfo(HEYZAP_PACKAGE, 0);
			int version = pInfo.versionCode;
			return version;
		} catch (NameNotFoundException e) {
			e.printStackTrace();
			return -1;
		}

	}

	// true if Heyzap is installed and is new enough version to support
	// Leaderboards
	static boolean hasHeyzapLeaderboards(Context context) {
		return heyzapIsInstalled(context) && heyzapVersion(context) >= LEADERBOARD_VERSION;
	}
    
    // true if Heyzap is installed and is new enough version to support Leaderboards
    static boolean hasHeyzapAchievements(Context context) {
        return heyzapIsInstalled(context) && heyzapVersion(context) >= ACHIEVEMENTS_VERSION;
    }

	static boolean packageIsInstalled(String packageName, Context context) {
		boolean installed = false;

		try {
			PackageManager pm = context.getPackageManager();
			Intent pi = pm.getLaunchIntentForPackage(packageName);
			if (pi != null) {
				List<ResolveInfo> list = pm.queryIntentActivities(pi, PackageManager.MATCH_DEFAULT_ONLY);
				if (list.size() > 0) {
					installed = true;
				}
			}
		} catch (Exception e) {
		}

		return installed;
	}

	private static boolean isAirplaneModeOn(Context context) {
		return Settings.System.getInt(context.getContentResolver(), Settings.System.AIRPLANE_MODE_ON, 0) != 0;
	}
	
	public static boolean isOnline(Context context) {
	    ConnectivityManager cm =
	        (ConnectivityManager)context.getSystemService(Context.CONNECTIVITY_SERVICE);

	    return (cm.getActiveNetworkInfo() != null && 
	       cm.getActiveNetworkInfo().isConnectedOrConnecting()) && !isAirplaneModeOn(context);
	}

    public static String hex(byte[] array) {
        StringBuffer sb = new StringBuffer();
        for (int i = 0; i < array.length; ++i) {
            sb.append(Integer.toHexString((array[i]
                & 0xFF) | 0x100).substring(1,3));       
        }
        return sb.toString();
    }

    public static String md5Hex(String message) {
        try {
            MessageDigest md =
                MessageDigest.getInstance("MD5");
            return hex (md.digest(message.getBytes("CP1252")));
        } catch (NoSuchAlgorithmException e) {
        } catch (UnsupportedEncodingException e) {
        }
        return null;
    }
    
    public static void installHeyzap(Context context, String additionalAnalyticsParam) {
        // Launch the android market
        String uri = String.format("market://details?id=%s&referrer=%s", HeyzapLib.HEYZAP_PACKAGE, HeyzapAnalytics.getAnalyticsReferrer(context, additionalAnalyticsParam));
        Log.d("HeyzapSDK", "Sending player to market, uri: " + uri);

        Intent popup = new Intent(Intent.ACTION_VIEW, Uri.parse(uri));
        popup.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_MULTIPLE_TASK);
        context.startActivity(popup);
    }
}
