package com.brc.acctrl.bean;

import android.arch.persistence.room.Entity;
import android.arch.persistence.room.PrimaryKey;

@Entity
public class AccessHistory {
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

    private String userName;

    public String getUserAvatar() {
        return userAvatar;
    }

    public void setUserAvatar(String userAvatar) {
        this.userAvatar = userAvatar;
    }

    private String userAvatar;
    private long accessTime;
    private String cardNo;
    private int type;

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public long getAccessTime() {
        return accessTime;
    }

    public void setAccessTime(long accessTime) {
        this.accessTime = accessTime;
    }

    public String getCardNo() {
        return cardNo;
    }

    public void setCardNo(String cardNo) {
        this.cardNo = cardNo;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public String getUserFaceLocalPath() {
        return userFaceLocalPath;
    }

    public void setUserFaceLocalPath(String userPath) {
        this.userFaceLocalPath = userPath;
    }

    private String userFaceLocalPath;

    public int getUploaded() {
        return uploaded;
    }

    public void setUploaded(int uploaded) {
        this.uploaded = uploaded;
    }

    private int uploaded;

    private String userId;

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getGroupId() {
        return groupId;
    }

    public void setGroupId(String groupId) {
        this.groupId = groupId;
    }

    private String groupId;

    private String captureFaceurl;

    private int ratio;

    public String getCaptureFaceurl() {
        return captureFaceurl;
    }

    public void setCaptureFaceurl(String captureFaceurl) {
        this.captureFaceurl = captureFaceurl;
    }

    public int getRatio() {
        return ratio;
    }

    public void setRatio(int ratio) {
        this.ratio = ratio;
    }
}
