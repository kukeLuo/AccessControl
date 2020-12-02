package com.brc.acctrl.utils;

import com.brc.acctrl.bean.MeetParticipant;
import com.brc.acctrl.bean.MeetingBean;
import com.brc.acctrl.db.MeetingDatabase;
import com.brc.acctrl.events.RefreshEvents;

import org.greenrobot.eventbus.EventBus;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Set;

import io.reactivex.SingleObserver;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;

/**
 * @author zhengdan
 * @date 2019-07-22
 * @Description:
 */
public class MeetingUtils {
    private static MeetingUtils sdkInstance;
    // key: starttime-endtime value: meetingId
    public static HashMap<String, String> meetingTimeMap = new HashMap<>();

    public static MeetingUtils getInstance() {
        if (null == sdkInstance) {
            synchronized (FaceSDKUtil.class) {
                if (null == sdkInstance) {
                    sdkInstance = new MeetingUtils();
                }
            }
        }

        return sdkInstance;
    }

    public void addMeeting(MeetingBean meetingData) {
//        mockTestPerson(meetingData);

        // 1. 添加到数据库中
        MeetingDatabase.getInstance().getMeetingDao().insertMeeting(meetingData);

        // 2.因为数据信息有带人员信息，所以直接采用该信息即可.人脸有效时间
//        String meetingId = meetingData.getMeetingId();
//        for (MeetParticipant participant : meetingData.getParticipants()) {
//            FaceSDKUtil.getInstance().addMeetingUser(participant, meetingId,
//                    meetingData.getFaceIssuedTime(), meetingData.getFaceEndTime());
//        }
    }

    private void mockTestPerson(MeetingBean meetingData) {
        MeetParticipant tempParticipant = new MeetParticipant();
        tempParticipant.setPersonId("000");
        tempParticipant.setPersonName("xxzhd");
        tempParticipant.setPersonurl("https://brcimgs.oss-cn-beijing.aliyuncs.com/7945857f96b8e03c4ddac59d8a262c2a");

        MeetParticipant tempParticipant1 = new MeetParticipant();
        tempParticipant1.setPersonId("111");
        tempParticipant1.setPersonName("洋洋");
//        tempParticipant1.setPersonurl("https://brcimgs.oss-cn-beijing.aliyuncs.com/c0936283ed0997bf669816adead219df");
        tempParticipant1.setPersonurl("https://brcimgs.oss-cn-beijing.aliyuncs.com/9dd921de4e213098fb20d9930645997d");

        MeetParticipant tempParticipant2 = new MeetParticipant();
        tempParticipant2.setPersonId("222");
        tempParticipant2.setPersonName("fg");
        tempParticipant2.setPersonurl("https://brcimgs.oss-cn-beijing.aliyuncs.com/a24d7ae7bb109160562ccb89411aedca");

        MeetParticipant tempParticipant3 = new MeetParticipant();
        tempParticipant3.setPersonId("333");
        tempParticipant3.setPersonName("yw");
        tempParticipant3.setPersonurl("https://brcimgs.oss-cn-beijing.aliyuncs.com/d3c04603b439f7480cfa47124969998d");

        MeetParticipant tempParticipant4 = new MeetParticipant();
        tempParticipant4.setPersonId("444");
        tempParticipant4.setPersonName("kunlei");
        tempParticipant4.setPersonurl("https://brcimgs.oss-cn-beijing.aliyuncs.com/a378ac39edfd29e57828fc77077f11c9");

        MeetParticipant tempParticipant5 = new MeetParticipant();
        tempParticipant5.setPersonId("555");
        tempParticipant5.setPersonName("沈想");
        tempParticipant5.setPersonurl("https://brcimgs.oss-cn-beijing.aliyuncs.com/b8dadb89f4e392165c0da1a8b1103db5");

        MeetParticipant tempParticipant6 = new MeetParticipant();
        tempParticipant6.setPersonId("666");
        tempParticipant6.setPersonName("石书康");
        tempParticipant6.setPersonurl("https://brcimgs.oss-cn-beijing.aliyuncs.com/3152d3e7725035b96db6771ef687261f");

        if (meetingData.getParticipants() != null) {
            meetingData.getParticipants().add(tempParticipant);
            meetingData.getParticipants().add(tempParticipant1);
            meetingData.getParticipants().add(tempParticipant2);
            meetingData.getParticipants().add(tempParticipant3);
            meetingData.getParticipants().add(tempParticipant4);
            meetingData.getParticipants().add(tempParticipant5);
            meetingData.getParticipants().add(tempParticipant6);
        } else {
            ArrayList<MeetParticipant> participants = new ArrayList<>();
            participants.add(tempParticipant);
            participants.add(tempParticipant1);
            participants.add(tempParticipant2);
            participants.add(tempParticipant3);
            participants.add(tempParticipant4);
            participants.add(tempParticipant5);
            participants.add(tempParticipant6);
            meetingData.setParticipants(participants);
        }
    }

