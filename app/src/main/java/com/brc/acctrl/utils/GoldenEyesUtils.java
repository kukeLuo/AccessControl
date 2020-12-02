package com.brc.acctrl.utils;

import android.app.Activity;
import android.content.Intent;
import android.serialport.CRC16M;

import com.sanquan.boardcomm.DoorAccessCommManager;
import com.sanquan.boardcomm.DoorAccessCommManager.OnBoardCommReceiveListener;
import com.sanquan.boardcomm.GpioController;
import com.sanquan.boardcomm.ISerialEvent;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;

@SuppressWarnings({"unused", "WeakerAccess", "UnusedReturnValue",
        "SameParameterValue"})
public class GoldenEyesUtils {

    public static byte WACTHDOG_OPEN = 2;
    public static byte WACTHDOG_CLOSE = 3;

    // 保存内容到文件中
    static private String WriteStringToFile(File file, String str) {
        if (!file.exists()) {
            LogUtil.e("File not exist");
            return null;
        }
        OutputStream output = null;
        OutputStreamWriter outputWrite = null;
        PrintWriter print = null;

        try {
            output = new FileOutputStream(file);
            outputWrite = new OutputStreamWriter(output);
            print = new PrintWriter(outputWrite);

            print.print(str);
            print.flush();
            output.close();
            return str;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    // 采集摄像头是否需要翻转
    public static void setCameraMirr(Activity activity, boolean mirrback) {
        if (mirrback) {
            Intent intent = new Intent("android.intent.action.sendkey");
            intent.putExtra("keycode", 900);
            activity.sendBroadcast(intent);
        } else {
            Intent intent = new Intent("android.intent.action.sendkey");
            intent.putExtra("keycode", 901);
            activity.sendBroadcast(intent);
        }

    }

    //
    public static int setGoldenEyesOnTimePowerOnOf(boolean enable,
                                                   long onTime, long offTime) {
        if (doorAccessCommManagerInstance != null && isInitDoorAccessSuccess) {
            doorAccessCommManagerInstance.setMCUPowerOnOff(enable, onTime,
                    offTime);

            return 1;
        }
        return 0;
    }

    // 设置狗，WACTHDOG_OPEN 打开，每隔20s会喂狗，如果2min没喂狗，硬件直接重启
    // WACTHDOG_CLOSE 关闭
    public static int setGoldenEyesWatchdog(byte operation) {
        if (doorAccessCommManagerInstance != null && isInitDoorAccessSuccess) {
            byte[] cardId = CRC16M.getSendBuf("0000000000000000");
            doorAccessCommManagerInstance.sendWatchogCommand(cardId, operation);

            return 1;
        }
        return 0;
    }

    // 显示隐藏系统栏
    public static void hideSystemBar(Activity activity) {
        Intent intent0 = new Intent("com.sqvideo.hidestatusbar");
        activity.sendBroadcast(intent0);
    }

    public static void showSystemBar(Activity activity) {
        Intent intent0 = new Intent("com.sqvideo.showstatusbar");
        activity.sendBroadcast(intent0);
    }

    public static int setGoldenEyesTopLED(int value) {
        File ChannelInfoFile = new File("/sys/ledlight/ledlight");

        String status = WriteStringToFile(ChannelInfoFile,
                String.valueOf(value));

        if (status == null)
            return 0;

        LogUtil.e("setGoldenEyesTopLED=" + value);

        return 1;
    }

    // 打开顶部补光灯
    public static void setGoldenEyesTopLED71() {
        GpioController.getInstance().turnOnLed();
    }

    // 关闭补光灯
    public static void setGoldenEyesTopLED_OFF71() {
        GpioController.getInstance().turnOffLed();
    }

    // 设置底部红灯
    public static void setGoldenEyesStateRedLED71() {
        GpioController.getInstance().turnOnPort(1);
    }

    // 设置底部绿灯
    public static void setGoldenEyesStateGreedLED71() {
        GpioController.getInstance().turnOnPort(2);
    }

    // 关闭红灯
    public static void setGoldenEyesStateRedLED_OFF71() {
        GpioController.getInstance().turnOffPort(1);
    }

    // 关闭绿灯
    public static void setGoldenEyesStateGreedLED_OFF71() {
        GpioController.getInstance().turnOffPort(2);
    }

    // 关闭黄灯。猜测黄灯是通过红灯和绿灯同时打开来处理
    public static void setGoldenEyesStateLED_OFF71() {
        GpioController.getInstance().turnOffPort(1);
        GpioController.getInstance().turnOffPort(2);
    }

    public static int setGoldenEyesStateRedLED() {
        File ChannelInfoFile = new File("/sys/ledlight/ledlight1");

        String status = WriteStringToFile(ChannelInfoFile, String.valueOf(1));

        if (status == null)
            return 0;

        ChannelInfoFile = new File("/sys/ledlight/ledlight2");
        status = WriteStringToFile(ChannelInfoFile, String.valueOf(0));

        if (status == null)
            return 0;

        LogUtil.e("setGoldenEyesStateRedLED=OK");

        return 1;
    }

    public static int setGoldenEyesStateGreedLED() {
        File ChannelInfoFile = new File("/sys/ledlight/ledlight1");

        String status = WriteStringToFile(ChannelInfoFile, String.valueOf(0));

        if (status == null)
            return 0;

        ChannelInfoFile = new File("/sys/ledlight/ledlight2");
        status = WriteStringToFile(ChannelInfoFile, String.valueOf(1));

        if (status == null)
            return 0;

        LogUtil.e("setGoldenEyesStateRedLED=OK");

        return 1;
    }

    public static int setGoldenEyesStateYelloLED() {
        File ChannelInfoFile = new File("/sys/ledlight/ledlight1");

        String status = WriteStringToFile(ChannelInfoFile, String.valueOf(1));

        if (status == null)
            return 0;

        ChannelInfoFile = new File("/sys/ledlight/ledlight2");
        status = WriteStringToFile(ChannelInfoFile, String.valueOf(1));

        if (status == null)
            return 0;

        LogUtil.e("setGoldenEyesStateRedLED=OK");

        return 1;
    }

    public static int setGoldenEyesStateLED_OFF() {
        File ChannelInfoFile = new File("/sys/ledlight/ledlight1");

        String status = WriteStringToFile(ChannelInfoFile, String.valueOf(0));

        if (status == null)
            return 0;

        ChannelInfoFile = new File("/sys/ledlight/ledlight2");
        status = WriteStringToFile(ChannelInfoFile, String.valueOf(0));

        if (status == null)
            return 0;

        LogUtil.e("setGoldenEyesStateRedLED=OK");

        return 1;
    }

    public static boolean isInitDoorAccessSuccess = false;
    public static DoorAccessCommManager doorAccessCommManagerInstance = null;

    // 初始化StarRing门禁机门禁访问管理器. 如果收到ic卡信息的话则会回调ICCardResponseListener
    public static int initGoldenEyesDoorAccessCommManager(final ICCardResponseListener ICCardlistener) {
        doorAccessCommManagerInstance.setOnBoardCommRequest(new OnBoardCommReceiveListener() {
            @Override
            public void onBoardHeartbeat(byte[] bytes, byte[] bytes1,
                                         byte[] bytes2, byte[] bytes3,
                                         byte[] bytes4) {

            }

            @Override
            public void onBoardPostAccessRecord(byte[] bytes, byte[] bytes1,
                                                byte[] bytes2, byte[] bytes3,
                                                byte[] bytes4, byte b,
                                                byte b1) {

                String strHex = CRC16M.getBufHexStr(bytes);
                String type = CRC16M.getBufHexStr(new byte[]{b1});
                if (false == "0000000000000000".equals(strHex))
                    ICCardlistener.data(type, strHex);
            }

            @Override
            public void onBoardLockTongueState(byte b, byte[] bytes) {

            }

            @Override
            public void onBoardKeyDown(byte b, byte[] bytes) {

            }

            @Override
            public void onBoardPowerSet(byte[] bytes) {

            }

            @Override
            public void onActionSuccess() {

            }

            @Override
            public void onActionFailed() {

            }

            @Override
            public void onMcuUpgradeStart() {

            }

            @Override
            public void onMcuUpgradeProgress(int i) {

            }

            @Override
            public void onMcuUpgradeSuccess() {

            }

            @Override
            public void onMcuUpgradeFailed(int i) {

            }

            @Override
            public void onMcuWiegandData(byte[] bytes, byte b) {

            }

            @Override
            public void onMcuReadM1Card(byte[] bytes, byte b) {
                LogUtil.e("onMcuReadM1Card data = " + bytes.toString());
            }

            @Override
            public void onMcuReadM1CardBank(byte[] bytes) {
                LogUtil.e("onMcuReadM1CardBank data = " + bytes.toString());
            }

            @Override
            public void onMcuReadM1CardUid(byte b, byte[] bytes) {
                LogUtil.e("onMcuReadM1CardUid data = " + bytes.toString());
            }

            @Override
            public void onMcuBluetoothData(byte[] bytes) {

            }
        });


        return 1;
    }

    public static int releaseGoldenEyesDoorAccessCommManager() {
        if (doorAccessCommManagerInstance != null) {
            doorAccessCommManagerInstance.closeComm();
        }
        return 1;
    }

    // 打开门5s后关闭
    public static int setGoldenEyesAutoOpenCloseDoor() {
        if (doorAccessCommManagerInstance != null && isInitDoorAccessSuccess) {
            byte[] cardId = CRC16M.getSendBuf("0000000000000000");
            doorAccessCommManagerInstance.sendOpenCommand(cardId,
                    DoorAccessCommManager.ACCESS_MODE_CARD);

            LogUtil.d("GoldenEyesUtils:setGoldenEyesAutoOpenCloseDoor");

            return 1;
        }
        return 0;
    }

    public static int setGoldenEyesSendRelayPullTime(byte times) {
        if (doorAccessCommManagerInstance != null && isInitDoorAccessSuccess) {
            byte[] cardId = CRC16M.getSendBuf("0000000000000000");
            doorAccessCommManagerInstance.sendRelayPullTime(cardId, times);

            return 1;
        }
        return 0;
    }


    public static int setGoldenEyesCloseDoor() {
        LogUtil.e("DOOR CLOSE");
        //应为硬件的问题，需要反向
        if (doorAccessCommManagerInstance != null && isInitDoorAccessSuccess) {
            byte[] cardId = CRC16M.getSendBuf("0000000000000000");
            doorAccessCommManagerInstance.sendCloseCommand(cardId,
                    DoorAccessCommManager.ACCESS_MODE_CARD);

            return 1;
        }
        return 0;
    }


    public static int setGoldenEyesOpenDoor() {
        LogUtil.e("DOOR OPEN");
        //应为硬件的问题，需要反向
        if (doorAccessCommManagerInstance != null && isInitDoorAccessSuccess) {
            byte[] cardId = CRC16M.getSendBuf("0000000000000000");
            doorAccessCommManagerInstance.sendLongOpenCommand(cardId,
                    DoorAccessCommManager.ACCESS_MODE_CARD);

            return 1;
        }
        return 0;
    }


    public static void openCommPort(final CommPortListener listener) {
        if (isInitDoorAccessSuccess)
            return;

        doorAccessCommManagerInstance = DoorAccessCommManager.getInstance();

        doorAccessCommManagerInstance.openComm("/dev/ttyS3", 9600,
                new ISerialEvent() {

                    @Override
                    public void onCommRecv(String arg0) {
                        onICCardDataRecv(arg0);
                    }

                    @Override
                    public void onCommSend(String arg0) {
                        LogUtil.e("onCommSend data = " + arg0);

                    }

                    @Override
                    public void onSerialFail(String arg0) {
                        LogUtil.e("onSerialFail data = " + arg0);
                    }

                    @Override
                    public void onSerialSuccess() {
                        isInitDoorAccessSuccess = true;

                        if (listener != null) {
                            listener.Success();
                        }
                    }
                });
    }


    public static void onICCardDataRecv(String data) {
        LogUtil.e("onICCardDataRecv data = " + data);
    }

    public interface CommPortListener {
        void Success();
    }

    public interface ICCardResponseListener {
        void data(String type, String data);
    }

    public static int reboot() {
        byte operation = 7;
        if (doorAccessCommManagerInstance != null && isInitDoorAccessSuccess) {
            byte[] cardId = CRC16M.getSendBuf("0000000000000000");
            doorAccessCommManagerInstance.sendWatchogCommand(cardId, operation);

            return 1;
        }
        //*/
        return 0;
    }

}
