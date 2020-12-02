package com.brc.acctrl.activity;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.TextView;

import com.brc.acctrl.R;
import com.brc.acctrl.bean.ReqRegister;
import com.brc.acctrl.bean.RspRegister;
import com.brc.acctrl.mqtt.MqttService;
import com.brc.acctrl.retrofit.BaseObserver;
import com.brc.acctrl.retrofit.RetrofitConfig;
import com.brc.acctrl.retrofit.RxJavaAction;
import com.brc.acctrl.utils.Constants;
import com.brc.acctrl.utils.LogUtil;
import com.brc.acctrl.utils.NetworkUtil;
import com.brc.acctrl.utils.SPUtil;

import java.util.ArrayList;

/**
 * @author zhengdan
 * @date 2019-07-03
 * @Description:
 */
public abstract class BaseNetworkActivity extends BaseUpdateAPPActivity {
    private TextView errTextView;
    private NetworkChange networkChange;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        registerNetworkChg();
    }

    @Override
    protected void onResume() {
        super.onResume();

        if (errTextView == null) {
            errTextView = getErrTextView();
        }

        checkNetwork();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        unregisterReceiver(networkChange);
    }

    private void checkNetwork() {
        LogUtil.e("checkNetwork");
        if (errTextView == null) {
            errTextView = getErrTextView();
        }

        boolean isNetworkConn = NetworkUtil.isNetworkAvailable(this);
        if (isNetworkConn) {
            if (NetworkUtil.networkStatus != NetworkUtil.NETWORK_OK) {
                // check to connect server
                connectServer();
            } else {
                if (errTextView != null) {
                    errTextView.setVisibility(View.GONE);
                }
            }
        } else {
            NetworkUtil.networkStatus = NetworkUtil.NETWORK_NO_DISCONNECT;
            setErrTextContent(R.string.str_error_no_network);
        }
    }

    private void registerNetworkChg() {
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(ConnectivityManager.CONNECTIVITY_ACTION);
        networkChange = new NetworkChange();
        registerReceiver(networkChange, intentFilter);
    }

    private void connectServer() {
        // 一开始判别是否有有线网络，但是还是暂时不用。以防后续需要使用无线
        ArrayList<ReqRegister> bodyData = new ArrayList<>();
        bodyData.add(new ReqRegister());
        RetrofitConfig.createService().registerDevice(
                Constants.APP_KEY, Constants.APP_SECRET, bodyData).compose(RxJavaAction.<RspRegister>setThread())
                .subscribe(new BaseObserver<RspRegister>(this, true, true) {
                    @Override
                    public void onSuccess(RspRegister responseData) {
                        if (responseData.status == 200) {
                            Constants.hasRegisterServerSuc = true;
                            // start mqtt service
                            Intent intent = new Intent(getApplicationContext(), MqttService.class);
                            startService(intent);

                            if (errTextView != null) {
                                errTextView.setVisibility(View.GONE);
                            }
                            NetworkUtil.networkStatus = NetworkUtil.NETWORK_OK;

                            // check face code
                            if (TextUtils.isEmpty(SPUtil.getInstance().getValue(SPUtil.FACE_AUTH_CODE, ""))) {
                                checkAuthCode();
                            }
                        } else {
                            NetworkUtil.networkStatus = NetworkUtil.NETWORK_REQUEST_ERROR;
                            Constants.hasRegisterServerSuc = false;
                            setErrTextContent(responseData.message);
                        }
                    }

                    @Override
                    protected void onErrorAction(String msg) {
                        Constants.hasRegisterServerSuc = false;
                        NetworkUtil.networkStatus = NetworkUtil.NETWORK_SERVER_ERROR;
                        setErrTextContent(R.string.str_error_network_server);
                    }
                });
    }

    @Override
    public void tryToRegisterServer() {
        connectServer();
    }

    public abstract TextView getErrTextView();

    public class NetworkChange extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals(ConnectivityManager.CONNECTIVITY_ACTION)) {
                LogUtil.e("NETWORK_CONNECT_CHANGE");
                if (NetworkUtil.isNetworkAvailable(context)) {
                    checkNetwork();
                } else {
                    NetworkUtil.networkStatus = NetworkUtil.NETWORK_NO_DISCONNECT;

                    try {
                        Intent mqttIntent = new Intent(getApplicationContext(), MqttService.class);
                        stopService(mqttIntent);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                    setErrTextContent(R.string.str_error_no_network);
                }
            }
        }
    }

    public void setErrTextContent(final int strId) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (errTextView != null) {
                    if (strId == 0) {
                        errTextView.setVisibility(View.GONE);
                    } else {
                        errTextView.setVisibility(View.VISIBLE);
                        errTextView.setText(strId);
                    }
                }
            }
        });
    }

    public void setErrTextContent(final String strContent) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (errTextView != null) {
                    if (TextUtils.isEmpty(strContent)) {
                        errTextView.setVisibility(View.GONE);
                    } else {
                        errTextView.setVisibility(View.VISIBLE);
                        errTextView.setText(strContent);
                    }
                }
            }
        });
    }

    protected abstract void checkAuthCode();
}