    // 对于更新而言，唯一需要关心的只是时间
    public void updateMeeting(MeetingBean meetingData) {
        // 1. 添加到数据库中
        MeetingDatabase.getInstance().getMeetingDao().insertMeeting(meetingData);

        // 2. 判别人员列表中是否有对应的人员，如果有则刷新对应的meetingId
        // 更新一般只会更新时间，所以这里还需要判别那些meetingId相同的数据
        // 这里不想先删除再添加是因为防止多次重复增加删除人员重复下载头像
        // 如果这里涉及到人员增删的话，则考虑直接删除然后全部重新下载
//        UserDatabase.getInstance().getAccessUserDao().fetchMeetingUsersByMeetingId(
//                meetingData.getMeetingId())
//                .subscribe(new SingleObserver<List<AccessUser>>() {
//
//                    @Override
//                    public void onSubscribe(Disposable d) {
//
//                    }
//
//                    @Override
//                    public void onSuccess(List<AccessUser> meetingUsers) {
//                        if (meetingUsers != null && meetingUsers.size() > 0) {
//                            for (AccessUser singleUser : meetingUsers) {
//                                singleUser.setValidStartTime(meetingData.getFaceIssuedTime());
//                                singleUser.setValidEndTime(meetingData.getFaceEndTime());
//                            }
//
//                            // 更新数据库
//                            UserDatabase.getInstance().getAccessUserDao().batchInsertUser(meetingUsers);
//                        }
//                    }
//
//                    @Override
//                    public void onError(Throwable e) {
//                        e.printStackTrace();
//                    }
//                });
    }

    public void delMeeting(String meetingId) {
        // 1. 删除数据库会议信息
        MeetingDatabase.getInstance().getMeetingDao().deleteMeetingById(meetingId);

        // 2. 判别人员列表中是否有对应的人员，如果有则删除对应的meetingId
//        UserDatabase.getInstance().getAccessUserDao().deleteMeetingUserByMeetingId(meetingId);
    }

    public void refreshShowMeetings(boolean isForceRefresh) {
        MeetingDatabase.getInstance().getMeetingDao().fetchShowMeetingFromDB(
                System.currentTimeMillis())
                .subscribeOn(Schedulers.io())
                .subscribe(new SingleObserver<List<MeetingBean>>() {

                    @Override
                    public void onSubscribe(Disposable d) {

                    }

                    @Override
                    public void onSuccess(List<MeetingBean> waitMeetings) {
                        meetingTimeMap.clear();
                        for (MeetingBean meeting : waitMeetings) {
                            String key = meeting.getFaceIssuedTime() + "-" + meeting.getFaceEndTime();
                            meetingTimeMap.put(key, meeting.getMeetingId());
                        }
                        EventBus.getDefault().post(new RefreshEvents.RefreshMeetingEvent(waitMeetings, isForceRefresh));
                    }

                    @Override
                    public void onError(Throwable e) {
                        e.printStackTrace();
                    }
                });
    }

    public String fetchCurrentMeetingId() {
        Set<String> keys = meetingTimeMap.keySet();
        long curTimeMills = System.currentTimeMillis();
        for (String singleStr : keys) {
            String[] timeSplitStrs = singleStr.split("-");
            long meetingStartTime = Long.parseLong(timeSplitStrs[0]);
            long meetingEndTime = Long.parseLong(timeSplitStrs[1]);
            if (curTimeMills >= meetingStartTime && curTimeMills < meetingEndTime) {
                return meetingTimeMap.get(singleStr);
            }
        }

        return "0";
    }
}
