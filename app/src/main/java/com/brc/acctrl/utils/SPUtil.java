package com.brc.acctrl.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.preference.PreferenceManager;

import com.brc.acctrl.MainApplication;

public class SPUtil {
    private Context context;
    private SharedPreferences sp = null;
    private Editor edit = null;


    public static String PASSWORD = "PASSWORD";
    public static String PASSWORD_OFFICE = "PASSWORD_OFFICE";
    public static String FACE_AUTH_CODE = "FACE_AUTH_CODE";
    public static String CONFIG_SERVER = "CONFIG_SERVER";
    public static String HAS_CONFIG_NAME = "HAS_CONFIG_NAME";
    public static String HAS_CONFIG_SERVER = "HAS_CONFIG_SERVER";
    public static String SUPPORT_LIVECHECK = "SUPPORT_LIVECHECK";
    public static String SUPPORT_SHOW_LIGHTS = "SUPPORT_SHOW_LIGHTS";
    public static String SUPPORT_SHOW_NAME= "SUPPORT_SHOW_NAME";
    public static String SUPPORT_REBOOT_TIMELY = "SUPPORT_REBOOT_TIMELY";
    public static String SUPPORT_SHOW_LIGHTS_STARTTIME = "SUPPORT_SHOW_LIGHTS_STARTTIME";
    public static String SUPPORT_SHOW_LIGHTS_ENDTIME = "SUPPORT_SHOW_LIGHTS_ENDTIME";
    public static String SUPPORT_SHOW_FILL_LIGHTS = "SUPPORT_SHOW_FILL_LIGHTS";
    //顶灯显示状态分为定时常开  定时识别开  常关   value 1 定时常开 2 定时识别开  3 常关
    public static String SHOW_FILL_LIGHTS_STATUS = "SHOW_FILL_LIGHTS_STATUS";
    public static String DOOR_OPEN_TIME = "DOOR_OPEN_TIME";
    public static String KEEP_DOOR_OPEN = "KEEP_DOOR_OPEN";

    public final static String DEFAULT_VOLUME_SIZE = "sp_volume_size";
    public final static String SP_MACHINE_MODE = "SP_MACHINE_MODE";
    public static final int ACCESS_TYPE_MEETING = 0;
    public static final int ACCESS_TYPE_BOSS = 1; // BOSS
    public static final int ACCESS_TYPE_OFFICE = 2;
    public static final int ACCESS_TYPE_SHUIPAI = 3;

    public final static String SP_MACHINE_INFO_TITLE = "SP_MACHINE_MODE_TITLE";
    public final static String SP_MACHINE_INFO_DESC = "SP_MACHINE_MODE_DESC";
    public final static String SP_MACHINE_INFO_DESC_MORE =
            "SP_MACHINE_MODE_DESC_MORE";

//    public final static String SP_FACE_GROUPID = "SP_FACE_GROUPID";


    /**
     * Create DefaultSharedPreferences
     *
     * @param context
     */
    public SPUtil(Context context) {
        this(context, PreferenceManager.getDefaultSharedPreferences(context));
    }

    /**
     * Create SharedPreferences by filename
     *
     * @param context
     * @param filename
     */
    public SPUtil(Context context, String filename) {
        this(context, context.getSharedPreferences(filename,
                Context.MODE_WORLD_WRITEABLE));
    }

    /**
     * Create SharedPreferences by SharedPreferences
     *
     * @param context
     * @param sp
     */
    public SPUtil(Context context, SharedPreferences sp) {
        this.context = context;
        this.sp = sp;
        edit = sp.edit();
    }

    static SPUtil mSpUtil = null;

    public static SPUtil getInstance() {
        if (mSpUtil == null) {
            mSpUtil = new SPUtil(MainApplication.getAPPInstance());
        }

        return mSpUtil;
    }

    // Boolean
    public void setValue(String key, boolean value) {
        edit.putBoolean(key, value);
        edit.commit();
    }

    public void setValue(int resKey, boolean value) {
        setValue(this.context.getString(resKey), value);
    }

    // Float
    public void setValue(String key, float value) {
        edit.putFloat(key, value);
        edit.commit();
    }

    public void setValue(int resKey, float value) {
        setValue(this.context.getString(resKey), value);
    }

    // Integer
    public void setValue(String key, int value) {
        edit.putInt(key, value);
        edit.commit();
    }

    public void setValue(int resKey, int value) {
        setValue(this.context.getString(resKey), value);
    }

    // Long
    public void setValue(String key, long value) {
        edit.putLong(key, value);
        edit.commit();
    }

    public void setValue(int resKey, long value) {
        setValue(this.context.getString(resKey), value);
    }

    // String
    public void setValue(String key, String value) {
        edit.putString(key, value);
        edit.commit();
    }

    public void setValue(int resKey, String value) {
        setValue(this.context.getString(resKey), value);
    }

    // Get

    // Boolean
    public boolean getValue(String key, boolean defaultValue) {
        return sp.getBoolean(key, defaultValue);
    }

    public boolean getValue(int resKey, boolean defaultValue) {
        return getValue(this.context.getString(resKey), defaultValue);
    }

    // Float
    public float getValue(String key, float defaultValue) {
        return sp.getFloat(key, defaultValue);
    }

    public float getValue(int resKey, float defaultValue) {
        return getValue(this.context.getString(resKey), defaultValue);
    }

    // Integer
    public int getValue(String key, int defaultValue) {
        return sp.getInt(key, defaultValue);
    }

    public int getValue(int resKey, int defaultValue) {
        return getValue(this.context.getString(resKey), defaultValue);
    }

    // Long
    public long getValue(String key, long defaultValue) {
        return sp.getLong(key, defaultValue);
    }

    public long getValue(int resKey, long defaultValue) {
        return getValue(this.context.getString(resKey), defaultValue);
    }

    // String
    public String getValue(String key, String defaultValue) {
        return sp.getString(key, defaultValue);
    }

    public String getValue(int resKey, String defaultValue) {
        return getValue(this.context.getString(resKey), defaultValue);
    }

    // Delete
    public void remove(String key) {
        edit.remove(key);
        edit.commit();
    }

    public void clear() {
        edit.clear();
        edit.commit();
    }
}
