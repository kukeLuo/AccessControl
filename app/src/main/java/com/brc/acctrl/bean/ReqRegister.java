package com.brc.acctrl.bean;

import com.brc.acctrl.utils.CommonUtil;
import com.brc.acctrl.utils.Constants;
import com.brc.acctrl.utils.LogUtil;
import com.brc.acctrl.utils.NetworkUtil;

public class ReqRegister {
    public String iotId;
    public String productKey;
    public String productModel;
    public String productName;
    public String version;
    public int channel;
    public int status;

    public ReqRegister() {
        iotId = NetworkUtil.ethernetMac();
        LogUtil.e("MAC: " + iotId);
        productKey = Constants.PRODUCT_KEY;
        productModel = Constants.PRODUCT_MODEL;
        productName = Constants.PRODUCT_NAME;
        channel = 0;
        status = 1;
        version = CommonUtil.getVersionName();
    }
}
