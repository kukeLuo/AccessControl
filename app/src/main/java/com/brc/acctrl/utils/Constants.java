package com.brc.acctrl.utils;

import android.text.TextUtils;

import org.w3c.dom.Text;

public class Constants {
    public final static String APP_KEY = "742105b3b9c2c462";
    public final static String APP_SECRET = "bvi1fwg3glugheeslmggcqwrvmsfbiya";
//  public final static String DEEPLINT_CODE = "2ec48d19-b093-44cb-b95a-8072a6248216";
//    public final static String DEEPLINT_CODE = "a6a97b65-9013-4a5f-9eef-de2dbf3d008b";
    //public final static String DEEPLINT_CODE = "c9dc7b0a-6dec-42c3-a3bc-f14b757372ac";
//    public final static String DEEPLINT_CODE = "022a041c-0cc5-428e-bcaf-c23d5a3ea488";
    public final static String DEEPLINT_CODE = "c7d97fbf-c33e-4177-ad46-fc1eea0ac6d5";
    public final static String ADMIN_PWD = "brc12345";

    public final static String PRODUCT_KEY = "GLSTFACEGL3QZHAJI";
    public final static String PRODUCT_MODEL = "GL3Q-ZHAJI";
    public final static String PRODUCT_NAME = "DEEPGLINT-FaceMachine";
    public final static String MQTT_KEY = PRODUCT_KEY;

    /**
     * 定时识别开
     */
    public static final String INDENTIFY_ON="INDENTIFY_ON";
    public static final String TIMING_ON="TIMING_ON";
    public static final String OFTEN_OFF="OFTEN_OFF";

    public  static int THRESH_DETECT_FACE = 80;

    public static boolean backToSplashPage = false;
    public static boolean hasRegisterServerSuc = false;

    public static boolean shouldShowTopLight = false;
    public static int dayOpenLightMinuteStart = 0;
    public static int dayOpenLightMinuteEnd = 0;

    public static void initOpenTopLights() {
        // default 早8点到晚6点顶灯不开
        shouldShowTopLight = SPUtil.getInstance().getValue(SPUtil.SUPPORT_SHOW_LIGHTS, true);
        // 晚6点开始
        dayOpenLightMinuteStart = SPUtil.getInstance().getValue(SPUtil.SUPPORT_SHOW_LIGHTS_STARTTIME, 1080);
        // 早8点结束灯
        dayOpenLightMinuteEnd = SPUtil.getInstance().getValue(SPUtil.SUPPORT_SHOW_LIGHTS_ENDTIME, 480);
    }

    public static boolean checkTopLightStatus(int hour, int minute) {
        if (!shouldShowTopLight) {
            return false;
        }

        int checkTime = hour * 60 + minute;
        // 如果是类似 晚上10点到早上12点
        if (dayOpenLightMinuteStart > dayOpenLightMinuteEnd) {
            return checkTime >= dayOpenLightMinuteStart || checkTime <= dayOpenLightMinuteEnd;
        } else {
            // 如果是类似 早上10点到下午2点
            return checkTime >= dayOpenLightMinuteStart && checkTime <= dayOpenLightMinuteEnd;
        }
    }
}
