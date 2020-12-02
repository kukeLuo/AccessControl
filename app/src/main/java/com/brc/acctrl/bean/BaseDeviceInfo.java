package com.brc.acctrl.bean;

import com.brc.acctrl.utils.NetworkUtil;

import java.util.ArrayList;

/**
 * @author zhengdan
 * @date 2019-07-29
 * @Description:
 */
public class BaseDeviceInfo {
    public ArrayList<DeviceModel> deviceModels;

    public class DeviceModel {
        public String iotId;
        public Property deviceProperties;

        public DeviceModel(String attr, long type) {
            iotId = NetworkUtil.ethernetMac();

            deviceProperties = new Property(attr, type);
        }
    }

    public class Property {
        public String iotId;
        public ArrayList<LevelProperty> deviceProperties;

        public Property(String attr, long type) {
            iotId = NetworkUtil.ethernetMac();
            deviceProperties = new ArrayList<>();

            deviceProperties.add(new LevelProperty(attr, type));
        }
    }

    public class LevelProperty {
        public String iotId;
        public String attribute;
        public long value;

        public LevelProperty(String attr, long type) {
            iotId = NetworkUtil.ethernetMac();
            attribute = attr;
            value = type;
        }
    }

    public BaseDeviceInfo(String attr, long type) {
        deviceModels = new ArrayList<>();
        deviceModels.add(new DeviceModel(attr, type));
    }
}
