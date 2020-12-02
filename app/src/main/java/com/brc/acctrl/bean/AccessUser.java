package com.brc.acctrl.bean;

import android.arch.persistence.room.Entity;
import android.arch.persistence.room.PrimaryKey;
import android.support.annotation.NonNull;

@Entity
public class AccessUser implements Comparable<AccessUser> {
    private String username;

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    @NonNull
    private String permissionId;

    public String getPermissionId() {
        return permissionId;
    }

    public void setPermissionId(String key) {
        this.permissionId = key;
    }

    @PrimaryKey
    @NonNull
    private String userId;

    public String getGroupId() {
        return groupId;
    }

    public void setGroupId(String groupId) {
        this.groupId = groupId;
    }

    private String groupId;

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getAvatarUrl() {
        return avatarUrl;
    }

    public void setAvatarUrl(String avatar) {
        this.avatarUrl = avatar;
    }

    private String avatarUrl;

    public String getDeviceBmpPath() {
        return deviceBmpPath;
    }

    public void setDeviceBmpPath(String deviceBmpPath) {
        this.deviceBmpPath = deviceBmpPath;
    }

    private String deviceBmpPath;

    private byte[] featureBytes;
    public byte[] getFeatureBytes() {
        return featureBytes;
    }

    public void setFeatureBytes(byte[] bytes) {
        this.featureBytes = bytes;
    }

    public long getValidStartTime() {
        return validStartTime;
    }

    public void setValidStartTime(long validStartTime) {
        this.validStartTime = validStartTime;
    }

    public long getValidEndTime() {
        return validEndTime;
    }

    public void setValidEndTime(long validEndTime) {
        this.validEndTime = validEndTime;
    }

    private long validStartTime;
    private long validEndTime;

    public String getMeetingId() {
        return meetingId;
    }

    public void setMeetingId(String meetingId) {
        this.meetingId = meetingId;
    }

    private String meetingId;

    public String getNamCapLetters() {
        return namCapLetters;
    }

    public void setNamCapLetters(String namCapLetters) {
        this.namCapLetters = namCapLetters;
    }

    public String getNamePinyin() {
        return namePinyin;
    }

    public void setNamePinyin(String namePinyin) {
        this.namePinyin = namePinyin;
    }

    private String namCapLetters;
    private String namePinyin;

    @Override
    public int compareTo(AccessUser r) {
        if (namePinyin.equals(r.namePinyin)) {
            return 0;
        }
        boolean flag;
        if ((flag = namePinyin.startsWith("#")) ^ r.namePinyin.startsWith("#")) {
            return flag ? -1 : 1;
        }
        return namePinyin.compareTo(r.getNamePinyin());
    }
}
