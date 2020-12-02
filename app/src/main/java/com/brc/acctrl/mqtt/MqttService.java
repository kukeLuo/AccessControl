package com.brc.acctrl.mqtt;

import android.app.Service;
import android.content.Intent;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.support.annotation.Nullable;
import android.text.TextUtils;

import com.brc.acctrl.MainApplication;
import com.brc.acctrl.bean.BaseDeviceInfo;
import com.brc.acctrl.bean.BaseRsp;
import com.brc.acctrl.bean.RspTopic;
import com.brc.acctrl.db.UserDatabase;
import com.brc.acctrl.events.RefreshEvents;
import com.brc.acctrl.retrofit.RetrofitConfig;
import com.brc.acctrl.utils.CommonUtil;
import com.brc.acctrl.utils.Constants;
import com.brc.acctrl.utils.FaceSDKUtil;
import com.brc.acctrl.utils.GoldenEyesUtils;
import com.brc.acctrl.utils.LogUtil;
import com.brc.acctrl.utils.NetworkUtil;
import com.brc.acctrl.utils.SPUtil;
import com.google.gson.reflect.TypeToken;

import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.greenrobot.eventbus.EventBus;

import java.lang.reflect.Type;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import io.reactivex.Observer;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;

public class MqttService extends Service {
    private static final String TAG = "MqttService";
    private MqttManager mqttManager;
    private static int mQos[] = new int[]{0, 1, 2};
    private static final String TOPIC_NAMES = "/brc/iot/api/device/%1$s/%2$s/services/invoke";
    private String topicDetailUrl;
    private static final int TOPIC_QOS = 1;
    // 本来想基于多线程处理，但是FaceSDKManager.getInstance().extractFeatureWithDetect该方法暂时不支持多线程调用，故还是使用单线程处理
    private ExecutorService connExecutor = Executors.newSingleThreadExecutor();
    //    private ExecutorService connExecutor = Executors.newFixedThreadPool(3);
    private Type topicType = new TypeToken<RspTopic>() {
    }.getType();

    private static final String ADD_PERSON = "addPerson";
    private static final String DEL_PERSON = "deletePerson";
    private static final String ADD_MEETING = "addMeeting";
    private static final String DEL_MEETING = "deleteMeeting";
    private static final String UPDATE_MEETING = "updateMeeting";
    private static final String REBOOT = "reboot";
    private static final String RESET_DEIVCE = "resetDevice";
    private static final String DEL_ALL_PERSON = "deleteAll";
    private static final String OPEN_DOOR = "openDoor";
    private static final String KEEP_OPEN_DOOR = "keepOpen";
    private static final String CLOSE_KEEP_OPEN_DOOR = "keepNormal";
    private static final String GET_PERSON = "getPerson";

