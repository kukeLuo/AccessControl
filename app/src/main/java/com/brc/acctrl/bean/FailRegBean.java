package com.brc.acctrl.bean;

import com.brc.acctrl.utils.NetworkUtil;

/**
 * @author zhengdan
 * @date 2019-09-02
 * @Description:
 */
public class FailRegBean {
    public String iotId;
    public long time;
    public String matchUserName;
    public int matchRatio;
    public String matchUserId;

    public FailRegBean(AccessFail accessFail) {
        iotId = NetworkUtil.ethernetMac();
        time = accessFail.getTime();
        matchUserName = accessFail.getMatchUserName();
        matchRatio = accessFail.getMatchRatio();
        matchUserId = accessFail.getMatchUserId();
    }
}
