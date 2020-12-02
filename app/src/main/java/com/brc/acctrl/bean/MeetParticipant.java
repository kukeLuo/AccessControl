package com.brc.acctrl.bean;

/**
 * @author zhengdan
 * @date 2019-07-19
 * @Description:
 */
public class MeetParticipant {
    public String getPersonId() {
        return personId;
    }

    public void setPersonId(String personId) {
        this.personId = personId;
    }

    public String getPersonName() {
        return personName;
    }

    public void setPersonName(String personName) {
        this.personName = personName;
    }

    public String getPersonurl() {
        return personurl;
    }

    public void setPersonurl(String personurl) {
        this.personurl = personurl;
    }

    private String personId;
    private String personName;
    private String personurl;

}
