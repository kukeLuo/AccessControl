package com.brc.acctrl.bean;

import com.brc.acctrl.utils.Constants;
import com.brc.acctrl.utils.NetworkUtil;

public class ReqUploadRecord {
    public String iotId;
    public String eventType;
    public String description;
    public String eventName;
    public String eventParams;
    public String alarmLevel;
    public String productKey;
    public String deviceName;
    public int idDelete;
    public long msgTime; // second

    public ReqUploadRecord() {
        iotId = NetworkUtil.ethernetMac();
        productKey = Constants.PRODUCT_KEY;
        deviceName = Constants.PRODUCT_NAME;
        eventType = "PERSON";
        eventName = "GLFaceEvent";
        alarmLevel = "1";
        idDelete = 0;
    }
}
