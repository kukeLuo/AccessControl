package com.brc.acctrl.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.brc.acctrl.utils.CommonUtil;
import com.brc.acctrl.utils.LogUtil;

public class OpenApkReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        String packageName = intent.getDataString();
        LogUtil.e("OpenApkReceiver onReceive: " + action);
        switch (action) {
            case Intent.ACTION_PACKAGE_ADDED:
                Log.e("安装", "安装了:" + packageName + "包名的程序");
                startApp(context);
                break;
            case Intent.ACTION_PACKAGE_REMOVED:
                System.out.println("卸载了:" + packageName + "包名的程序");
                break;
            case Intent.ACTION_PACKAGE_REPLACED:
                Log.e("安装", "包升级了一个安装，重新启动此程序....");
                CommonUtil.showToast(context, "更新成功");
                startApp(context);
                break;
        }
    }

    /**
     * 监测到升级后执行app的启动
     */
    public void startApp(Context context) {
        // 根据包名打开安装的apk
        Intent resolveIntent = context.getPackageManager().getLaunchIntentForPackage("com.brc.acctrl");
        context.startActivity(resolveIntent);

        /*// 打开自身 一般用于软件升级
        Intent intent = new Intent(context, MainApplication.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(intent);*/
    }

}
