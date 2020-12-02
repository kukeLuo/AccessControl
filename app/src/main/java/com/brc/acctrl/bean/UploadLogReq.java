package com.brc.acctrl.bean;

import android.text.TextUtils;

import com.brc.acctrl.MainApplication;
import com.brc.acctrl.utils.Base64Util;
import com.brc.acctrl.utils.Constants;
import com.brc.acctrl.utils.FaceSDKUtil;
import com.brc.acctrl.utils.NetworkUtil;

import java.io.File;
/**
 * @author zhengdan
 * @date 2019-09-02
 * @Description:
 */
public class UploadLogReq {
    public int alarm_type;
    public int level;
    public long log_time;
    public String log_title;
    public String log_content;
    public String productId;
    public String productName;
    public int type;
    public String image; // base64

    public UploadLogReq(AccessFail accessFail) {
        productId = NetworkUtil.ethernetMac();
        log_time = accessFail.getTime();
        if (!TextUtils.isEmpty(accessFail.getErrJpgName())) {
            File errFile = new File(FaceSDKUtil.errJpgDirectory(), accessFail.getErrJpgName());
            image = Base64Util.bmpFileToBase64(errFile.getAbsolutePath());
        }

        alarm_type = 2;
        log_title = accessFail.getMatchRatio() < 5 ? "人脸识别失败-底库没人" : "人脸识别失败-匹配率低";
        level = 1;
        productName = Constants.PRODUCT_NAME;
        log_content = MainApplication.getAPPInstance().gson.toJson(new FailRegBean(accessFail));
    }
}
