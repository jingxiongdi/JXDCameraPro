package com.jxd.jxdcamerapro.simulatekey;

import android.accessibilityservice.AccessibilityService;
import android.content.Context;
import android.graphics.Color;
import android.graphics.PixelFormat;
import android.graphics.drawable.GradientDrawable;
import android.provider.Settings;
import android.text.TextUtils;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.WindowManager;
import android.view.accessibility.AccessibilityEvent;
import android.widget.Button;
import android.widget.LinearLayout;

public class SimulateKeyService extends AccessibilityService {
    private static final String TAG = SimulateKeyService.class.getSimpleName();
    public SimulateKeyService() {
    }

    @Override
    public void onAccessibilityEvent(AccessibilityEvent event) {

    }

    @Override
    public void onInterrupt() {

    }

    @Override
    public void onCreate() {
        super.onCreate();
        WindowManager windowManager =  (WindowManager) getApplicationContext().getSystemService(Context.WINDOW_SERVICE);

        WindowManager.LayoutParams mParams = new WindowManager.LayoutParams();
        mParams.flags |= WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE;// 焦点
        mParams.gravity = Gravity.BOTTOM;
        mParams.type = WindowManager.LayoutParams.TYPE_PHONE;
        mParams.width = WindowManager.LayoutParams.WRAP_CONTENT;
        mParams.height = WindowManager.LayoutParams.WRAP_CONTENT;
        mParams.format = PixelFormat.TRANSLUCENT;

        LinearLayout linearLayout = new LinearLayout(this);
        linearLayout.setOrientation(LinearLayout.HORIZONTAL);

        Button recentBtn = new Button(this);
        //recentBtn.setBackgroundColor(Color.TRANSPARENT);
        recentBtn.setText("最近任务");
        recentBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.v(TAG, "recentBtn============== ");
                performGlobalAction(AccessibilityService.GLOBAL_ACTION_RECENTS);
            }
        });

        Button homeBtn = new Button(this);
       // homeBtn.setBackgroundColor(Color.TRANSPARENT);
        homeBtn.setText("桌面");
        homeBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                performGlobalAction(AccessibilityService.GLOBAL_ACTION_HOME);
            }
        });

        Button backBtn = new Button(this);
        //backBtn.setBackgroundColor(Color.TRANSPARENT);
        backBtn.setText("返回");
        backBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                performGlobalAction(AccessibilityService.GLOBAL_ACTION_BACK);
            }
        });

        linearLayout.addView(recentBtn);
        linearLayout.addView(homeBtn);
        linearLayout.addView(backBtn);


        windowManager.addView(linearLayout, mParams);
    }

    public static boolean isAccessibilitySettingsOn(Context mContext) {
        int accessibilityEnabled = 0;
        final String service = mContext.getPackageName() + "/" + SimulateKeyService.class.getCanonicalName();
        try {
            accessibilityEnabled = Settings.Secure.getInt(
                    mContext.getApplicationContext().getContentResolver(),
                    android.provider.Settings.Secure.ACCESSIBILITY_ENABLED);
            Log.v(TAG, "accessibilityEnabled = " + accessibilityEnabled);
        } catch (Settings.SettingNotFoundException e) {
            Log.e(TAG, "Error finding setting, default accessibility to not found: "
                    + e.getMessage());
        }
        TextUtils.SimpleStringSplitter mStringColonSplitter = new TextUtils.SimpleStringSplitter(':');

        if (accessibilityEnabled == 1) {
            Log.v(TAG, "***ACCESSIBILITY IS ENABLED*** -----------------");
            String settingValue = Settings.Secure.getString(
                    mContext.getApplicationContext().getContentResolver(),
                    Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES);
            if (settingValue != null) {
                mStringColonSplitter.setString(settingValue);
                while (mStringColonSplitter.hasNext()) {
                    String accessibilityService = mStringColonSplitter.next();

                    Log.v(TAG, "-------------- > accessibilityService :: " + accessibilityService + " " + service);
                    if (accessibilityService.equalsIgnoreCase(service)) {
                        Log.v(TAG, "We've found the correct setting - accessibility is switched on!");
                        return true;
                    }
                }
            }
        } else {
            Log.v(TAG, "***ACCESSIBILITY IS DISABLED***");
        }

        return false;
    }
}
