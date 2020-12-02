/**
 * 2 * Copyright (C), 2019, zmlearn
 * 3 * FileName: RecordDatabase
 * 4 * Author: zhd
 * 5 * Date: 2019/3/28 下午8:22
 * 6
 */
package com.brc.acctrl.db;

import android.arch.persistence.db.SupportSQLiteDatabase;
import android.arch.persistence.room.Database;
import android.arch.persistence.room.Room;
import android.arch.persistence.room.RoomDatabase;
import android.arch.persistence.room.migration.Migration;

import com.brc.acctrl.MainApplication;
import com.brc.acctrl.bean.AccessUser;

@Database(entities = {AccessUser.class}, version = 5, exportSchema = false)
public abstract class UserDatabase extends RoomDatabase {
    private final static String DB_USER = "accessuser.db";
    private static UserDatabase INSTANCE;
    private static final Object sLock = new Object();

    public abstract AccessUserDao getAccessUserDao();

    public static UserDatabase getInstance() {
        if (INSTANCE == null) {
            synchronized (sLock) {
                if (INSTANCE == null) {
                    INSTANCE =
                            Room.databaseBuilder(MainApplication.getAPPInstance().getApplicationContext(),
                                    UserDatabase.class, DB_USER).fallbackToDestructiveMigration().build();
                }
            }
        }
        return INSTANCE;
    }

    static final Migration MIGRATION_2_3 = new Migration(2, 3) {
        @Override
        public void migrate(SupportSQLiteDatabase database) {
            database.execSQL("ALTER TABLE AccessUser "
                    + " ADD COLUMN validStartTime INTEGER NOT NULL DEFAULT 0");
            database.execSQL("ALTER TABLE AccessUser "
                    + " ADD COLUMN validEndTime INTEGER NOT NULL DEFAULT 0");
            database.execSQL("ALTER TABLE AccessUser "
                    + " ADD COLUMN meetingId TEXT");
        }
    };
}