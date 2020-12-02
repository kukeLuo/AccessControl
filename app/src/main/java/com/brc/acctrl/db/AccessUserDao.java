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

import com.brc.acctrl.bean.AccessUser;

import java.util.List;

import io.reactivex.Single;

@Dao
public interface AccessUserDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    long insertUser(AccessUser accessUser);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    long[] batchInsertUser(List<AccessUser> accessUser);

    @Query("SELECT * FROM AccessUser WHERE userId = :id AND groupId = :groupId")
    Single<AccessUser> fetchUserFromDB(String id, String groupId);

    @Query("SELECT * FROM AccessUser WHERE permissionId = :id")
    Single<AccessUser> fetchUserByUniKey(String id);

    @Query("SELECT * FROM AccessUser WHERE userId = :uid")
    Single<AccessUser> fetchUserByUserId(String uid);

    @Query("SELECT * FROM AccessUser WHERE validStartTime = :startTime AND validEndTime = :endTime")
    Single<List<AccessUser>> fetchMeetingUsersByMeetingTime(long startTime, long endTime);

    @Query("SELECT * FROM AccessUser WHERE meetingId = :id")
    Single<List<AccessUser>> fetchMeetingUsersByMeetingId(String id);

    @Query("SELECT * FROM AccessUser WHERE meetingId = :id AND username LIKE '%' || :name || '%'")
    Single<List<AccessUser>> fetchMeetingUsersByMeetingId(String id, String name);

    @Query("SELECT userId FROM AccessUser WHERE validEndTime == 0 OR validEndTime > :expireTime")
    List<String> fetchUnexpireUsers(long expireTime);

    @Query("SELECT userId FROM AccessUser")
    List<String> fetchUsersIdFromeDB();

    @Query("SELECT * FROM AccessUser")
    Single<List<AccessUser>> fetchUsersFromDB();

    @Query("SELECT * FROM AccessUser WHERE meetingId = :groupId")
    Single<List<AccessUser>> fetchUsersFromDBByGroup(String groupId);

    @Query("DELETE FROM AccessUser WHERE userId IN (:ids)")
    void deleteUsersByIds(long[] ids);

    @Query("DELETE FROM AccessUser WHERE userId = :id")
    void deleteUserById(String id);

    @Query("DELETE FROM AccessUser WHERE meetingId = :id")
    void deleteMeetingUserByMeetingId(String id);

    @Query("DELETE FROM AccessUser WHERE validEndTime > 0 AND validEndTime < :expireTime")
    void deleteExpireUsers(long expireTime);

    @Query("DELETE FROM AccessUser WHERE userId = :id AND groupId = :groupId")
    void deleteUserByIdAndGroupId(String id, String groupId);

    @Query("DELETE FROM AccessUser WHERE permissionId = :id")
    void deleteUserByUniKey(String id);

    @Query("DELETE FROM AccessUser")
    void deleteAll();

    @Query("SELECT count(*) from AccessUser")
    int cntDBUserNum();
}
