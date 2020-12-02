package com.brc.acctrl.utils;

import org.greenrobot.eventbus.EventBus;

/**
 * Created by zhendan on 6/9/2016.
 */

public class EventBusUtil {
    public static void registerEventBus(Object obj) {
        if (EventBus.getDefault().isRegistered(obj)) {
            return;
        }
        EventBus.getDefault().register(obj);
    }

    public static void unregisterEventBus(Object obj) {
        if (EventBus.getDefault().isRegistered(obj)) {
            EventBus.getDefault().unregister(obj);
        }
    }
}
