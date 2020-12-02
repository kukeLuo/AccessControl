package com.brc.acctrl.bean;

import com.brc.acctrl.utils.Constants;
import com.brc.acctrl.utils.NetworkUtil;

import java.util.ArrayList;

public class ReqUploadModelBean<T> {
    public String iotId;
    public String productKey;
    public String productModel;
    public ArrayList<T> deviceEvents;

    public ReqUploadModelBean() {
        iotId = NetworkUtil.ethernetMac();
        productKey = Constants.PRODUCT_KEY;
        productModel = Constants.PRODUCT_MODEL;
        deviceEvents = new ArrayList<>();
    }
}
