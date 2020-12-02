package com.brc.acctrl.bean;

import android.arch.persistence.room.Entity;
import android.arch.persistence.room.PrimaryKey;


@Entity
public class AccessLog {
    public final static int TYPE_FACE = 0;
    public final static int TYPE_CARD = 1;

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

    private int type;
    private int comparison;

    public int getComparison() {
        return comparison;
    }

    public void setComparison(int comparison) {
        this.comparison = comparison;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public String getAvatarUrl() {
        return avatarUrl;
    }

    public void setAvatarUrl(String avatarUrl) {
        this.avatarUrl = avatarUrl;
    }
    public AccessLog() {

    }

    public AccessLog(String errFile, String errUser, String errUserId, int errRatio) {
        this.errJpgName = errFile;
        this.time = System.currentTimeMillis();
        this.matchUserName = errUser;
        this.matchRatio = errRatio;
        this.matchUserId = errUserId;
    }

    public AccessLog(String errFile, String avatarUrl,String errUser, String errUserId, int errRatio, long time,int type,int comparison) {
        this.errJpgName = errFile;
        this.avatarUrl=avatarUrl;
        this.time = time;
        this.matchUserName = errUser;
        this.matchRatio = errRatio;
        this.matchUserId = errUserId;
        this.type=type;
        this.comparison=comparison;
    }

}
