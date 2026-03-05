package com.appusagestats;

import android.app.AppOpsManager;
import android.app.usage.UsageEvents;
import android.app.usage.UsageStatsManager;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Process;
import android.provider.Settings;

import androidx.annotation.NonNull;

import com.facebook.react.bridge.Arguments;
import com.facebook.react.bridge.Promise;
import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReactContextBaseJavaModule;
import com.facebook.react.bridge.ReactMethod;
import com.facebook.react.bridge.ReadableArray;
import com.facebook.react.bridge.WritableArray;
import com.facebook.react.bridge.WritableMap;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

public class UsageStatsModule extends ReactContextBaseJavaModule {

    private static final String MODULE_NAME = "UsageStatsModule";
    private static final String E_PERMISSION_DENIED = "E_PERMISSION_DENIED";
    private static final String E_UNKNOWN = "E_UNKNOWN";

    UsageStatsModule(ReactApplicationContext context) {
        super(context);
    }

    @Override
    @NonNull
    public String getName() {
        return MODULE_NAME;
    }

    // -----------------------------------------------------------------------
    // Public API
    // -----------------------------------------------------------------------

    @ReactMethod
    public void openUsageAccessSettings(Promise promise) {
        try {
            Intent intent = new Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            getReactApplicationContext().startActivity(intent);
            promise.resolve(null);
        } catch (Exception e) {
            promise.reject(E_UNKNOWN, "Failed to open usage access settings: " + e.getMessage(), e);
        }
    }

    @ReactMethod
    public void hasUsageAccess(Promise promise) {
        try {
            promise.resolve(checkPermission());
        } catch (Exception e) {
            promise.reject(E_UNKNOWN, e.getMessage(), e);
        }
    }

    @ReactMethod
    public void getUsageByApps(ReadableArray appNames, int dayInterval, Promise promise) {
        try {
            if (!checkPermission()) {
                promise.reject(E_PERMISSION_DENIED,
                        "Usage access permission not granted. Call openUsageAccessSettings() first.");
                return;
            }

            Set<String> packageFilter = new HashSet<>();
            for (int j = 0; j < appNames.size(); j++) {
                packageFilter.add(appNames.getString(j));
            }

            Calendar calendar = Calendar.getInstance();
            WritableArray resultArray = Arguments.createArray();

            long endTime = calendar.getTimeInMillis();
            setStartOfDay(calendar);
            long startTime = calendar.getTimeInMillis();

            for (int i = 0; i < dayInterval; i++) {
                HashMap<String, AppUsageInfo> appUsageMap = trackAppUsage(startTime, endTime, packageFilter);

                for (String packageName : packageFilter) {
                    AppUsageInfo info = appUsageMap.get(packageName);
                    long seconds = (info != null) ? info.totalTimeInForeground / 1000 : 0;
                    addToResultArray(resultArray, packageName, seconds, startTime);
                }

                // Move to previous day
                calendar.add(Calendar.DAY_OF_MONTH, -1);
                setStartOfDay(calendar);
                startTime = calendar.getTimeInMillis();
                setEndOfDay(calendar);
                endTime = calendar.getTimeInMillis();
            }

            promise.resolve(resultArray);
        } catch (Exception e) {
            promise.reject(E_UNKNOWN, e.getMessage(), e);
        }
    }

    @ReactMethod
    public void getUsageAllApps(int dayInterval, Promise promise) {
        try {
            if (!checkPermission()) {
                promise.reject(E_PERMISSION_DENIED,
                        "Usage access permission not granted. Call openUsageAccessSettings() first.");
                return;
            }

            Calendar calendar = Calendar.getInstance();
            long endTime = calendar.getTimeInMillis();
            setStartOfDay(calendar);
            long startTime = calendar.getTimeInMillis();

            WritableArray resultArray = Arguments.createArray();

            for (int i = 0; i < dayInterval; i++) {
                HashMap<String, AppUsageInfo> appUsageMap = trackAppUsage(startTime, endTime, null);

                WritableMap reportMap = Arguments.createMap();
                WritableArray dataArray = Arguments.createArray();
                reportMap.putString("date", formatDate(startTime));

                for (Map.Entry<String, AppUsageInfo> entry : appUsageMap.entrySet()) {
                    long seconds = entry.getValue().totalTimeInForeground / 1000;
                    if (seconds > 0) {
                        WritableMap appMap = Arguments.createMap();
                        appMap.putString("trackId", entry.getKey());
                        appMap.putDouble("usage", seconds);
                        dataArray.pushMap(appMap);
                    }
                }

                reportMap.putArray("data", dataArray);
                resultArray.pushMap(reportMap);

                // Move to previous day
                calendar.add(Calendar.DAY_OF_MONTH, -1);
                setStartOfDay(calendar);
                startTime = calendar.getTimeInMillis();
                setEndOfDay(calendar);
                endTime = calendar.getTimeInMillis();
            }

            promise.resolve(resultArray);
        } catch (Exception e) {
            promise.reject(E_UNKNOWN, e.getMessage(), e);
        }
    }

