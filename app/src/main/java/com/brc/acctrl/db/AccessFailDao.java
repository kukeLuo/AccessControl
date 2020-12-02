/**
 * 2 * Copyright (C), 2019, zmlearn
 * 3 * FileName: AccessHistoryDao
 * 4 * Author: zhd
 * 5 * Date: 2019/3/28 下午8:19
 * 6
 */
package com.brc.acctrl.db;

import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Delete;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.OnConflictStrategy;
import android.arch.persistence.room.Query;

import com.brc.acctrl.bean.AccessFail;

import java.util.List;

import io.reactivex.Single;

@Dao
public interface AccessFailDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertRecord(AccessFail failRecord);

    @Query("SELECT count(*) from AccessFail")
    int cntRecordNum();

    @Query("SELECT * FROM AccessFail WHERE time < :expireTime")
    Single<List<AccessFail>> fetchAllFailRegFiles(long expireTime);

    @Query("SELECT * FROM AccessFail LIMIT 100")
    Single<List<AccessFail>> fetchLimitFailRegFiles();

    @Query("DELETE FROM AccessFail")
    void deleteAllFailReg();

    @Delete
    void deleteAllFailRegFirst100(List<AccessFail> delData);

    @Delete
    void deleteSingleRecord(AccessFail delData);
}
