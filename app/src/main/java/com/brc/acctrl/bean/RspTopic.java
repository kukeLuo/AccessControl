package com.brc.acctrl.bean;

import java.util.List;

public class RspTopic {
    public String getIotId() {
        return iotId;
    }

    public void setIotId(String iotId) {
        this.iotId = iotId;
    }

    public String getResourceId() {
        return resourceId;
    }

    public void setResourceId(String resourceId) {
        this.resourceId = resourceId;
    }

    public String getServiceIdentifier() {
        return serviceIdentifier;
    }

    public void setServiceIdentifier(String serviceIdentifier) {
        this.serviceIdentifier = serviceIdentifier;
    }

    public List<DeviceServiceParams> getParamsList() {
        return paramsList;
    }

    public void setParamsList(List<DeviceServiceParams> paramsList) {
        this.paramsList = paramsList;
    }

    // 设备ID
    private String iotId;
    // 资源ID
    private String resourceId;
    // 服务标识ID
    private String serviceIdentifier;

    public String getCallbackUrl() {
        return callbackUrl;
    }

    public void setCallbackUrl(String callbackUrl) {
        this.callbackUrl = callbackUrl;
    }

    // callback
    private String callbackUrl;

    // 服务参数
    private List<DeviceServiceParams> paramsList;
}