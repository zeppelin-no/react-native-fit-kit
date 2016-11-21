package com.zeppelin.fit.react;

// import android.app.IntentService;
// import android.content.Intent;
import com.facebook.react.bridge.Promise;
import com.facebook.react.bridge.WritableArray;
import com.facebook.react.bridge.WritableMap;
import com.facebook.react.bridge.Arguments;
import android.util.Log;
import android.content.Context;

import com.patloew.rxfit.RxFit;
import rx.Observable;
import rx.Observer;
import rx.functions.Func1;

import com.google.android.gms.fitness.request.DataReadRequest;
import com.google.android.gms.fitness.result.DataReadResult;
import com.google.android.gms.fitness.data.DataPoint;
import com.google.android.gms.fitness.data.DataType;
import com.google.android.gms.fitness.data.DataSet;
import com.google.android.gms.fitness.data.Field;

import com.google.android.gms.fitness.Fitness;
import com.google.android.gms.common.api.Api;
import com.google.android.gms.common.Scopes;
import com.google.android.gms.common.api.Scope;

import java.util.concurrent.TimeUnit;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Calendar;

public class FitBodyMetricsService {

    private RxFit rxFit;
    public static final String TAG = "RCTFitKit";

    public FitBodyMetricsService(RxFit rxFit, Promise promise, long startTime, Context context) {
        this.rxFit = rxFit;
        readBodyMetrics(promise, startTime, context);
    }

    private void readBodyMetrics(final Promise promise, long startTime, Context context) {
        Calendar cal = Calendar.getInstance();
        Date now = new Date();
        cal.setTime(now);
        final long endTime = cal.getTimeInMillis();

        DataReadRequest dataReadRequest = new DataReadRequest.Builder()
            .read(DataType.TYPE_WEIGHT)
            .read(DataType.TYPE_HEIGHT)
            .setTimeRange(startTime, endTime, TimeUnit.MILLISECONDS)
            .build();

        final WritableArray bodySamples = Arguments.createArray();
        final WritableMap bodyMetrics = Arguments.createMap();
        final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ");

        rxFit.history().read(dataReadRequest)
            .flatMapObservable(new Func1<DataReadResult, Observable<DataSet>>() {
                    @Override
                    public Observable<DataSet> call(DataReadResult dataReadResult) {
                        return Observable.from(dataReadResult.getDataSets());
                    }
                })
            .subscribe(new Observer<DataSet>() {
                @Override
                public void onCompleted() {
                    Log.i(TAG, "Observable done!");
                    bodyMetrics.putArray("bodySamples", bodySamples);
                    bodyMetrics.putString("endDate", dateFormat.format(endTime));
                    promise.resolve(bodyMetrics);
                }

                @Override
                public void onError(Throwable e) {
                    Log.e(TAG, "ooops error");
                    e.printStackTrace();
                    promise.reject("getBodyMetrics error!", e);
                }

                @Override
                public void onNext(DataSet dataSet) {
                    Log.i(TAG, "Data returned for readBodyMetrics");

                    for (DataPoint dp : dataSet.getDataPoints()) {
                        String dateTime = dateFormat.format(dp.getStartTime(TimeUnit.MILLISECONDS));
                        for(Field field : dp.getDataType().getFields()) {
                            WritableMap bodyMetric = Arguments.createMap();

                            switch (field.getName()) {
                                case "weight":
                                    bodyMetric.putDouble("bodyMass", dp.getValue(field).asFloat());
                                    bodyMetric.putString("dateTime", dateTime);
                                    bodySamples.pushMap(bodyMetric);
                                    break;
                                case "height":
                                    bodyMetric.putDouble("height", dp.getValue(field).asFloat());
                                    bodyMetric.putString("dateTime", dateTime);
                                    bodySamples.pushMap(bodyMetric);
                                    break;
                            }

                        }
                    }
                }
            });
    }
}
