package com.zeppelin.fit.react;

// import android.app.IntentService;
// import android.content.Intent;
import com.facebook.react.bridge.Promise;
import com.facebook.react.bridge.WritableArray;
import com.facebook.react.bridge.WritableMap;
import com.facebook.react.bridge.ReadableMap;
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

import com.google.android.gms.fitness.data.Bucket;
import com.google.android.gms.fitness.data.DataSource;

import com.google.android.gms.fitness.Fitness;
import com.google.android.gms.common.api.Api;
import com.google.android.gms.common.Scopes;
import com.google.android.gms.common.api.Scope;

import java.util.concurrent.TimeUnit;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Calendar;

import java.text.DateFormat;
import static java.text.DateFormat.getDateInstance;
import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.Map;
import java.sql.Timestamp;

public class FitStepService {

    private RxFit rxFit;
    public static final String TAG = "RCTFitKit";
    private final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS");

    public FitStepService(RxFit rxFit, Promise promise, Context context, ReadableMap options) {
        this.rxFit = rxFit;
        Log.i(TAG, "FitStepService");

        long[] timeBounds = getTimeBounds(options);
        getDailySteps(promise, timeBounds);
    }

    private void getDailySteps(final Promise promise, long[] timeBounds) {
        Log.i(TAG, "getDailySteps");

        DataSource ESTIMATED_STEP_DELTAS = new DataSource.Builder()
            .setDataType(DataType.TYPE_STEP_COUNT_DELTA)
            .setType(DataSource.TYPE_DERIVED)
            .setStreamName("estimated_steps")
            .setAppPackageName("com.google.android.gms")
            .build();

        DataReadRequest dataReadRequest = new DataReadRequest.Builder()
            .aggregate(ESTIMATED_STEP_DELTAS, DataType.AGGREGATE_STEP_COUNT_DELTA)
            // .aggregate(DataType.TYPE_DISTANCE_DELTA, DataType.AGGREGATE_DISTANCE_DELTA)
            // .aggregate(DataType.TYPE_CALORIES_EXPENDED, DataType.AGGREGATE_CALORIES_EXPENDED)
            // .aggregate(DataType.TYPE_ACTIVITY_SEGMENT, DataType.AGGREGATE_ACTIVITY_SUMMARY)
            .bucketByTime(1, TimeUnit.DAYS)
            .setTimeRange(timeBounds[0], timeBounds[1], TimeUnit.MILLISECONDS)
            .build();

        final WritableArray stepSamples = Arguments.createArray();
        final WritableMap stepsData = Arguments.createMap();
        stepsData.putString("endDate", dateFormat.format(timeBounds[1]));

        rxFit.history().read(dataReadRequest)
            .flatMapObservable(new Func1<DataReadResult, Observable<Bucket>>() {
                    @Override
                    public Observable<Bucket> call(DataReadResult dataReadResult) {
                        return Observable.from(dataReadResult.getBuckets());
                    }
                })
            .subscribe(new Observer<Bucket>() {
                @Override
                public void onCompleted() {
                    Log.i(TAG, "Observable done!??????");
                    stepsData.putArray("stepSamples", stepSamples);
                    promise.resolve(stepsData);
                }

                @Override
                public void onError(Throwable e) {
                    Log.e(TAG, "ooops error????????????");
                    promise.reject("getBodyMetrics error!", e);
                }

                @Override
                public void onNext(Bucket bucket) {
                    for (DataSet dataSet : bucket.getDataSets()) {
                        stepSamples.pushMap(formatStepData(dataSet));
                    }
                }
            });
    }

    private WritableMap formatStepData(DataSet dataSet) {
        WritableMap step = Arguments.createMap();

        for (DataPoint dp : dataSet.getDataPoints()) {
            step.putString("date", dateFormat.format(dp.getStartTime(TimeUnit.MILLISECONDS)));
            for(Field field : dp.getDataType().getFields()) {
                switch (field.getName()) {
                    case "steps":
                        step.putInt("value", dp.getValue(field).asInt());
                        break;
                }
            }
        }

        return step;
    }

    private long dateToInt(String startDateString) {
        long startDate = 1;
        try {
            // DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ");
            DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS");
            // dateFormat.setTimeZone(TimeZone.getTimeZone("GMT"));
            Date parsedDate = dateFormat.parse(startDateString);
            Timestamp timestamp = new Timestamp(parsedDate.getTime());
            startDate = timestamp.getTime();
        } catch(Exception e) {
            Log.e(TAG, "error with the startDate string, using 1");
            Log.e(TAG, Log.getStackTraceString(e));
        }

        return startDate;
    }

    private long[] getTimeBounds(ReadableMap options) {
        long endDate = 1;
        long startDate = 1;
        Calendar cal = Calendar.getInstance();
        Date now = new Date();
        cal.setTime(now);

        if (options.hasKey("endDate") && !options.isNull("endDate")) {
            endDate = dateToInt(options.getString("endDate"));
        } else {
            endDate = cal.getTimeInMillis();
        }

        if (options.hasKey("startDate") && !options.isNull("startDate")) {
            Log.e(TAG, options.getString("startDate"));
            startDate = dateToInt(options.getString("startDate"));
        } else {
            cal.add(Calendar.WEEK_OF_YEAR, -2);
            cal.set(Calendar.HOUR_OF_DAY, 0);
            cal.set(Calendar.MINUTE, 0);
            cal.set(Calendar.SECOND, 0);
            cal.set(Calendar.MILLISECOND, 0);
            startDate = cal.getTimeInMillis();
        }

        long[] timeBounds = {startDate, endDate};

        return timeBounds;
    }
}
