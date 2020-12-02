package com.brc.acctrl.db;

import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.OnConflictStrategy;
import android.arch.persistence.room.Query;
import android.arch.persistence.room.Update;


import com.brc.acctrl.bean.AccessLog;

import java.util.List;

import io.reactivex.Single;

@Dao
public interface AccessLogDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertRecord(AccessLog accessRecord);

    // 因为目前没法读卡，所以只能检测人脸
    @Query("SELECT * FROM AccessLog WHERE type = 0 ORDER BY time DESC LIMIT 20 OFFSET :offsetCnt")
    Single<List<AccessLog>> fetchAllAccessLogFromDB(int offsetCnt);

 /*   @Query("SELECT * FROM AccessLog WHERE uploaded = 0 AND type = 0")
    Single<List<AccessLog>> fetchUnuploadRecordFromDB();*/

    @Query("DELETE FROM AccessLog WHERE time < :expireTime")
    void deleteExpireRecordsByTime(long expireTime);

    @Query("DELETE from AccessLog")
    void deleteAll();

    @Update
    void updateIdentivicationLog(List<AccessLog> records);
}
