package com.brc.acctrl.activity;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;

import com.brc.acctrl.events.RefreshEvents;
import com.brc.acctrl.utils.GoldenEyesUtils;

import org.greenrobot.eventbus.EventBus;

import java.io.File;

/**
 * @author zhengdan
 * @date 2019-07-23
 * @Description:
 */
public abstract class BaseLightActivity extends BaseDateTimeActivity {
    private SensorManager sensorManager;
    private float lastLightValue = 0.f;
    private String lightTextFilePath;
    private int calCnt = 0;
    private float calValue = 0.f;
    private static final int CMP_CNT = 4;
    //    private static final float CMP_LIGHT_VALUE = CMP_CNT * 4.0f;
    private static final float CMP_LIGHT_VALUE_SINGLE = 13.0f;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//        checkSaveTextFile();

        //第一步：获取 SensorManager 的实例
        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        //第二步：获取 Sensor 传感器类型
        Sensor sensor = sensorManager.getDefaultSensor(Sensor.TYPE_LIGHT);
        //第四步：注册 SensorEventListener
        sensorManager.registerListener(listener, sensor, SensorManager.SENSOR_DELAY_NORMAL);

        saveHandler.sendEmptyMessageDelayed(0, 5000L);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        //传感器使用完毕，释放资源
        if (sensorManager != null) {
            sensorManager.unregisterListener(listener);
        }
        saveHandler.removeCallbacksAndMessages(null);
    }

    private void checkSaveTextFile() {
        File txtFile = new File(Environment.getExternalStorageDirectory(), "Lights.txt");
        if (!txtFile.exists()) {
            try {
                txtFile.createNewFile();
            } catch (Exception e) {

            }
        }

        lightTextFilePath = txtFile.getAbsolutePath();
    }

    private SensorEventListener listener = new SensorEventListener() {
        @Override
        public void onSensorChanged(SensorEvent event) {
            lastLightValue = event.values[0];
        }

        @Override
        public void onAccuracyChanged(Sensor sensor, int accuracy) {
        }
    };

    private Handler saveHandler = new Handler() {
        @Override
        public void dispatchMessage(Message msg) {
            super.dispatchMessage(msg);
            EventBus.getDefault().post(new RefreshEvents.RefreshLightEvent(lastLightValue));

            if (isSettingTopShow) {
                resetCalCnt();
                return;
            }

            if (lastLightValue < CMP_LIGHT_VALUE_SINGLE) {
                GoldenEyesUtils.setGoldenEyesTopLED71();
                resetCalCnt();
            } else {
                calCnt++;
                calValue += lastLightValue;
                if (calCnt >= CMP_CNT && calValue > calCnt * CMP_LIGHT_VALUE_SINGLE) {
                    GoldenEyesUtils.setGoldenEyesTopLED_OFF71();
                    resetCalCnt();
                }
            }

            saveHandler.sendEmptyMessageDelayed(0, 1000L);
//            FileUtils.appendMethodA(lightTextFilePath, lastLightValue);
        }
    };

    private void resetCalCnt() {
        calCnt = 0;
        calValue = 0.f;
    }
}
