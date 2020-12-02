package com.brc.acctrl.bean;

/**
 * @author zhengdan
 * @date 2019-08-19
 * @Description:
 */
public class RspWeather {
    public NowData now;
    public DayData f1;

    public static class NowData {
        public String weather;
        public String weather_pic;
        public AqiDetail aqiDetail;
    }

    public static class AqiDetail {
        public int pm2_5;
    }

    public static class DayData {
        public int night_air_temperature;
        public int day_air_temperature;
    }
}
