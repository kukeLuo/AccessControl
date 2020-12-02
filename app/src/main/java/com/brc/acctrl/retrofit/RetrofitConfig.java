package com.brc.acctrl.retrofit;

import com.brc.acctrl.BuildConfig;
import com.brc.acctrl.activity.ServerConfigActivity;
import com.brc.acctrl.mqtt.HttpConst;
import com.brc.acctrl.utils.LogUtil;
import com.brc.acctrl.utils.SPUtil;
import com.facebook.stetho.okhttp3.StethoInterceptor;
import com.google.gson.Gson;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.UnsupportedCharsetException;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

import okhttp3.Interceptor;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okhttp3.ResponseBody;
import okhttp3.logging.HttpLoggingInterceptor;
import okio.Buffer;
import okio.BufferedSource;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory;
import retrofit2.converter.gson.GsonConverterFactory;

/**
 * Created by zhendan on 2015/12/18.
 */
public class RetrofitConfig {
    public static String BASE_HOST;

    private static Retrofit retrofitIns;
    private static APIService apiService;
    private static final int CONNECT_TIMEOUT = 5;

    public static Retrofit getInstance() {
        if (retrofitIns == null) {
            OkHttpClient client =
                    new OkHttpClient.Builder().connectTimeout(CONNECT_TIMEOUT,
                            TimeUnit.SECONDS).addNetworkInterceptor(new StethoInterceptor()).addInterceptor(getInterceptor()).build();

            int baseType=SPUtil.getInstance().getValue(ServerConfigActivity.SP_APP_HOST_TYPE, 1);
            switch (baseType){
                case 1:
                    BASE_HOST=HttpConst.BASE_URL_PRODUCT;
                    break;
                case 2:
                    BASE_HOST=HttpConst.BASE_URL_GATEWAY;
                    break;
                case 0:
                    BASE_HOST=HttpConst.BASE_URL_SIT;
                    break;
                default:
                    break;
            }
           /* BASE_HOST = SPUtil.getInstance().getValue(ServerConfigActivity.SP_APP_HOST_TYPE, 1) == 1
                    ? HttpConst.BASE_URL_PRODUCT : HttpConst.BASE_URL_SIT;*/
            retrofitIns =
                    new Retrofit.Builder().baseUrl(BASE_HOST).client(client)
                            .addConverterFactory(GsonConverterFactory.create()).addCallAdapterFactory
                            (RxJava2CallAdapterFactory.create()).build();
        }
        return retrofitIns;
    }

    public static void resetRetrofit() {
        retrofitIns = null;
    }

    public static <S> S createService(Class<S> serviceClass) {
        return getInstance().create(serviceClass);
    }

    public static APIService createService() {
        if (apiService == null) {
            apiService = getInstance().create(APIService.class);
        }
        return apiService;
    }

    public static RequestBody str2ReqBody(String content) {
        return RequestBody.create(MediaType.parse("text/plain"), content);
    }

    /**
     * 设置拦截器
     *
     * @return
     */
    private static  Interceptor getInterceptor() {

        HttpLoggingInterceptor interceptor = new HttpLoggingInterceptor(new HttpLoggingInterceptor.Logger() {
            @Override
            public void log(String message) {

                LogUtil.i("拦截信息=="+message);
            }
        });
        //显示日志
        interceptor.setLevel(HttpLoggingInterceptor.Level.BODY);

        return interceptor;
    }
    static class LoggingInterceptor implements Interceptor {
        private final Charset UTF8 = Charset.forName("UTF-8");

        @Override
        public Response intercept(Chain chain) throws IOException {
            Request request = chain.request();

            long t1 = System.nanoTime();
            LogUtil.trackLogDebug(String.format("Sending request %s on " +
                    "%s%n%s", request
                    .url(), chain.connection(), request.headers()));
            LogUtil.trackLogDebug(String.format("Sending request body %s",
                    new Gson().toJson(request.body())));
            Request headerRequest;

            try {
                headerRequest = request.newBuilder().addHeader("Content-Type"
                        , "application/json;charset=UTF-8").build();
            } catch (Exception e) {
                LogUtil.trackLogDebug("addHeader Error");
                e.printStackTrace();
                Response response = chain.proceed(request);

                long t2 = System.nanoTime();
                LogUtil.trackLogDebug(String.format(Locale.getDefault(),
                        "Received response "
                                + "for %s in %.1fms", response.request().url(),
                        (t2 - t1) / 1e6d));

                return response;
            }

            Response response = chain.proceed(headerRequest);

            long t2 = System.nanoTime();
            LogUtil.trackLogDebug(String.format(Locale.getDefault(),
                    "Received response for "
                            + "%s" + " in %.1fms", response.request().url(),
                    (t2 - t1) / 1e6d));

            if (BuildConfig.DEBUG) {
                ResponseBody responseBody = response.body();
                String rBody = null;

                BufferedSource source = responseBody.source();
                source.request(Long.MAX_VALUE); // Buffer the entire body.
                Buffer buffer = source.buffer();

                Charset charset = UTF8;
                MediaType contentType = responseBody.contentType();
                if (contentType != null) {
                    try {
                        charset = contentType.charset(UTF8);
                    } catch (UnsupportedCharsetException e) {
                        e.printStackTrace();
                    }
                }
                rBody = buffer.clone().readString(charset);
                LogUtil.trackLogDebug(unicodetoString(rBody));
            }
            return response;
        }

        public String unicodetoString(String unicode) {
            if (unicode == null || "".equals(unicode)) {
                return null;
            }
            StringBuilder sb = new StringBuilder();
            int i = -1;
            int pos = 0;
            while ((i = unicode.indexOf("\\u", pos)) != -1) {
                sb.append(unicode.substring(pos, i));
                if (i + 5 < unicode.length()) {
                    pos = i + 6;
                    sb.append((char) Integer.parseInt(unicode.substring(i + 2
                            , i + 6), 16));
                }
            }
            return sb.toString();
        }
    }
}
