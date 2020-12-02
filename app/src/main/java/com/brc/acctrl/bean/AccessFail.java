package com.brc.acctrl.bean;

import android.arch.persistence.room.Entity;
import android.arch.persistence.room.PrimaryKey;

@Entity
public class AccessFail {
    public void setId(Long id) {
        this.id = id;
    }

    public Long getId() {
        return id;
    }

    @PrimaryKey(autoGenerate = true)
    private Long id;

    private long time;

    public long getTime() {
        return time;
    }

    public void setTime(long time) {
        this.time = time;
    }

    public String getMatchUserName() {
        return matchUserName;
    }

    public void setMatchUserName(String matchUserId) {
        this.matchUserName = matchUserId;
    }

    public int getMatchRatio() {
        return matchRatio;
    }

    public void setMatchRatio(int matchRatio) {
        this.matchRatio = matchRatio;
    }

    private String matchUserName;

    public String getMatchUserId() {
        return matchUserId;
    }

    public void setMatchUserId(String matchUserId) {
        this.matchUserId = matchUserId;
    }

    private String matchUserId;
    private int matchRatio;

    public String getErrJpgName() {
        return errJpgName;
    }

    public void setErrJpgName(String errJpgName) {
        this.errJpgName = errJpgName;
    }

    private String errJpgName;
    private String avatarUrl;


    public String getAvatarUrl() {
        return avatarUrl;
    }

    public void setAvatarUrl(String avatarUrl) {
        this.avatarUrl = avatarUrl;
    }

    public AccessFail() {

    }

    public AccessFail(String errFile, String errUser, String errUserId, int errRatio) {
        this.errJpgName = errFile;
        this.time = System.currentTimeMillis();
        this.matchUserName = errUser;
        this.matchRatio = errRatio;
        this.matchUserId = errUserId;
    }

    public AccessFail(String errFile,  String avatarUrl,String errUser, String errUserId, int errRatio, long time) {
        this.errJpgName = errFile;
        this.avatarUrl=avatarUrl;
        this.time = time;
        this.matchUserName = errUser;
        this.matchRatio = errRatio;
        this.matchUserId = errUserId;
    }
}
