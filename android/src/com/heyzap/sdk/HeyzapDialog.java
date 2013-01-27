package com.heyzap.sdk;

import android.app.Activity;
import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

// import com.heyzap.sdk.R;

class HeyzapDialog extends Dialog {
    private static final int BUTTON_WIDTH = 120;
    private static final int BUTTON_HEIGHT = 40;
    private static final int BUTTON_TEXT_SIZE = 13;
    protected LinearLayout dialogView;
    private RelativeLayout buttonRow;
    protected Context activityContext;
    private boolean overlay = false;
    private DismissDialogBroadcastReceiver dismissReceiver;
    private Float dimAmount = null;

    public HeyzapDialog(Context context) {
        this(context, true);
    }

    public HeyzapDialog(Context context, boolean overlay) {
        this(context, overlay, null);
    }

    public HeyzapDialog(Context context, boolean overlay, Float dimAmount) {
        super(overlay ? context.getApplicationContext() : context);
        this.activityContext = context;
        this.overlay = overlay;
        this.dimAmount = dimAmount;
        try {
            this.dismissReceiver = new DismissDialogBroadcastReceiver();
        } catch (NullPointerException e) {
            // one guy had a phone that did this
        }
    }

    protected int getLayout() {
        return Rzap.layout("heyzap_dialog");
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(getLayout());
        super.onCreate(savedInstanceState);
    }

    @Override
    public void show() {
        if (overlay) {
            this.getWindow().setType(WindowManager.LayoutParams.TYPE_SYSTEM_ALERT);
            if (dismissReceiver != null) {
                getContext().registerReceiver(dismissReceiver, new IntentFilter(Intent.ACTION_CLOSE_SYSTEM_DIALOGS));
            }
        }
        super.show();
    }

    @Override
    public void dismiss() {
        if (overlay) {
            try {
                if (dismissReceiver != null) {
                    getContext().unregisterReceiver(dismissReceiver);
                }
            } catch (RuntimeException e) {
            }
        }
        try {
            super.dismiss();
        } catch (IllegalArgumentException e) {// View not attached to window manager)
            // fine then.
        }
    }

    @Override
    public void onDetachedFromWindow() {
        try {
            this.getWindow().setType(WindowManager.LayoutParams.TYPE_TOAST);
        } catch (IllegalArgumentException e) {
            // ignore
        }
        super.onDetachedFromWindow();
    }

    @Override
    public void onAttachedToWindow() {
        if (overlay) {
            try {
                this.getWindow().setType(WindowManager.LayoutParams.TYPE_APPLICATION);
                this.getWindow().getWindowManager().updateViewLayout(this.getWindow().getDecorView(), getWmParams());
            } catch (IllegalArgumentException e) {// View not attached to window manager
                this.dismiss();
            }
        }
        super.onAttachedToWindow();
    }

    public Context getActivityContext() {
        return this.activityContext;
    }

    public void startActivity(Intent intent) {
        if (!(getActivityContext() instanceof Activity)) {
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        }
        getActivityContext().startActivity(intent);
    }

    public void setView(View contentView) {
        ViewGroup content = (ViewGroup) findViewById(Rzap.id("content"));
        content.removeAllViews();
        content.addView(contentView);
    }

    public void clearDialogBackground() {
        ViewGroup content = (ViewGroup) findViewById(android.R.id.content);
        ((View) content.getParent()).setBackgroundDrawable(null);
    }

//    public void addPrimaryButton(String title, View.OnClickListener listener) {
//        addPrimaryButton(title, listener, Rzap.drawable("dialog_button_primary"));
//    }

//    public void addPrimaryButton(String title, View.OnClickListener listener, int buttonId) {
//        addButton(title, 0xFFFFFFFF, buttonId, RelativeLayout.ALIGN_PARENT_RIGHT, listener);
//    }

//    public void addSecondaryButton(String title, View.OnClickListener listener) {
//        addButton(title, 0xFF000000, Rzap.drawable("dialog_button_secondary"), RelativeLayout.ALIGN_PARENT_LEFT, listener);
//    }

//    public void addButton(String title, int textColor, int backgroundResource, int position, View.OnClickListener listener) {
//        final float scale = getActivityContext().getResources().getDisplayMetrics().density;
//
//        if (buttonRow == null) {
//            buttonRow = new RelativeLayout(getActivityContext());
//            /* this is a hack because the Amazon Fire has different dialogs than other platforms */
//            if (android.os.Build.MANUFACTURER.equals("Amazon") == false) {
//                buttonRow.setBackgroundResource(Rzap.drawable("dialog_button_background"));
//            }
//
//            buttonRow.setPadding((int) (scale * 2), (int) (scale * 5), (int) (scale * 2), (int) (scale * 4));
//            ((ViewGroup) findViewById(Rzap.id("buttons"))).addView(buttonRow);
//        }
//
//        Button button = new Button(getActivityContext());
//        button.setBackgroundResource(backgroundResource);
//        button.setTextColor(textColor);
//        button.setTextSize(BUTTON_TEXT_SIZE);
//        button.setText(title);
//        button.setOnClickListener(listener);
//
//        RelativeLayout.LayoutParams layout = new RelativeLayout.LayoutParams((int) (BUTTON_WIDTH * scale), (int) (BUTTON_HEIGHT * scale));
//        layout.addRule(position);
//        buttonRow.addView(button, layout);
//    }

    private class DismissDialogBroadcastReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            HeyzapDialog.this.dismiss();
        }
    };

    public WindowManager.LayoutParams getWmParams() {
        WindowManager.LayoutParams params = this.getWindow().getAttributes();
        if (dimAmount != null) {
            params.dimAmount = 0.0f;
        }
        return params;
    }
}
