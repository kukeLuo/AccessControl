package com.brc.acctrl.bean;

import java.util.ArrayList;

public class ReqUploadModels<T> {
    public ArrayList<ReqUploadModelBean<T>> deviceModels;

    public ReqUploadModels() {
        deviceModels = new ArrayList<>();
    }
}