    private Handler reConnectHandler = new Handler() {
        @Override
        public void dispatchMessage(Message msg) {
            super.dispatchMessage(msg);
            tryToReconnectMqtt();
        }
    };

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        LogUtil.e("MqttService onCreate");
        super.onCreate();
        topicDetailUrl = String.format(TOPIC_NAMES, Constants.MQTT_KEY,
                NetworkUtil.ethernetMac());
        if (mqttManager == null) {
            mqttManager = MqttManager.getInstance(getApplicationContext());
        }
        mqttManager.setCallback(mqttCallback);
    }

    private void connectToMqttService() {
        connExecutor.execute(new Runnable() {
            @Override
            public void run() {
                if (mqttManager != null) {
                    mqttManager.init();
                    boolean result = mqttManager.connectClient();
                    if (result) {
                        LogUtil.e("mqtt connectClient result: " + result);
                        mqttManager.subscribe(topicDetailUrl, TOPIC_QOS);
                    } else {
                        // 此时开始的时候mqtt server 有问题，需要尝试重连
                        reConnectHandler.sendEmptyMessageDelayed(0, 10000L);
                    }
                }
            }
        });
    }

    private MqttCallback mqttCallback = new MqttCallback() {
        @Override
        public void connectionLost(Throwable cause) {
            LogUtil.e("mqtt connectionLost cause: " + cause);
            tryToReconnectMqtt();
        }

        @Override
        public void messageArrived(String topic, MqttMessage message) throws Exception {
            try {
                RspTopic topicValue =
                        MainApplication.getAPPInstance().gson.fromJson(message.toString(),
                                topicType);
                if (topicValue == null) {
                    return;
                }

                String serviceId = topicValue.getServiceIdentifier();
                if (TextUtils.isEmpty(serviceId)) {
                    return;
                }

                LogUtil.i("MQTT:" + serviceId + "===" + message.toString());
                if (serviceId.equals(REBOOT) || serviceId.equals(RESET_DEIVCE)) {
                    connExecutor.execute(new Runnable() {
                        @Override
                        public void run() {
                            try {
                                Thread.sleep(3000L);
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                            CommonUtil.reboot();
                        }
                    });
                    return;
                } else if (serviceId.equals(DEL_ALL_PERSON)) {
                    connExecutor.execute(new Runnable() {
                        @Override
                        public void run() {
                            UserDatabase.getInstance().getAccessUserDao().deleteAll();
                            FaceSDKUtil.getInstance().deleteAllJpgFiles();
                            EventBus.getDefault().post(new RefreshEvents.RefreshFaceEvent(true));
                        }
                    });
                    return;
                } else if (serviceId.equals(OPEN_DOOR)) {
                    GoldenEyesUtils.setGoldenEyesAutoOpenCloseDoor();
                    return;
                } else if (serviceId.equals(CLOSE_KEEP_OPEN_DOOR)) {
                    SPUtil.getInstance().setValue(SPUtil.KEEP_DOOR_OPEN, false);
                    EventBus.getDefault().post(new RefreshEvents.KeepDoorOpenEvent(false));
                    GoldenEyesUtils.setGoldenEyesCloseDoor();

                    publishMsg2Server(new BaseDeviceInfo("doorType", 2));
                    return;
                } else if (serviceId.equals(KEEP_OPEN_DOOR)) {
                    SPUtil.getInstance().setValue(SPUtil.KEEP_DOOR_OPEN, true);
                    EventBus.getDefault().post(new RefreshEvents.KeepDoorOpenEvent(true));
                    GoldenEyesUtils.setGoldenEyesOpenDoor();

                    publishMsg2Server(new BaseDeviceInfo("doorType", 1));
                    return;
                } else if (serviceId.equals(GET_PERSON)) {
                    publishPersonCnt2Server();
                    return;
                }

                if (topicValue.getParamsList() != null && topicValue.getParamsList().size() > 0) {
//                    LogUtil.i("MQTT: ACTION DATA NUMBER = " + topicValue.getParamsList().size());
                    switch (serviceId) {
                        case ADD_PERSON:
                            MqttActions.changePersonActionNumber();
                            connExecutor.execute(MqttActions.processAddPerson(topicValue.getCallbackUrl(), topicValue.getParamsList()));
                            break;
                        case DEL_PERSON:
                            MqttActions.changePersonActionNumber();
                            connExecutor.execute(MqttActions.processDelPerson(topicValue.getParamsList()));
                            break;
                        case ADD_MEETING:
                            if (CommonUtil.bMeetingMode()) {
                                connExecutor.execute(MqttActions.processAddMeeting(topicValue.getParamsList()));
                            }
                            break;
                        case UPDATE_MEETING:
                            if (CommonUtil.bMeetingMode()) {
                                connExecutor.execute(MqttActions.processUpdateMeeting(topicValue.getParamsList()));
                            }
                            break;
                        case DEL_MEETING:
                            if (CommonUtil.bMeetingMode()) {
                                connExecutor.execute(MqttActions.processDelMeeting(topicValue.getParamsList()));
                            }
                            break;
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        @Override
        public void deliveryComplete(IMqttDeliveryToken token) {
            LogUtil.e("mqtt deliveryComplete token: " + token);
        }
    };

    private void tryToReconnectMqtt() {
        connExecutor.execute(new Runnable() {
            @Override
            public void run() {
                if (NetworkUtil.isNetworkAvailable(MqttService.this) && mqttManager != null&&NetworkUtil.ping2extranet()) {
                    boolean result = mqttManager.reconnect();
                    if (result) {
                        mqttManager.subscribe(topicDetailUrl, TOPIC_QOS);
                        return;
                    }
                }
                reConnectHandler.sendEmptyMessageDelayed(0, 10000L);
            }
        });
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        LogUtil.e("MqttService onStartCommand");
        connectToMqttService();
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        LogUtil.e("MqttService onDestroy");
        MqttManager.release();
        reConnectHandler.removeCallbacksAndMessages(null);
    }

    private void publishPersonCnt2Server() {
        connExecutor.execute(new Runnable() {
            @Override
            public void run() {
                int totalCnt = UserDatabase.getInstance().getAccessUserDao().cntDBUserNum();
                BaseDeviceInfo rsp = new BaseDeviceInfo("personCount", totalCnt);
                publishMsg2Server(rsp);
            }
        });
    }

    private void publishMsg2Server(BaseDeviceInfo req) {
        RetrofitConfig.createService().sendBase2Server(
                Constants.APP_KEY, Constants.APP_SECRET, req).subscribeOn(Schedulers.io())
                .subscribe(new Observer<BaseRsp<String>>() {
                    @Override
                    public void onSubscribe(Disposable d) {

                    }

                    @Override
                    public void onNext(BaseRsp<String> stringBaseRsp) {
                        LogUtil.trackLogDebug("publishMsg2Server rsp:" + stringBaseRsp.data);
                    }

                    @Override
                    public void onError(Throwable e) {

                    }

                    @Override
                    public void onComplete() {

                    }
                });
    }
}