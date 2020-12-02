package com.brc.acctrl.mqtt;

/**
 * Created by Icarus.Lee on 2018/9/28.
 * EventBus消息推送实体
 */
public class MessageEvent {

    public static final int REFRESH_DEVICES = 101;
    public static final int MQTT_MESSAGE_ARRIVED = 102;
    public static final int SET_PUSH_MESSAGE_READED = 109;
    /**
     * 消息类型
     */
    public int type;
    /**
     * 消息实体
     */
    public Object msg;

    public MessageEvent() {
    }

    public MessageEvent(int type) {
        this.type = type;
    }

    public MessageEvent(int type, Object msg) {
        this.type = type;
        this.msg = msg;
    }
}
