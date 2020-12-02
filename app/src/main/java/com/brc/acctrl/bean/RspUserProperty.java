package com.brc.acctrl.bean;

public class RspUserProperty {

    /**
     * personId : 123455
     * name : 姓名
     * faceBase64Image : Base64的人脸图片
     * faceUrl : 人脸图片完整地址
     * faceSize : 人脸大小
     * groupId : 分组ID
     * deviceId : 设备IotId
     * validStartTime : UTC时间
     * validEndTime : UTC时间
     */

    private String personId;
    private String name;
    private String faceBase64Image;
    private String faceUrl;
    private String faceSize;
    private String groupId;
    private String iotId;

    public String getPermissionId() {
        return permissionId;
    }

    public void setPermissionId(String permissionId) {
        this.permissionId = permissionId;
    }

    private String permissionId;
    private long validStartTime;
    private long validEndTime;

    public String getPersonId() {
        return personId;
    }

    public void setPersonId(String personId) {
        this.personId = personId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getFaceBase64Image() {
        return faceBase64Image;
    }

    public void setFaceBase64Image(String faceBase64Image) {
        this.faceBase64Image = faceBase64Image;
    }

    public String getFaceUrl() {
        return faceUrl;
    }

    public void setFaceUrl(String faceUrl) {
        this.faceUrl = faceUrl;
    }

    public String getFaceSize() {
        return faceSize;
    }

    public void setFaceSize(String faceSize) {
        this.faceSize = faceSize;
    }

    public String getGroupId() {
        return groupId;
    }

    public void setGroupId(String groupId) {
        this.groupId = groupId;
    }

    public String getIotId() {
        return iotId;
    }

    public void setIotId(String deviceId) {
        this.iotId = deviceId;
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
}
