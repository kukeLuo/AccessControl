package com.brc.acctrl.db;

import android.arch.persistence.room.Database;
import android.arch.persistence.room.Room;
import android.arch.persistence.room.RoomDatabase;

import com.brc.acctrl.MainApplication;
import com.brc.acctrl.bean.AccessLog;

@Database(entities = {AccessLog.class}, version = 2, exportSchema = false)
public abstract class IdentificationLogDatabase extends RoomDatabase {
    private final static String DB_IDENTIFICATION = "accesslog.db";
    private static IdentificationLogDatabase INSTANCE;
    private static final Object sLock = new Object();

    public abstract AccessLogDao getAccessLogDao();

    public static IdentificationLogDatabase getInstance() {
        if (INSTANCE == null) {
            synchronized (sLock) {
                if (INSTANCE == null) {
                    INSTANCE =
                            Room.databaseBuilder(MainApplication.getAPPInstance().getApplicationContext(), IdentificationLogDatabase.class, DB_IDENTIFICATION).fallbackToDestructiveMigration().build();
                }
            }
        }
        return INSTANCE;
    }
}
