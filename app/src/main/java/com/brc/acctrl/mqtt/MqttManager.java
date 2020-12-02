package com.brc.acctrl.mqtt;

import android.content.Context;
import android.util.Log;

import com.brc.acctrl.activity.ServerConfigActivity;
import com.brc.acctrl.utils.LogUtil;
import com.brc.acctrl.utils.NetworkUtil;
import com.brc.acctrl.utils.SPUtil;

import org.eclipse.paho.android.service.MqttAndroidClient;
import org.eclipse.paho.client.mqttv3.IMqttMessageListener;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;


/**
 * Created by yufen on 2018-6-20.
 */

public class MqttManager {
    private static final String TAG = "MqttManager";
    // 单例
    private static MqttManager mInstance = null;
    // 回调
    private MqttCallback mCallback;
    private MqttClient mClient;
    private MqttAndroidClient mAndroidClient;
    private MqttConnectOptions mConnectOptions;

    private static final String PROTOCOL_TCP = "tcp://";
    private static final String PROTOCOL_SSL = "ssl://";

    private static final String COLON = ":";
    private static final String USER_NAME = "admin";
    private static final String PASSWORD = "password";

    private Context mContext;

    private MqttManager(Context context) {
        this.mContext = context;
    }

    public static MqttManager getInstance(Context context) {
        if (null == mInstance) {
            synchronized (MqttManager.class) {
                if (null == mInstance) {
                    mInstance = new MqttManager(context);
                }
            }
        }
        return mInstance;
    }

    public void setCallback(MqttCallback callback) {
        this.mCallback = callback;
    }

    public void init() {
        initClient();
        initConnectOptions();
    }

    private void initConnectOptions() {
        if (mConnectOptions != null) {
            return;
        }
        mConnectOptions = new MqttConnectOptions();
        mConnectOptions.setCleanSession(false);// 清除缓存
        mConnectOptions.setAutomaticReconnect(false);
        mConnectOptions.setConnectionTimeout(10);// 设置超时时间，单位：秒
        mConnectOptions.setKeepAliveInterval(25);// 心跳包发送间隔，单位：秒
    }

    private boolean initClient() {
        boolean flag = false;
        if (mClient != null && mClient.isConnected()) {
            return true;
        }

        String uri = SPUtil.getInstance().getValue(ServerConfigActivity.SP_APP_HOST_TYPE, 1) == 1
                ? HttpConst.BASE_MQTT_PRODUCT : SPUtil.getInstance().getValue(ServerConfigActivity.SP_APP_HOST_TYPE, 1) == 2? HttpConst.BASE_MQTT_GATEWAY :HttpConst.BASE_MQTT_SIT;

        LogUtil.e("mqtt uri:" + uri);
//        final String clientId = AndroidIdUtil.getInstance(mContext).getUniqueId();
        String clientId = "GL-" + NetworkUtil.ethernetMac();
        LogUtil.e("MQTT CLIENTID = " + clientId);

        try {
            mClient = new MqttClient(uri, clientId, new MemoryPersistence());
            if (mCallback != null) {
                mClient.setCallback(mCallback);
            }
            flag = true;
        } catch (MqttException e) {
            e.printStackTrace();
            Log.e(TAG, "mqtt initClient exception: " + e);
        }
        return flag;
    }

    public boolean connectClient() {
        boolean flag = false;
        if (mClient != null && !mClient.isConnected()) {
            try {
                mClient.connect(mConnectOptions);
                flag = true;
            } catch (MqttException e) {
                e.printStackTrace();
                int errorCode = e.getReasonCode();
                if (32100 == errorCode) {
                    flag = true;
                }
                Log.e(TAG, "mqtt connectClient Exception: " + e);
            }
        } else {
            Log.e(TAG, "mqtt client is null OR client is connected");
        }
        return flag;
    }

    public boolean subscribe(String topicName, int qos,
                             IMqttMessageListener listener) {
        boolean flag = false;
        if (mClient != null && mClient.isConnected()) {
            // Subscribe to the requested topic
            // The QoS specified is the maximum level that messages will be
            // sent to the client at.
            // For instance if QoS 1 is specified, any messages originally
            // published at QoS 2 will
            // be downgraded to 1 when delivering to the client but messages
            // published at 1 and 0
            // will be received at the same level they were published at.
            try {
                if (listener != null) {
                    mClient.subscribe(topicName, qos, listener);
                } else {
                    mClient.subscribe(topicName, qos);
                }
                flag = true;
            } catch (MqttException e) {
                e.printStackTrace();
                Log.e(TAG, "mqtt subscribe Exception: " + e);
            }
        }
        return flag;
    }

    public boolean subscribe(String topicName, int qos) {
        return subscribe(topicName, qos, null);
    }

    public boolean subscribe(String[] topicName, int qos[],
                             IMqttMessageListener[] listeners) {
        boolean flag = false;
        if (mClient != null && mClient.isConnected()) {
            try {
                if (listeners != null) {
                    mClient.subscribe(topicName, qos, listeners);
                } else {
                    mClient.subscribe(topicName, qos);
                }
                flag = true;
            } catch (MqttException e) {
                Log.e(TAG, "mqtt subscribe MqttException: " + e);
            }
        }
        return flag;
    }

    public boolean subscribe(String[] topicName, int qos[]) {
        return subscribe(topicName, qos, null);
    }

    public void unsubscribe(String topicName) {
        if (mClient != null) {
            try {
                mClient.unsubscribe(topicName);
            } catch (MqttException e) {
            }
        }
    }

    public void unsubscribe(String[] topicName) {
        if (mClient != null) {
            try {
                mClient.unsubscribe(topicName);
            } catch (MqttException e) {
            }
        }
    }

    public void publish(String topicName, int qos, byte[] payload,
                        boolean isRetained) {
        if (mClient != null && mClient.isConnected()) {
            MqttMessage message = new MqttMessage();
            message.setPayload(payload);
            message.setQos(qos);
            message.setRetained(isRetained);

            try {
                mClient.publish(topicName, message);
            } catch (MqttException e) {
                Log.e(TAG, "mqtt publish MqttException: " + e);
            }
        }
    }

    public void publish(String topicName, int qos, String message,
                        boolean isRetained) {
        publish(topicName, qos, message.getBytes(), isRetained);
    }

    /**
     * 释放单例, 及其所引用的资源
     */
    public static void release() {
        if (mInstance != null) {
            mInstance.disConnectClient();
            mInstance = null;
        }
    }

    /**
     * 取消连接
     *
     * @throws MqttException
     */
    public void disConnectClient() {
        mClient.setCallback(null);
        if (mClient != null && mClient.isConnected()) {
            try {
                mClient.disconnect();
            } catch (MqttException e) {
                Log.e(TAG, "mqtt disConnectClient MqttException: " + e);
            }
        }
        mClient = null;
    }

    public boolean reconnect() {
        if (mClient != null && !mClient.isConnected()) {
            try {
                if (mCallback != null) {
                    mClient.setCallback(mCallback);
                }
                mClient.connect(mConnectOptions);
                return true;
            } catch (MqttException e) {
                e.printStackTrace();
                int errorCode = e.getReasonCode();
                // 32100 already connected
                if (32100 == errorCode) {
                    return true;
                }
            }
        } else {
            return true;
        }

        return false;
    }
}
