package com.heyzap.sdk;

import com.heyzap.http.RequestParams;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.WindowManager;
import android.webkit.WebView;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;

public class AdOverlay extends ClickableToast {
	private Context context;
	private WebView webview;
	private long shownAt;
	private String promotedGamePackage;
	private String strategy;
	private RelativeLayout closeWrapper;
	private RelativeLayout adWrapper;
	private boolean hasData = false;
	private String impressionId;
	private String clickUrl;
	private static final float MAX_SIZE_PERCENT = 0.98f;
	private static final int MAX_SIZE_DP_WIDTH = 360;
	private static final int MAX_SIZE_DP_HEIGHT = 360;

	public static enum AdState {
		NONE, LOADING, LOADED
	};

	public AdOverlay(final Context context) {
		super(context.getApplicationContext());

		this.context = context;
		this.setContentView(Rzap.layout("ad_layout"));
		this.setFocusable(true);
		this.setFocusableInTouchMode(true);
        this.context = context;

		Activity activity = (Activity) context;

		int width = (int) Math.round(activity.getWindowManager().getDefaultDisplay().getWidth() * MAX_SIZE_PERCENT);
		int height = (int) Math.round(activity.getWindowManager().getDefaultDisplay().getHeight() * MAX_SIZE_PERCENT);

		width = (int) Math.min(Utils.getScaledSize(context, MAX_SIZE_DP_WIDTH), width);
		height = (int) Math.min(Utils.getScaledSize(context, MAX_SIZE_DP_HEIGHT), height);

		width = (int) Math.min(width, height);
		height = (int) Math.min(width, height);

		adWrapper = (RelativeLayout) findViewById(Rzap.id("ad_wrapper"));

		webview = (WebView) findViewById(Rzap.id("web_view"));

		android.view.ViewGroup.LayoutParams layoutParams = adWrapper.getLayoutParams();

		layoutParams.width = width;
		layoutParams.height = height;

		adWrapper.setLayoutParams(layoutParams);

		webview.setVerticalScrollBarEnabled(false);
		webview.setHorizontalScrollBarEnabled(false);
		webview.setScrollBarStyle(WebView.SCROLLBARS_OUTSIDE_OVERLAY);

		closeWrapper = (RelativeLayout) findViewById(Rzap.id("close_wrapper"));

		closeWrapper.setOnTouchListener(new OnTouchListener() {
			@Override
			public boolean onTouch(View arg0, MotionEvent arg1) {
			    AdOverlay.this.hide();
				return true;
			}
		});
		
		// Set up Heyzap Logo
		ImageView logo = (ImageView) findViewById(Rzap.id("heyzap_corner"));
		// if a newish Heyzap is installed, launch game details.
		// if an older Heyzap is installed, just launch Heyzap.
		// if Heyzap isn't installed, open it in the market.
		logo.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View arg0) {
				AdOverlay.this.hide();
				String gamePackage = promotedGamePackage == null ? "null" : promotedGamePackage;
				if (Utils.heyzapIsInstalled(context)) {
					Intent i = new Intent(Intent.ACTION_MAIN); 
					i.setAction(Utils.HEYZAP_PACKAGE);
					i.putExtra("from_ad_for_game_package", gamePackage);
//					i.addCategory(Intent.CATEGORY_LAUNCHER);
					i.putExtra("packageName", context.getPackageName());
					i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
					
					if (promotedGamePackage != null && Utils.heyzapVersion(context) >= Utils.GAME_DETAILS_PACKAGE_VERSION) {
						i.setComponent(new ComponentName(Utils.HEYZAP_PACKAGE, Utils.HEYZAP_PACKAGE + ".activity.GameDetails"));
						i.putExtra("game_package", promotedGamePackage);
					} else {
						i.setComponent(new ComponentName(Utils.HEYZAP_PACKAGE, Utils.HEYZAP_PACKAGE + ".activity.CheckinHub"));
					}

					context.startActivity(i);
				} else {
					Utils.installHeyzap(context, String.format("action=ad_heyzap_logo&game_package=%s", gamePackage));
				}
			}
		});

		webview.setOnTouchListener(new OnTouchListener() {
			@Override
			public boolean onTouch(View v, MotionEvent event) {
				if ((System.currentTimeMillis() - shownAt) > 500) {
					HeyzapLib.logAdClickAndGoToMarket(AdOverlay.this.context, promotedGamePackage, strategy, impressionId, clickUrl);
				}
				return true;
			}
		}); 

		webview.setBackgroundColor(0x00000000);
		webview.loadDataWithBaseURL(
				null,
				"<style> .body { margin:0; padding:0; } #container { margin: 0; width: 100%;  height: 100%; overflow: hidden; -webkit-border-radius: 20px; border-radius: 20px; background-color: #FFFFFF; } </style><body><div id='container'><center><img style='padding: 60px' src='http://www.heyzap.com/images/common/spinners/64.gif'/></center></div></body>",
				"text/html", "utf-8", null);
	}

	public void update(String strategy, String promotedGamePackage, String adHTML) {
		webview.loadDataWithBaseURL(null, adHTML, "text/html", "utf-8", null);

		webview.setVisibility(View.VISIBLE);

		this.hasData = true;
		this.strategy = strategy;
		this.promotedGamePackage = promotedGamePackage;
	}
	
	public void setClickUrl(String clickUrl){
		this.clickUrl = clickUrl;
	}

	@Override
	public WindowManager.LayoutParams getWmParams() {
		WindowManager.LayoutParams params = super.getWmParams();
		params.gravity = Gravity.CENTER;
		params.width = WindowManager.LayoutParams.WRAP_CONTENT;
		params.verticalMargin = 0.0f;
		params.horizontalMargin = 0.0f;
		params.flags &= ~WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN;
		params.flags &= ~WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE;
		params.flags |= WindowManager.LayoutParams.FLAG_WATCH_OUTSIDE_TOUCH;
		params.flags &= ~WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL;

		return params;
	}

    public void setContext(Context context){
         this.context = context;
    }
	
    @Override
    public void hide() {
        super.hide();
        HeyzapLib.closeAd(context, false);
    }

	@Override
	public void show() {
		shownAt = System.currentTimeMillis();
		if( !this.isShown() )
		{
			super.show();
			RequestParams params = new RequestParams();
			params.put("impression_id", impressionId);
			params.put("promoted_android_package", this.promotedGamePackage);
			SDKRestClient.post(context, "http://ads.heyzap.com/in_game_api/ads/register_impression", params);
		}
	}

	private boolean isOutOfBounds(MotionEvent event) {
		final int x = (int) event.getX();
		final int y = (int) event.getY();
		final int slop = ViewConfiguration.get(getContext()).getScaledWindowTouchSlop();
		final View decorView = this;
		return (x < -slop) || (y < -slop) || (x > (decorView.getWidth() + slop)) || (y > (decorView.getHeight() + slop));
	}

	@Override
	public boolean onTouchEvent(MotionEvent event) {
		return true;
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_BACK) {
			this.hide();
			return true;
		} else {
			return super.onKeyDown(keyCode, event);
		}
	}

	public void setImpressionId(String currentImpressionId) {
		this.impressionId = currentImpressionId;
	}
}
