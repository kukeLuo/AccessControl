package com.brc.acctrl.bean;

import android.arch.persistence.room.Entity;
import android.arch.persistence.room.PrimaryKey;
import android.arch.persistence.room.TypeConverters;
import android.support.annotation.NonNull;

import com.brc.acctrl.convert.MeetParticipantConverters;

import java.util.ArrayList;

@Entity
public class MeetingBean {
    @PrimaryKey
    @NonNull
    private String meetingId;

    private String hostId;
    private String hostname;
    private String hosturl;
    private String title;
    private String grade;
    private String meetingRoom;
    private long startTime;
    private long endTime;
    private long faceIssuedTime;
    private long faceEndTime;
    private int status;
    private String groupId;
    private String groupName;

    @TypeConverters(MeetParticipantConverters.class)
    private ArrayList<MeetParticipant> participants;

    @NonNull
    public String getMeetingId() {
        return meetingId;
    }

    public void setMeetingId(@NonNull String meetingId) {
        this.meetingId = meetingId;
    }

    public String getHostId() {
        return hostId;
    }

    public void setHostId(String hostId) {
        this.hostId = hostId;
    }

    public String getHostname() {
        return hostname;
    }

    public void setHostname(String hostname) {
        this.hostname = hostname;
    }

    public String getHosturl() {
        return hosturl;
    }

    public void setHosturl(String hosturl) {
        this.hosturl = hosturl;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getGrade() {
        return grade;
    }

    public void setGrade(String grade) {
        this.grade = grade;
    }

    public String getMeetingRoom() {
        return meetingRoom;
    }

    public void setMeetingRoom(String meetingRoom) {
        this.meetingRoom = meetingRoom;
    }

    public long getStartTime() {
        return startTime;
    }

    public void setStartTime(long startTime) {
        this.startTime = startTime;
    }

    public long getEndTime() {
        return endTime;
    }

    public void setEndTime(long endTime) {
        this.endTime = endTime;
    }

    public long getFaceIssuedTime() {
        return faceIssuedTime;
    }

    public void setFaceIssuedTime(long faceIssuedTime) {
        this.faceIssuedTime = faceIssuedTime;
    }

    public long getFaceEndTime() {
        return faceEndTime;
    }

    public void setFaceEndTime(long faceEndTime) {
        this.faceEndTime = faceEndTime;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public String getGroupId() {
        return groupId;
    }

    public void setGroupId(String groupId) {
        this.groupId = groupId;
    }

    public String getGroupName() {
        return groupName;
    }

    public void setGroupName(String groupName) {
        this.groupName = groupName;
    }

    public ArrayList<MeetParticipant> getParticipants() {
        return participants;
    }

    public void setParticipants(ArrayList<MeetParticipant> participants) {
        this.participants = participants;
    }
}
