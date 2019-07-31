package com.rui.common_base.util;

import android.content.Context;
import android.content.SharedPreferences;

import com.rui.common_base.base.BaseApplication;

public class SharedPreferenceUtil {
    private static String name = "config";

    private SharedPreferenceUtil() {
        throw new UnsupportedOperationException("cannot be instantiated");
    }

    public static SharedPreferences getSharedPreference() {
        return BaseApplication.getApplication().getSharedPreferences(name, Context.MODE_PRIVATE);
    }

    public static void write(String key, Object value) {
        SharedPreferences sp = getSharedPreference();
        SharedPreferences.Editor editor = sp.edit();
        if (value instanceof String) {
            editor.putString(key, (String) value);
        } else if (value instanceof Integer) {
            editor.putInt(key, (Integer) value);
        } else if (value instanceof Float) {
            editor.putFloat(key, (Float) value);
        } else if (value instanceof Long) {
            editor.putLong(key, (Long) value);
        } else if (value instanceof Boolean) {
            editor.putBoolean(key, (Boolean) value);
        }
        editor.apply();
    }

    public static String read(String key, String defValue) {
        SharedPreferences sp = getSharedPreference();
        return sp.getString(key, defValue);
    }

    public static int read(String key, int defValue) {
        SharedPreferences sp = getSharedPreference();
        return sp.getInt(key, defValue);
    }

    public static boolean read(String key, boolean defValue) {
        SharedPreferences sp = getSharedPreference();
        return sp.getBoolean(key, defValue);
    }

}
