package com.brc.acctrl.mqtt;

import android.text.TextUtils;

import com.brc.acctrl.MainApplication;
import com.brc.acctrl.bean.DeviceServiceParams;
import com.brc.acctrl.bean.MeetingBean;
import com.brc.acctrl.bean.ReqFaceCallback;
import com.brc.acctrl.bean.RspUserProperty;
import com.brc.acctrl.db.UserDatabase;
import com.brc.acctrl.events.RefreshEvents;
import com.brc.acctrl.retrofit.RetrofitConfig;
import com.brc.acctrl.utils.Constants;
import com.brc.acctrl.utils.FaceSDKUtil;
import com.brc.acctrl.utils.LogUtil;
import com.brc.acctrl.utils.MeetingUtils;
import com.google.gson.reflect.TypeToken;

import org.greenrobot.eventbus.EventBus;

import java.lang.reflect.Type;
import java.util.List;

import io.reactivex.Observer;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;

public class MqttActions {
    public static volatile int DOOR_PERSON_ACTION_CNT = 0;
    private static Type userType = new TypeToken<RspUserProperty>() {
    }.getType();
    private static Type meetingType = new TypeToken<MeetingBean>() {
    }.getType();
    public static Object lockObj = new Object();

    // 个人图片保存的是以personId.jpg
    public static Runnable processAddPerson(String callbackUrl, List<DeviceServiceParams> paramsList) {
        return new Runnable() {
            @Override
            public void run() {
                // 判别validStartTime / validEndTime是否为0，为0表示长期有效的管理员
                // 有时间间隔的则为 1. 参加会议的人(会议模式) 2. 临时权限人员 **1可以暂时不管
                for (DeviceServiceParams property : paramsList) {
                    String valueStr = property.getPropertyValue();
                    RspUserProperty userInfo =
                            MainApplication.getAPPInstance().gson.fromJson(valueStr,
                                    userType);

                    // 暂时不允许会议人员添加
                    if (userInfo.getValidStartTime() > 0) {
                        callback2Server(callbackUrl, userInfo.getPermissionId(), "ValidStartTime不为0");
                        continue;
                    }

                    // 因为服务器不会下发会议人员，所以这里可以暂时不用关心
//                    long permitTime = userInfo.getValidEndTime() - userInfo.getValidEndTime();
//                    if (permitTime > 0 && permitTime < 43200000) {
//                        continue;
//                    }

                    if (TextUtils.isEmpty(userInfo.getPersonId())) {
                        callback2Server(callbackUrl, userInfo.getPermissionId(), "PersonId为空");
                        continue;
                    }

                    // userInfo.getFaceBase64Image() 因为目前不再使用base64
                    if (TextUtils.isEmpty(userInfo.getFaceUrl())) {
                        LogUtil.i("mqtt face base64 && url = null");
                        callback2Server(callbackUrl, userInfo.getPermissionId(), "人脸下载URL为空");
                        continue;
                    }

                    LogUtil.i("mqtt user name = " + userInfo.getName());
                    try {
                        String resultCode = FaceSDKUtil.getInstance().addNewUser(userInfo);
                        if (TextUtils.isEmpty(resultCode)) {
                            callback2Server(callbackUrl, userInfo.getPermissionId(), null);
                        } else {
                            callback2Server(callbackUrl, userInfo.getPermissionId(), resultCode);
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                }

                checkHasUnfinishedAction();
            }
        };
    }

    private static void callback2Server(String url, String permissionId, String err) {
        if (TextUtils.isEmpty(err)) {
            callbackServerGetFace(url, new ReqFaceCallback(permissionId));
        } else {
            callbackServerGetFace(url, new ReqFaceCallback(permissionId, err));
        }
    }

    private static void checkHasUnfinishedAction() {
        synchronized (lockObj) {
            MqttActions.DOOR_PERSON_ACTION_CNT--;
            if (MqttActions.DOOR_PERSON_ACTION_CNT > 0) {
                return;
            }
        }

        try {
            Thread.sleep(500);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            synchronized (lockObj) {
                if (MqttActions.DOOR_PERSON_ACTION_CNT == 0) {
                    EventBus.getDefault().post(new RefreshEvents.RefreshFaceEvent(true));
                }
            }
        }
    }

    public static void changePersonActionNumber() {
        synchronized (lockObj) {
            MqttActions.DOOR_PERSON_ACTION_CNT++;
        }
    }

    private static void callbackServerGetFace(String callbackUrl, ReqFaceCallback callbackBody) {
        if (TextUtils.isEmpty(callbackUrl)) {
            return;
        }

        // api 请求上传，成功后修改对应的id
        RetrofitConfig.createService().callbackFaceDelivery(callbackUrl,
                Constants.APP_KEY, Constants.APP_SECRET, callbackBody)
                .subscribeOn(Schedulers.io())
                .subscribe(new Observer<String>() {
                    @Override
                    public void onSubscribe(Disposable d) {

                    }

                    @Override
                    public void onNext(String o) {

                    }

                    @Override
                    public void onError(Throwable e) {

                    }

                    @Override
                    public void onComplete() {

                    }
                });
    }

    public static Runnable processDelPerson(List<DeviceServiceParams> paramsList) {
        return new Runnable() {
            @Override
            public void run() {
                for (DeviceServiceParams property : paramsList) {
                    String valueStr = property.getPropertyValue();
                    RspUserProperty userInfo =
                            MainApplication.getAPPInstance().gson.fromJson(valueStr,
                                    userType);

                    if (TextUtils.isEmpty(userInfo.getPersonId())) {
                        continue;
                    }

                    // 这里同步操作，而不是异步操作. 因为 userId 是唯一值，所以可以只通过该值来删除即可
                    UserDatabase.getInstance().getAccessUserDao().deleteUserById(userInfo.getPersonId());
                }

                checkHasUnfinishedAction();
            }
        };
    }

    public static Runnable processAddMeeting(List<DeviceServiceParams> paramsList) {
        return new Runnable() {
            @Override
            public void run() {
                for (DeviceServiceParams property : paramsList) {
                    String valueStr = property.getPropertyValue();
                    MeetingBean meetingData =
                            MainApplication.getAPPInstance().gson.fromJson(valueStr,
                                    meetingType);

                    if (TextUtils.isEmpty(meetingData.getMeetingId())) {
                        continue;
                    }

                    // 添加会议信息
                    MeetingUtils.getInstance().addMeeting(meetingData);
                }

                // 刷新会议信息,这个如果是有新插入会议，则 meetingId 肯定不同，所以可以不用强制
                MeetingUtils.getInstance().refreshShowMeetings(false);
            }
        };
    }

    public static Runnable processUpdateMeeting(List<DeviceServiceParams> paramsList) {
        return new Runnable() {
            @Override
            public void run() {
                for (DeviceServiceParams property : paramsList) {
                    String valueStr = property.getPropertyValue();
                    MeetingBean meetingData =
                            MainApplication.getAPPInstance().gson.fromJson(valueStr,
                                    meetingType);

                    if (TextUtils.isEmpty(meetingData.getMeetingId())) {
                        continue;
                    }

                    // 添加会议信息
                    MeetingUtils.getInstance().updateMeeting(meetingData);
                }

                // 可能当前会议的人修改要调整，所以需要强制刷新
                MeetingUtils.getInstance().refreshShowMeetings(true);
            }
        };
    }

    public static Runnable processDelMeeting(List<DeviceServiceParams> paramsList) {
        return new Runnable() {
            @Override
            public void run() {
                for (DeviceServiceParams property : paramsList) {
                    String valueStr = property.getPropertyValue();
                    MeetingBean meetingData =
                            MainApplication.getAPPInstance().gson.fromJson(valueStr,
                                    meetingType);

                    if (TextUtils.isEmpty(meetingData.getMeetingId())) {
                        continue;
                    }

                    // 删除会议信息
                    MeetingUtils.getInstance().delMeeting(meetingData.getMeetingId());
                }

                MeetingUtils.getInstance().refreshShowMeetings(false);
            }
        };
    }
}
