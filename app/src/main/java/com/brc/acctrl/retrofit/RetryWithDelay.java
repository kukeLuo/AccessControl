package com.brc.acctrl.retrofit;

import java.util.concurrent.TimeUnit;

import io.reactivex.Observable;
import io.reactivex.ObservableSource;
import io.reactivex.functions.Function;

/**
 * @author zhengdan
 * @date 2019-09-03
 * @Description:
 */
public class RetryWithDelay implements
        Function<Observable<? extends Throwable>, Observable<?>> {

    private int maxRetries = 2;//最大出错重试次数
    private int retryDelayMillis = 1000;//重试间隔时间
    private int retryCount = 0;//当前出错重试次数

    public RetryWithDelay() {
    }

    public RetryWithDelay(int maxRetries, int retryDelayMillis) {
        this.maxRetries = maxRetries;
        this.retryDelayMillis = retryDelayMillis;
    }

    @Override
    public Observable<?> apply(Observable<? extends Throwable> observable) throws Exception {
        return observable
                .flatMap(new Function<Throwable, ObservableSource<?>>() {
                    @Override
                    public ObservableSource<?> apply(Throwable throwable) throws Exception {
                        if (++retryCount <= maxRetries) {
                            // When this Observable calls onNext, the original Observable will be retried (i.e. re-subscribed).
                            return Observable.timer(retryDelayMillis * retryCount,
                                    TimeUnit.MILLISECONDS);
                        }
                        return Observable.error(throwable);
                    }
                });
    }
}
