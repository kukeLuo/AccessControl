package com.brc.acctrl.bean;

public class ReqFaceCallback {
    public String permissionId;
    public int status;
    public String description;

    public ReqFaceCallback(String id) {
        this.permissionId = id;
        this.status = 1;
    }

    public ReqFaceCallback(String id, String description) {
        this.permissionId = id;
        this.description = description;
        this.status = 0;
    }
}
