package com.brc.acctrl.activity;

import android.text.TextUtils;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.brc.acctrl.R;
import com.brc.acctrl.listener.IServerContentChgListener;
import com.brc.acctrl.utils.CommonUtil;
import com.brc.acctrl.utils.Constants;
import com.brc.acctrl.utils.SPUtil;
import com.brc.acctrl.view.CommonEditView;

import butterknife.BindView;
import butterknife.OnClick;

public class ServerConfigOldActivity extends BaseActivity implements IServerContentChgListener {
    @BindView(R.id.tv_title)
    TextView tvTitle;
    @BindView(R.id.tv_save)
    TextView tvSave;
    @BindView(R.id.layout_head)
    RelativeLayout layoutHead;
    @BindView(R.id.layout_server_addr)
    CommonEditView layoutServerAddr;
    @BindView(R.id.layout_server_port)
    CommonEditView layoutServerPort;
    @BindView(R.id.layout_server_mqtt_addr)
    CommonEditView layoutServerMqttAddr;
    @BindView(R.id.layout_server_mqtt_port)
    CommonEditView layoutServerMqttPort;
    @BindView(R.id.text_mode_http)
    TextView textModeHttp;
    @BindView(R.id.text_mode_https)
    TextView textModeHttps;
    @BindView(R.id.text_mode_tcp)
    TextView textModeTcp;
    @BindView(R.id.text_mode_ssl)
    TextView textModeSsl;
    @BindView(R.id.tv_http_server)
    TextView tvHttpServer;
    @BindView(R.id.tv_mqtt_server)
    TextView tvMqttServer;

    public final static String SP_SERVER_TYPE = "SP_SERVER_TYPE";
    public final static String SP_SERVER_ADDR = "SP_SERVER_ADDR";
    public final static String SP_SERVER_PORT = "SP_SERVER_PORT";
    public final static String SP_SERVER_MQTT_ADDR = "SP_SERVER_MQTT_ADDR";
    public final static String SP_SERVER_MQTT_PORT = "SP_SERVER_MQTT_PORT";
    public final static String SP_SERVER_MQTT_TYPE = "SP_SERVER_MQTT_TYPE";

    private String serverType = "https";
    private String mqttServerType = "tcp";

    @Override
    public int getLayoutId() {
        return R.layout.activity_server_config_old;
    }

    @Override
    public void initViews() {
        layoutServerAddr.setContent(SPUtil.getInstance().getValue(SP_SERVER_ADDR, ""));
        layoutServerAddr.setEditInputServerAddr();
        layoutServerPort.setContent(SPUtil.getInstance().getValue(SP_SERVER_PORT, ""));
        layoutServerPort.setEditInputNumberType();
        layoutServerPort.setEditTextCommonWatcher();

        layoutServerMqttAddr.setContent(SPUtil.getInstance().getValue(SP_SERVER_MQTT_ADDR, ""));
        layoutServerMqttAddr.setEditInputServerAddr();
        layoutServerMqttPort.setContent(SPUtil.getInstance().getValue(SP_SERVER_MQTT_PORT, ""));
        layoutServerMqttPort.setEditInputNumberType();
        layoutServerMqttPort.setEditTextCommonWatcher();

        layoutServerPort.setContentChgListener(this);
        layoutServerAddr.setContentChgListener(this);
        layoutServerMqttAddr.setContentChgListener(this);
        layoutServerMqttPort.setContentChgListener(this);

//        layoutServerAddr.setContent("sitapiiot.brc.com.cn");
//        layoutServerMqttAddr.setContent("sit.iot.brc.com.cn");
//        layoutServerMqttPort.setContent("32002");

        serverType = SPUtil.getInstance().getValue(SP_SERVER_TYPE, "https");
        mqttServerType = SPUtil.getInstance().getValue(SP_SERVER_MQTT_TYPE,
                "tcp");
        resetTextMode();
    }

    private void resetTextMode() {
        textModeHttp.setActivated(serverType.equals("http"));
        textModeHttps.setActivated(!serverType.equals("http"));

        textModeTcp.setActivated(mqttServerType.equals("tcp"));
        textModeSsl.setActivated(!mqttServerType.equals("tcp"));
        resetServerContent();
    }

    private void resetServerContent() {
        String serverUrl = serverType;
        String httpServer = layoutServerAddr.getContent();
        String httpPort = layoutServerPort.getContent();
        if (TextUtils.isEmpty(httpPort)) {
            serverUrl = serverType + "://" + httpServer;
        } else {
            serverUrl = serverType + "://" + httpServer + ":" + httpPort;
        }
        tvHttpServer.setText(serverUrl);

        String mqttUrl = mqttServerType;
        String mqttServer = layoutServerMqttAddr.getContent();
        String mqttPort = layoutServerMqttPort.getContent();
        if (TextUtils.isEmpty(mqttPort)) {
            mqttUrl = mqttServerType + "://" + mqttServer;
        } else {
            mqttUrl = mqttServerType + "://" + mqttServer + ":" + mqttPort;
        }
        tvMqttServer.setText(mqttUrl);
    }

    private void checkAndSaveServer() {
        String serverAddr = layoutServerAddr.getContent();
        String serverPort = layoutServerPort.getContent();
        if (TextUtils.isEmpty(serverAddr) && TextUtils.isEmpty(serverPort)) {
            CommonUtil.showToast(ServerConfigOldActivity.this,
                    R.string.str_err_server_addr_port);
            return;
        }

        // should check server and save if suc
        SPUtil.getInstance().setValue(SP_SERVER_TYPE, serverType);
        SPUtil.getInstance().setValue(SP_SERVER_ADDR, serverAddr);
        SPUtil.getInstance().setValue(SP_SERVER_PORT, serverPort);

        String serverMqttAddr = layoutServerMqttAddr.getContent();
        String serverMqttPort = layoutServerMqttPort.getContent();
        if (TextUtils.isEmpty(serverMqttAddr) || TextUtils.isEmpty(serverMqttPort)) {
            CommonUtil.showToast(ServerConfigOldActivity.this,
                    R.string.str_err_server_mqtt_addr_port);
            return;
        }

        // should check server and save if suc
        SPUtil.getInstance().setValue(SP_SERVER_MQTT_TYPE, mqttServerType);
        SPUtil.getInstance().setValue(SP_SERVER_MQTT_ADDR, serverMqttAddr);
        SPUtil.getInstance().setValue(SP_SERVER_MQTT_PORT, serverMqttPort);

        Constants.backToSplashPage = true;
        finish();
    }

    @OnClick({R.id.img_back, R.id.text_mode_http, R.id.text_mode_https,
            R.id.text_mode_tcp, R.id.text_mode_ssl, R.id.tv_save})
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.img_back:
                finish();
                break;
            case R.id.text_mode_http:
                serverType = "http";
                resetTextMode();
                break;
            case R.id.text_mode_https:
                serverType = "https";
                resetTextMode();
                break;
            case R.id.text_mode_tcp:
                mqttServerType = "tcp";
                resetTextMode();
                break;
            case R.id.text_mode_ssl:
                mqttServerType = "ssl";
                resetTextMode();
                break;
            case R.id.tv_save:
                checkAndSaveServer();
                break;
        }
    }

    @Override
    public void realtimeRefreshText() {
        resetServerContent();
    }
}
