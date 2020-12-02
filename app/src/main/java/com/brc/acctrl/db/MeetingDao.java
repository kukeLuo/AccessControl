/**
 * 2 * Copyright (C), 2019, zmlearn
 * 3 * FileName: AccessHistoryDao
 * 4 * Author: zhd
 * 5 * Date: 2019/3/28 下午8:19
 * 6
 */
package com.brc.acctrl.db;

import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.OnConflictStrategy;
import android.arch.persistence.room.Query;

import com.brc.acctrl.bean.MeetingBean;

import java.util.ArrayList;
import java.util.List;

import io.reactivex.Single;

@Dao
public interface MeetingDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    long insertMeeting(MeetingBean meeting);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    long[]  insertBatchMeetings(ArrayList<MeetingBean> meetings);

    @Query("SELECT * FROM MeetingBean WHERE meetingId = :id")
    Single<MeetingBean> loadSingleMeeting(String id);

    // 只需要取会议结束时间比当前时间晚的最新的2个即可
    @Query("SELECT * FROM MeetingBean WHERE endTime > :curMillsTime ORDER BY endTime ASC")
    Single<List<MeetingBean>> fetchShowMeetingFromDB(long curMillsTime);

    @Query("SELECT meetingId FROM MeetingBean WHERE startTime = :startTime AND endTime = :endTime")
    Single<String> fetchMeetingIDByTime(long startTime, long endTime);

    @Query("DELETE FROM MeetingBean WHERE meetingId = :id")
    void deleteMeetingById(String id);

    @Query("DELETE from MeetingBean")
    void deleteAll();

}
