package com.brc.acctrl.utils;

import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.media.AudioManager;
import android.view.Gravity;
import android.view.View;
import android.view.inputmethod.InputMethodManager;

import com.brc.acctrl.MainApplication;
import com.brc.acctrl.R;
import com.brc.acctrl.activity.BaseActivity;
import com.brc.acctrl.bean.MeetingBean;
import com.brc.acctrl.view.MeetingInfoView;
import com.dovar.dtoast.DToast;

import java.io.PrintWriter;

public class CommonUtil {
    public static void showToast(Context mCtx, String msg) {
        if (mCtx == null) {
            return;
        }
        DToast.make(mCtx)
                .setText(R.id.tv_content_default, msg)
                .setGravity(Gravity.BOTTOM | Gravity.CENTER, 0, 30)
                .show();
    }

    public static void showToast(Context mCtx, int msgId) {
        if (mCtx == null) {
            return;
        }
        DToast.make(mCtx)
                .setText(R.id.tv_content_default, StringUtil.CpStrGet(msgId))
                .setGravity(Gravity.BOTTOM | Gravity.CENTER, 0, 30)
                .show();
    }

    // fetch current version code / name
    public static int getVersion() {
        int version = 0;
        try {
            PackageManager manager =
                    MainApplication.getAPPInstance().getApplicationContext()
                            .getPackageManager();
            PackageInfo info =
                    manager.getPackageInfo(MainApplication.getAPPInstance()
                            .getApplicationContext().getPackageName(), 0);
            version = info.versionCode;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return version;
    }

    public static String getVersionName() {
        String version = "";
        // 获取packagemanager的实例
        PackageManager packageManager =
                MainApplication.getAPPInstance().getPackageManager();
        // getPackageName()是你当前类的包名，0代表是获取版本信息
        PackageInfo packInfo = null;
        try {
            packInfo =
                    packageManager.getPackageInfo(MainApplication.getAPPInstance().getPackageName
                            (), 0);
            version = packInfo.versionName;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        return version;
    }

    public static boolean bMeetingMode() {
        int mode = SPUtil.getInstance().getValue(SPUtil.SP_MACHINE_MODE, SPUtil.ACCESS_TYPE_MEETING);
        return mode == SPUtil.ACCESS_TYPE_MEETING;
    }

    public static void initMeetingLayoutData(MeetingInfoView view, MeetingBean meeting) {
        view.setVisibility(View.VISIBLE);
        view.setMeetingAuthor(StringUtil.CpStrStrPara(R.string.str_meeting_author_title, meeting.getHostname()));
        view.setMeetingDesc(meeting.getTitle());

        view.setMeetingTime(StringUtil.CpStrStr2Para(R.string.str_meeting_time,
                DateTimeUtil.formatMeetingTime(meeting.getStartTime()),
                DateTimeUtil.formatMeetingTime(meeting.getEndTime())));
        view.setMeetingGroup(meeting.getGroupName());
    }

    public static void hideIMM(Activity mCtx) {
        try {
            if (mCtx.getCurrentFocus() != null && mCtx.getCurrentFocus().getWindowToken() != null) {
                ((InputMethodManager) mCtx.getSystemService(Context.INPUT_METHOD_SERVICE))
                        .hideSoftInputFromWindow(mCtx.getCurrentFocus().getWindowToken(),
                                InputMethodManager.HIDE_NOT_ALWAYS);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * root下静默安装
     * */
    public static boolean rootSlienceInstallApk(String apkPath){
        PrintWriter PrintWriter = null;
        Process process = null;
        try {
            process = Runtime.getRuntime().exec("su");
            PrintWriter = new PrintWriter(process.getOutputStream());
            PrintWriter.println("chmod 777 "+apkPath);
            PrintWriter.println("export LD_LIBRARY_PATH=/vendor/lib:/system/lib");
            PrintWriter.println("pm install -r "+apkPath);
            PrintWriter.flush();
            PrintWriter.close();
            int value = process.waitFor();
            return value == 0;
        } catch (Exception e) {
            e.printStackTrace();
        }finally{
            if(process!=null){
                process.destroy();
            }
        }
        return false;
    }

    /**
     * root下启动apk
     * */
    public static boolean rootStartApk(String packageName,String activityName){
        boolean isSuccess = false;
        String cmd = "am start -n " + packageName + "/" + activityName + " \n";
        Process process = null;
        try {
            process = Runtime.getRuntime().exec(cmd);
            int value = process.waitFor();
            return value == 0;
        } catch (Exception e) {
            e.printStackTrace();
        } finally{
            if(process!=null){
                process.destroy();
            }
        }
        return isSuccess;
    }

    public static void reboot(){
        try{
            //Process proc =Runtime.getRuntime().exec(new String[]{"su","-c","shutdown"});  //关机
            Process proc =Runtime.getRuntime().exec(new String[]{"su","-c","reboot -p"});  //关机重启
            proc.waitFor();
        }catch(Exception e){
            e.printStackTrace();
        }
    }
    public static void detectSound(AudioManager mAudioManager){
        int media = mAudioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
        int targetVoice=SPUtil.getInstance().getValue(SPUtil.DEFAULT_VOLUME_SIZE,5)/6;
        LogUtil.i("media:"+media+",,targetVoice:"+targetVoice+",,max:"+mAudioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC));
        if(media!=targetVoice){
            mAudioManager.setStreamVolume(AudioManager.STREAM_MUSIC, targetVoice, AudioManager.FLAG_SHOW_UI);
        }
    }
}
