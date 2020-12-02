package com.brc.acctrl.retrofit;

import com.brc.acctrl.bean.BaseDeviceInfo;
import com.brc.acctrl.bean.BaseFileInfo;
import com.brc.acctrl.bean.BaseRsp;
import com.brc.acctrl.bean.ReqFaceCallback;
import com.brc.acctrl.bean.ReqRegister;
import com.brc.acctrl.bean.ReqUploadModels;
import com.brc.acctrl.bean.RspImagePath;
import com.brc.acctrl.bean.RspRegister;
import com.brc.acctrl.bean.RspUpdate;
import com.brc.acctrl.bean.RspWeather;
import com.brc.acctrl.bean.UploadFailReq;
import com.brc.acctrl.bean.UploadFailRsp;
import com.brc.acctrl.bean.UploadLogReq;

import org.json.JSONObject;

import java.io.File;
import java.util.ArrayList;
import java.util.Map;

import io.reactivex.Observable;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.http.Body;
import retrofit2.http.FieldMap;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.Part;
import retrofit2.http.PartMap;
import retrofit2.http.Query;
import retrofit2.http.Url;

/**
 * Created by zhendan on 2016/1/14.
 */
public interface APIService {
    @GET("oss/api/app/version/list")
    Observable<RspUpdate> checkUpdate(@Header("appKey") String key,
                                      @Header("timeStamp") long timeStamp, @Header("nonce") String nonce,
                                      @Header("version") String version, @Header("sign") String sign, @Query(
            "appPackageName") String name);


    @POST("devicecenter/api/open/device/register")
    Observable<RspRegister> registerDevice(@Header("appKey") String appKey,
                                           @Header("appSecret") String secret,
                                           @Body ArrayList<ReqRegister> body);

    @POST("adaptor/api/cloud/0/device/event")
    Observable<BaseRsp<String>> uploadAccessRecord(@Header("appKey") String appKey,
                                           @Header("appSecret") String secret,
                                           @Body ReqUploadModels body);

    @POST()
    Observable<String> callbackFaceDelivery(@Url String url, @Header(
            "appKey") String appKey, @Header("appSecret") String secret,
                                            @Body ReqFaceCallback body);

    @POST("oss/api/oss/uploadBase64")
    Observable<UploadFailRsp> uploadFailImage(@Body UploadFailReq req);

    @POST("adaptor/api/cloud/0/device/info")
    Observable<BaseRsp<String>> sendBase2Server(@Header("appKey") String key,
                                          @Header("appSecret") String secret,
                                          @Body BaseDeviceInfo req);

    @GET("devicecenter/api/open/license/product/device/auth")
    Observable<BaseRsp<String>> fetchAuthCode(@Header("appKey") String key,
                                          @Header("appSecret") String secret,
                                          @Query("iotId") String strIotId,
                                          @Query("productKey") String pk);

    @GET("devicecenter/api/open/license/product/device/code/update")
    Observable<BaseRsp> checkCodeValidateStatus(@Header("appKey") String key,
                                              @Header("appSecret") String secret,
                                              @Query("iotId") String strIotId,
                                              @Query("productKey") String pk,
                                                @Query("validStatus") int status);

    @GET("oss/api/oss/weather/gps")
    Observable<BaseRsp<RspWeather>> fetchWeather();

    @POST("logcenter/api/app/log/addOrUpdateDeviceLog")
    Observable<RspRegister> uploadLogData(@Body UploadLogReq body);


    @POST("oss/api/oss/upload")
    Observable<BaseRsp<RspImagePath>> fetchUrlByFile(@Body MultipartBody file);
}
