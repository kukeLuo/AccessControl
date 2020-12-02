package com.brc.acctrl.convert;

import android.arch.persistence.room.TypeConverter;

import com.brc.acctrl.bean.MeetParticipant;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;

/**
 * @author zhengdan
 * @date 2019-07-19
 * @Description:
 */
public class MeetParticipantConverters {
    private static Type converType = new TypeToken<ArrayList<MeetParticipant>>() {}.getType();
    private static Gson convertGson = new Gson();

    @TypeConverter
    public static ArrayList<MeetParticipant> fromString(String value) {
        if (value == null) {
            return null;
        } else {
            return convertGson.fromJson(value, converType);
        }
    }

    @TypeConverter
    public static String listToString(ArrayList<MeetParticipant> data) {
        return data == null ? null : convertGson.toJson(data);
    }

}
