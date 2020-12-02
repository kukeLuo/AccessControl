package com.brc.acctrl.db;

import com.brc.acctrl.bean.AccessFail;
import com.brc.acctrl.bean.AccessHistory;
import com.brc.acctrl.bean.AccessLog;
import com.brc.acctrl.bean.AccessUser;
import com.brc.acctrl.bean.MeetingBean;
import com.brc.acctrl.events.RefreshEvents;
import com.brc.acctrl.utils.FaceSDKUtil;

import org.greenrobot.eventbus.EventBus;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import io.reactivex.SingleObserver;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;

public class DBStore {
    private Disposable disposable;
    private final Executor mDiskIOForDB;

    public DBStore() {
        mDiskIOForDB = Executors.newSingleThreadExecutor();
    }

    private static class DBStoreInstance {
        public static DBStore instance = new DBStore();
    }

    public static DBStore getInstance() {
        return DBStoreInstance.instance;
    }

    // access history
    public void insertAccessRecord(AccessHistory record) {
        mDiskIOForDB.execute(new Runnable() {
            @Override
            public void run() {
                RecordDatabase.getInstance().getAccessHistoryDao().insertRecord(record);
            }
        });
    }
    // access log
    public void insertAccessLog(AccessLog accessLog) {
        mDiskIOForDB.execute(new Runnable() {
            @Override
            public void run() {
                IdentificationLogDatabase.getInstance().getAccessLogDao().insertRecord(accessLog);
            }
        });
    }
    public void deleteAccessLogByTime(long expireTime) {
        mDiskIOForDB.execute(new Runnable() {
            @Override
            public void run() {
                IdentificationLogDatabase.getInstance().getAccessLogDao().deleteExpireRecordsByTime(expireTime);
            }
        });
    }

    public void deleteAllRecords() {
        mDiskIOForDB.execute(new Runnable() {
            @Override
            public void run() {
                RecordDatabase.getInstance().getAccessHistoryDao().deleteAll();
            }
        });
    }

    // access user
    public void insertAccessUser(AccessUser user) {
        mDiskIOForDB.execute(new Runnable() {
            @Override
            public void run() {
                UserDatabase.getInstance().getAccessUserDao().insertUser(user);
            }
        });
    }

    public void insertAccessUsers(ArrayList<AccessUser> users) {
        mDiskIOForDB.execute(new Runnable() {
            @Override
            public void run() {
                UserDatabase.getInstance().getAccessUserDao().batchInsertUser(users);
            }
        });
    }

    public void deleteAccessUser(String userId, String groupId) {
        mDiskIOForDB.execute(new Runnable() {
            @Override
            public void run() {
                UserDatabase.getInstance().getAccessUserDao().deleteUserByIdAndGroupId(userId, groupId);
            }
        });
    }

