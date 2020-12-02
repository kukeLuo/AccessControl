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
import android.arch.persistence.room.Update;

import com.brc.acctrl.bean.AccessHistory;

import java.util.List;

import io.reactivex.Single;

@Dao
public interface AccessHistoryDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertRecord(AccessHistory accessRecord);

    // 因为目前没法读卡，所以只能检测人脸
    @Query("SELECT * FROM AccessHistory WHERE type = 0 ORDER BY accessTime DESC LIMIT 20 OFFSET :offsetCnt")
    Single<List<AccessHistory>> fetchAllRecordFromDB(int offsetCnt);

    @Query("SELECT * FROM AccessHistory WHERE uploaded = 0 AND type = 0")
    Single<List<AccessHistory>> fetchUnuploadRecordFromDB();

    @Query("DELETE FROM AccessHistory WHERE uploaded = 1 AND accessTime < :expireTime")
    void deleteExpireRecordsByTime(long expireTime);

    @Query("DELETE from AccessHistory")
    void deleteAll();

    @Update
    void updateRecords(List<AccessHistory> records);

}
