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
import com.brc.acctrl.bean.MeetingBean;

@Database(entities = {MeetingBean.class}, version = 1, exportSchema = false)
public abstract class MeetingDatabase extends RoomDatabase {
    private final static String DB_MEETING = "meeting.db";
    private static MeetingDatabase INSTANCE;
    private static final Object sLock = new Object();

    public abstract MeetingDao getMeetingDao();

    public static MeetingDatabase getInstance() {
        if (INSTANCE == null) {
            synchronized (sLock) {
                if (INSTANCE == null) {
                    INSTANCE =
                            Room.databaseBuilder(MainApplication.getAPPInstance().getApplicationContext(), MeetingDatabase.class, DB_MEETING).fallbackToDestructiveMigration().build();
                }
            }
        }
        return INSTANCE;
    }
}