    public void deleteAccessUser(String userId) {
        mDiskIOForDB.execute(new Runnable() {
            @Override
            public void run() {
                UserDatabase.getInstance().getAccessUserDao().deleteUserById(userId);
                try {
                    Thread.sleep(300L);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                EventBus.getDefault().post(new RefreshEvents.RefreshFaceEvent(true));
            }
        });
    }

    public void deleteAllUsers() {
        mDiskIOForDB.execute(new Runnable() {
            @Override
            public void run() {
                UserDatabase.getInstance().getAccessUserDao().deleteAll();
            }
        });
    }

    public void deleteBatchAccessUser(long[] userIds) {
        mDiskIOForDB.execute(new Runnable() {
            @Override
            public void run() {
                UserDatabase.getInstance().getAccessUserDao().deleteUsersByIds(userIds);
            }
        });
    }

    public void updateAccessRecords(List<AccessHistory> histories) {
        mDiskIOForDB.execute(new Runnable() {
            @Override
            public void run() {
                RecordDatabase.getInstance().getAccessHistoryDao().updateRecords(histories);
            }
        });
    }

    public void deleteExpireRecord(long expireTime) {
        mDiskIOForDB.execute(new Runnable() {
            @Override
            public void run() {
                RecordDatabase.getInstance().getAccessHistoryDao().deleteExpireRecordsByTime(expireTime);
            }
        });
    }

    public void insertBatchMeetings(ArrayList<MeetingBean> meetings) {
        mDiskIOForDB.execute(new Runnable() {
            @Override
            public void run() {
                MeetingDatabase.getInstance().getMeetingDao().insertBatchMeetings(meetings);
            }
        });
    }

    public void insertMeeting(MeetingBean meeting) {
        mDiskIOForDB.execute(new Runnable() {
            @Override
            public void run() {
                MeetingDatabase.getInstance().getMeetingDao().insertMeeting(meeting);
            }
        });
    }

    public void deleteUnusedData() {
        mDiskIOForDB.execute(new Runnable() {
            @Override
            public void run() {
                // 删除全部会议，因为会议每天会重新定时下发
                MeetingDatabase.getInstance().getMeetingDao().deleteAll();

                // 删除有效期已过的人
//                UserDatabase.getInstance().getAccessUserDao().deleteExpireUsers(System.currentTimeMillis());

                // 获取没有删除的人，然后和文件夹中人脸比对即可
                List<String> remainUserIds = UserDatabase.getInstance().getAccessUserDao().
                        fetchUsersIdFromeDB();

                try {
                    File faceFolder = new File(FaceSDKUtil.getInstance().getFaceFolderPath());
                    File[] allFaceJpg = faceFolder.listFiles();
                    if (allFaceJpg != null && allFaceJpg.length > 0) {
                        for (File singleFile : allFaceJpg) {
                            if (singleFile.isFile()) {
                                String jpgUserId = singleFile.getName().replace(".jpg", "");
                                if (!remainUserIds.contains(jpgUserId)) {
                                    singleFile.delete();
                                }
                            }
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }

    public void insertRegFail(AccessFail regFail) {
        mDiskIOForDB.execute(new Runnable() {
            @Override
            public void run() {
                // 先计算记录数量，如果大于一定量(500)则提取出记录删除对应文件并删除记录
                // 实际很少遇到，因为验证错误就马上发送服务器了
                int totalCnt = FailRecordDatabase.getInstance().getAccessFailDao().cntRecordNum();
                FailRecordDatabase.getInstance().getAccessFailDao().insertRecord(regFail);
                if (totalCnt >= 500) {
                    FailRecordDatabase.getInstance().getAccessFailDao().fetchLimitFailRegFiles()
                            .subscribeOn(Schedulers.io())
                            .subscribe(new SingleObserver<List<AccessFail>>() {
                                @Override
                                public void onSubscribe(Disposable d) {

                                }

                                @Override
                                public void onSuccess(List<AccessFail> failHistories) {
                                    if (failHistories != null && failHistories.size() > 0) {
                                        for (AccessFail failReq : failHistories) {
                                            File jpgFile = new File(FaceSDKUtil.errJpgDirectory(), failReq.getErrJpgName());
                                            if (jpgFile.exists()) {
                                                jpgFile.delete();
                                            }

                                            try {
                                                Thread.sleep(300);
                                            } catch (InterruptedException e) {
                                                e.printStackTrace();
                                            }
                                        }

                                        FailRecordDatabase.getInstance().getAccessFailDao().deleteAllFailRegFirst100(failHistories);
                                    }
                                }

                                @Override
                                public void onError(Throwable e) {
                                    e.printStackTrace();
                                }
                            });

                }
            }
        });
    }

    public void delRegFail(AccessFail regFail) {
        mDiskIOForDB.execute(new Runnable() {
            @Override
            public void run() {
                FailRecordDatabase.getInstance().getAccessFailDao().deleteSingleRecord(regFail);
            }
        });
    }
}