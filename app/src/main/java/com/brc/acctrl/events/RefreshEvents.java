package com.brc.acctrl.events;

import com.brc.acctrl.bean.MeetingBean;
import com.brc.acctrl.bean.WeatherShow;

import java.util.List;

/**
 * @author zhengdan
 * @date 2019-07-22
 * @Description:
 */
public class RefreshEvents {
    public static class CatchCardEvent {
    }

    public static class FinishPageEvent {
    }

    public static class RefreshFaceEvent {
        private boolean forceRefresh;
        public RefreshFaceEvent(boolean force) {
            this.forceRefresh = force;
        }

        public boolean shouleRefresh() {
            return this.forceRefresh;
        }
    }

    public static class RefreshLightEvent {
        private float lightValue;
        public RefreshLightEvent(float value) {
            this.lightValue = value;
        }

        public float getLightValue() {
            return this.lightValue;
        }
    }

    public static class RestartAPPEvent {
    }

    public static class ANREvent {
    }

    public static class RefreshMeetingEvent {
        private List<MeetingBean> meetings;
        private boolean forceRefresh = false;
        public RefreshMeetingEvent(List<MeetingBean> meetingDaos, boolean force) {
            meetings = meetingDaos;
            this.forceRefresh = force;
        }

        public List<MeetingBean> getMeetings() {
            return meetings;
        }

        public boolean isForceRefresh() {
            return this.forceRefresh;
        }
    }

    public static class WeatherEvent {
        private WeatherShow weatherShow;

        public WeatherEvent(WeatherShow weather) {
            this.weatherShow = weather;
        }

        public WeatherShow getWeather() {
            return this.weatherShow;
        }
    }

    public static class KeepDoorOpenEvent {
        private boolean bKeepOpen;

        public KeepDoorOpenEvent(boolean open) {
            this.bKeepOpen = open;
        }

        public boolean isKeepOpen() {
            return this.bKeepOpen;
        }
    }
}
