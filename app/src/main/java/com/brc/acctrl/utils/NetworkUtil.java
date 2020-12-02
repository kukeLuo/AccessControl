package com.brc.acctrl.utils;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Handler;
import android.text.TextUtils;
import android.util.Log;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.MalformedURLException;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.net.URL;
import java.util.Enumeration;

import static com.brc.acctrl.retrofit.RetrofitConfig.BASE_HOST;

public class NetworkUtil {
    public final static int NETWORK_OK = 1;
    public final static int NETWORK_NO_DISCONNECT = 2;
    // 错误情况 1.配置错误 2.服务器异常
    public final static int NETWORK_SERVER_ERROR = 3;
    public final static int NETWORK_REQUEST_ERROR = 4;
    public static int networkStatus = 0;

    private static String MAC_ETHERNET = "";
    private static Handler mHandler = new Handler();

    // for ethernet mac
    public static String ethernetMac() {
        if (TextUtils.isEmpty(MAC_ETHERNET)) {
            try {
                MAC_ETHERNET =
                        loadReadFile("/sys/class/net/eth0/address").toUpperCase().substring(0, 12);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        return MAC_ETHERNET;
    }

    private static String loadReadFile(String path) {
        StringBuffer sb = new StringBuffer();
        try {
            BufferedReader reader = new BufferedReader(new FileReader(path));
            char[] buf = new char[8192];
            int len = 0;
            while ((len = reader.read(buf)) != -1) {
                String str = String.valueOf(buf, 0, len);
                sb.append(str);
            }
            reader.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return sb.toString().replaceAll(":", "");
    }

    // fetch current IP address
    public static String getEthernetIp() {
        try {
            // 获取本地设备的所有网络接口
            Enumeration<NetworkInterface> enumerationNi = NetworkInterface
                    .getNetworkInterfaces();
            while (enumerationNi.hasMoreElements()) {
                NetworkInterface networkInterface = enumerationNi.nextElement();
                String interfaceName = networkInterface.getDisplayName();
                Log.i("tag", "网络名字" + interfaceName);

                // 如果是有限网卡
                if (interfaceName.equals("eth0")) {
                    Enumeration<InetAddress> enumIpAddr = networkInterface
                            .getInetAddresses();

                    while (enumIpAddr.hasMoreElements()) {
                        // 返回枚举集合中的下一个IP地址信息
                        InetAddress inetAddress = enumIpAddr.nextElement();
                        // 不是回环地址，并且是ipv4的地址
                        if (!inetAddress.isLoopbackAddress()
                                && inetAddress instanceof Inet4Address) {
                            Log.i("tag", inetAddress.getHostAddress() + "   ");

                            return inetAddress.getHostAddress();
                        }
                    }
                }
            }

        } catch (SocketException e) {
            e.printStackTrace();
        }
        return "";
    }

    public static boolean isEthernetConnected(Context context) {
        if (context != null) {
            ConnectivityManager mConnectivityManager =
                    (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo mInternetNetWorkInfo =
                    mConnectivityManager.getNetworkInfo(ConnectivityManager.TYPE_ETHERNET);
            if (mInternetNetWorkInfo == null) {
                return false;
            } else {
                return mInternetNetWorkInfo.isConnected() && mInternetNetWorkInfo.isAvailable();
            }
        }
        return false;
    }

    /**
     * 检测当的网络（WLAN、3G/2G）状态
     * @param context Context
     * @return true 表示网络可用
     */
    public static boolean isNetworkAvailable(Context context) {
        ConnectivityManager connectivity = (ConnectivityManager) context
                .getSystemService(Context.CONNECTIVITY_SERVICE);
        if (connectivity != null) {
            NetworkInfo info = connectivity.getActiveNetworkInfo();
            if (info != null && info.isConnected()) {
                // 当前网络是连接的
                if (info.getState() == NetworkInfo.State.CONNECTED) {
                    // 当前所连接的网络可用
                   return true;
                }
            }
        }
        return false;
    }

    //判断是否有外网连接（普通方法不能判断外网的网络是否连接，比如连接上局域网）
    public static final boolean ping2extranet() {
        String result = null;
        try {
            String ip = BASE_HOST;// ping 的地址，可以换成任何一种可靠的外网
//            String ip = "www.baidu.com";// ping 的地址，可以换成任何一种可靠的外网
            Process p = Runtime.getRuntime().exec("ping -c 3 -w 100 " + ip);// ping网址3次
            //读取ping的内容，可以不加
            InputStream input = p.getInputStream();
            BufferedReader in = new BufferedReader(new InputStreamReader(input));
            StringBuffer stringBuffer = new StringBuffer();
            String content = "";
            while ((content = in.readLine()) != null) {
                stringBuffer.append(content);
            }
            // ping的状态
            int status = p.waitFor();
            if (status == 0) {
                result = "success";
                return true;
            } else {
                result = "failed";
            }
        } catch (IOException e) {
            result = "IOException";
        } catch (InterruptedException e) {
            result = "InterruptedException";
        } finally {
        }
        return false;
    }
}
