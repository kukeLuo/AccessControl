package com.brc.acctrl.bean;

import com.brc.acctrl.utils.NetworkUtil;

/**
 * @author zhengdan
 * @date 2019-07-24
 * @Description:
 */
public class UploadFailReq {
    public String base64String;
    public String type;

    public UploadFailReq(String bmpString) {
        base64String = bmpString;
        type = NetworkUtil.ethernetMac();
    }
}