    // -----------------------------------------------------------------------
    // Internal helpers
    // -----------------------------------------------------------------------

    private boolean checkPermission() {
        AppOpsManager appOpsManager = (AppOpsManager) getReactApplicationContext()
                .getSystemService(Context.APP_OPS_SERVICE);
        if (appOpsManager == null) {
            return false;
        }
        int mode;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            mode = appOpsManager.unsafeCheckOpNoThrow(
                    AppOpsManager.OPSTR_GET_USAGE_STATS,
                    Process.myUid(),
                    getReactApplicationContext().getPackageName());
        } else {
            mode = appOpsManager.checkOpNoThrow(
                    AppOpsManager.OPSTR_GET_USAGE_STATS,
                    Process.myUid(),
                    getReactApplicationContext().getPackageName());
        }
        return mode == AppOpsManager.MODE_ALLOWED;
    }

    private String formatDate(long timestamp) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.US);
        return sdf.format(new Date(timestamp));
    }

    private void addToResultArray(WritableArray array, String packageName, long usageSeconds, long startTime) {
        WritableMap map = Arguments.createMap();
        map.putString("trackId", packageName);
        map.putDouble("usage", usageSeconds);
        map.putString("date", formatDate(startTime));
        array.pushMap(map);
    }

    private void setStartOfDay(Calendar calendar) {
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
    }

    private void setEndOfDay(Calendar calendar) {
        calendar.set(Calendar.HOUR_OF_DAY, 23);
        calendar.set(Calendar.MINUTE, 59);
        calendar.set(Calendar.SECOND, 59);
        calendar.set(Calendar.MILLISECOND, 999);
    }

    /**
     * Core event-based algorithm: queries UsageEvents and computes foreground durations.
     *
     * @param startTimeMillis Window start (inclusive).
     * @param endTimeMillis   Window end (exclusive).
     * @param packageFilter   If non-null, only track these packages (performance optimization).
     * @return Map of packageName -> usage info.
     */
    private HashMap<String, AppUsageInfo> trackAppUsage(long startTimeMillis, long endTimeMillis,
            Set<String> packageFilter) {
        UsageStatsManager usageStatsManager = (UsageStatsManager) getReactApplicationContext()
                .getSystemService(Context.USAGE_STATS_SERVICE);

        HashMap<String, AppUsageInfo> appUsageMap = new HashMap<>();

        if (usageStatsManager == null) {
            return appUsageMap;
        }

        UsageEvents usageEvents = usageStatsManager.queryEvents(startTimeMillis, endTimeMillis);
        if (usageEvents == null) {
            return appUsageMap;
        }

        while (usageEvents.hasNextEvent()) {
            UsageEvents.Event event = new UsageEvents.Event();
            usageEvents.getNextEvent(event);

            String packageName = event.getPackageName();

            // Skip packages not in filter (when filter is provided)
            if (packageFilter != null && !packageFilter.contains(packageName)) {
                continue;
            }

            AppUsageInfo info = appUsageMap.get(packageName);
            if (info == null) {
                info = new AppUsageInfo(packageName);
                appUsageMap.put(packageName, info);
            }

            int eventType = event.getEventType();

            if (eventType == UsageEvents.Event.ACTIVITY_RESUMED) {
                info.lastForegroundTime = event.getTimeStamp();
            } else if ((eventType == UsageEvents.Event.ACTIVITY_PAUSED
                    || eventType == UsageEvents.Event.ACTIVITY_STOPPED)) {
                if (info.lastForegroundTime > 0) {
                    info.totalTimeInForeground += event.getTimeStamp() - info.lastForegroundTime;
                    info.lastForegroundTime = -1;
                }
                // If lastForegroundTime is 0 or -1, we don't have a matching RESUMED event;
                // skip to avoid negative durations.
            }
        }

        // Handle apps that are still in the foreground at the end of the window.
        for (AppUsageInfo info : appUsageMap.values()) {
            if (info.lastForegroundTime > 0) {
                info.totalTimeInForeground += endTimeMillis - info.lastForegroundTime;
                info.lastForegroundTime = -1;
            }
        }

        return appUsageMap;
    }

    // -----------------------------------------------------------------------
    // Inner class
    // -----------------------------------------------------------------------

    private static class AppUsageInfo {
        final String packageName;
        long totalTimeInForeground = 0;
        /** >0 means currently in foreground since that timestamp; -1 means not in foreground; 0 means unknown. */
        long lastForegroundTime = 0;

        AppUsageInfo(String packageName) {
            this.packageName = packageName;
        }
    }
}
