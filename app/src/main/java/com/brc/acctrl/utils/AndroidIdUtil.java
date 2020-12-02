package com.brc.acctrl.utils;

import android.content.Context;
import android.os.Build;
import android.provider.Settings;

/**
 * Created by yufen on 2018-6-21.
 */

public class AndroidIdUtil {
  private static volatile AndroidIdUtil mInstance = null;
  private String ANDROID_ID = null;
  private String SERIAL_NUMBER = null;
  private Context mContext;

  private AndroidIdUtil(Context context) {
    this.mContext = context;
    init();
  }

  public static AndroidIdUtil getInstance(Context context) {
    if (null == mInstance) {
      synchronized (AndroidIdUtil.class) {
        if (null == mInstance) {
          mInstance = new AndroidIdUtil(context);
        }
      }
    }
    return mInstance;
  }

  private void init() {
    initAndroidId();
    initSerialNumber();
  }


  private void initAndroidId() {
    ANDROID_ID =
        Settings.Secure.getString(mContext.getContentResolver(), Settings.Secure.ANDROID_ID);
  }

  private void initSerialNumber() {
    SERIAL_NUMBER = Build.SERIAL;
  }

  public String getUniqueId() {
    String id = ANDROID_ID + SERIAL_NUMBER;
    return MD5Util.getMD5(id);
  }
}
