/**
 * 2 * Copyright (C), 2019, zmlearn
 * 3 * FileName: RecordDatabase
 * 4 * Author: zhd
 * 5 * Date: 2019/3/28 下午8:22
 * 6
 */
package com.brc.acctrl.db;

import android.arch.persistence.room.Database;
import android.arch.persistence.room.Room;
import android.arch.persistence.room.RoomDatabase;

import com.brc.acctrl.MainApplication;
import com.brc.acctrl.bean.AccessFail;

@Database(entities = {AccessFail.class}, version = 2, exportSchema = false)
public abstract class FailRecordDatabase extends RoomDatabase {
    private final static String DB_FAIL_RECORD = "accessfail.db";
    private static FailRecordDatabase INSTANCE;
    private static final Object sLock = new Object();

    public abstract AccessFailDao getAccessFailDao();

    public static FailRecordDatabase getInstance() {
        if (INSTANCE == null) {
            synchronized (sLock) {
                if (INSTANCE == null) {
                    INSTANCE =
                            Room.databaseBuilder(MainApplication.getAPPInstance().getApplicationContext(), FailRecordDatabase.class, DB_FAIL_RECORD).fallbackToDestructiveMigration().build();
                }
            }
        }
        return INSTANCE;
    }
}