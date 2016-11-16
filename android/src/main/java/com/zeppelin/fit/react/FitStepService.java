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

public class FitStepService {

    private RxFit rxFit;
    public static final String TAG = "RCTFitKit";

    public FitStepService(RxFit rxFit, Promise promise, long startTime, Context context) {
        this.rxFit = rxFit;
        Log.i(TAG, "FitStepService");
        getDailySteps(promise, startTime, context);
    }

    private void getDailySteps(final Promise promise, long startTime, Context context) {
        Log.i(TAG, "getDailySteps");
        Calendar cal = Calendar.getInstance();
        Date now = new Date();
        cal.setTime(now);
        final long endTime = cal.getTimeInMillis();
        cal.add(Calendar.WEEK_OF_YEAR, -2);
        startTime = cal.getTimeInMillis();

        // DataSource ds = new DataSource.Builder()
        //     .setAppPackageName("com.google.android.gms")
        //     .setDataType(DataType.TYPE_STEP_COUNT_DELTA)
        //     .setType(DataSource.TYPE_DERIVED)
        //     .setStreamName("estimated_steps")
        //     .build();

        // final DataSource ds = new DataSource.Builder()
        //     .setAppPackageName("com.google.android.gms")
        //     .setDataType(DataType.TYPE_STEP_COUNT_DELTA)
        //     .setType(DataSource.TYPE_DERIVED)
        //     .setStreamName("estimated_steps")
        //     .build();

        Log.i(TAG, "start: " + startTime);
        Log.i(TAG, "end:   " + endTime);

        // final DataReadRequest dataReadRequest = new DataReadRequest.Builder()
        //     .aggregate(ds, DataType.AGGREGATE_STEP_COUNT_DELTA)
        //     .bucketByTime(1, TimeUnit.DAYS)
        //     .setTimeRange(startTime, endTime, TimeUnit.MILLISECONDS)
        //     // .setTimeRange(timeBounds[0], timeBounds[1], TimeUnit.MILLISECONDS)
        //     .build();

        DataSource ESTIMATED_STEP_DELTAS = new DataSource.Builder()
            .setDataType(DataType.TYPE_STEP_COUNT_DELTA)
            .setType(DataSource.TYPE_DERIVED)
            .setStreamName("estimated_steps")
            .setAppPackageName("com.google.android.gms")
            .build();
        DataReadRequest dataReadRequest = new DataReadRequest.Builder()
            .aggregate(ESTIMATED_STEP_DELTAS,    DataType.AGGREGATE_STEP_COUNT_DELTA)
            .aggregate(DataType.TYPE_DISTANCE_DELTA, DataType.AGGREGATE_DISTANCE_DELTA)
            .aggregate(DataType.TYPE_CALORIES_EXPENDED, DataType.AGGREGATE_CALORIES_EXPENDED)
            // .aggregate(DataType.TYPE_ACTIVITY_SEGMENT, DataType.AGGREGATE_ACTIVITY_SUMMARY)
            .bucketByTime(1, TimeUnit.DAYS)
            .setTimeRange(startTime, endTime, TimeUnit.MILLISECONDS)
            .build();

        // DataReadRequest dataReadRequest = new DataReadRequest.Builder()
        //     .aggregate(DataType.TYPE_STEP_COUNT_DELTA, DataType.AGGREGATE_STEP_COUNT_DELTA)
        //     // .aggregate(DataType.TYPE_CALORIES_EXPENDED, DataType.AGGREGATE_CALORIES_EXPENDED)
        //     .bucketByTime(1, TimeUnit.DAYS)
        //     .setTimeRange(startTime, endTime, TimeUnit.MILLISECONDS)
        //     .build();
            // // The data request can specify multiple data types to return, effectively
            // // combining multiple data queries into one call.
            // // In this example, it's very unlikely that the request is for several hundred
            // // datapoints each consisting of a few steps and a timestamp.  The more likely
            // // scenario is wanting to see how many steps were walked per day, for 7 days.
            // .aggregate(DataType.TYPE_STEP_COUNT_DELTA, DataType.AGGREGATE_STEP_COUNT_DELTA)
            //         // Analogous to a "Group By" in SQL, defines how data should be aggregated.
            //         // bucketByTime allows for a time span, whereas bucketBySession would allow
            //         // bucketing by "sessions", which would need to be defined in code.
            // .bucketByTime(1, TimeUnit.DAYS)
            // .setTimeRange(startTime, endTime, TimeUnit.MILLISECONDS)
            // .build();

        // final WritableArray bodySamples = Arguments.createArray();
        // final WritableMap bodyMetrics = Arguments.createMap();
        // final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ");

        Log.i(TAG, "reading the stuffz");

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
                    // bodyMetrics.putArray("bodySamples", bodySamples);
                    // bodyMetrics.putString("endDate", dateFormat.format(endTime));
                    // promise.resolve(bodyMetrics);
                }

                @Override
                public void onError(Throwable e) {
                    Log.e(TAG, "ooops error????????????");
                    // e.printStackTrace();
                    // promise.reject("getBodyMetrics error!");
                }

                @Override
                public void onNext(Bucket bucket) {
                    Log.i(TAG, "=======================================");
                    // Log.i(TAG, "Data returned for getDailySteps: " + bucket);

                    // List<DataSet> dataSets = bucket.getDataSets();
                    for (DataSet dataSet : bucket.getDataSets()) {
                        showDataSet(dataSet);
                    }
                    // for (DataPoint dp : dataSet.getDataPoints()) {
                    //     String dateTime = dateFormat.format(dp.getStartTime(TimeUnit.MILLISECONDS));
                    //     for(Field field : dp.getDataType().getFields()) {
                    //         WritableMap bodyMetric = Arguments.createMap();

                    //         switch (field.getName()) {
                    //             case "weight":
                    //                 bodyMetric.putDouble("bodyMass", dp.getValue(field).asFloat());
                    //                 bodyMetric.putString("dateTime", dateTime);
                    //                 bodySamples.pushMap(bodyMetric);
                    //                 break;
                    //             case "height":
                    //                 bodyMetric.putDouble("height", dp.getValue(field).asFloat());
                    //                 bodyMetric.putString("dateTime", dateTime);
                    //                 bodySamples.pushMap(bodyMetric);
                    //                 break;
                    //         }

                    //     }
                    // }
                }
            });
    }

    private void showDataSet(DataSet dataSet) {
        Log.e(TAG, "Data returned for Data type: " + dataSet.getDataType().getName());
        DateFormat dateFormat = DateFormat.getDateInstance();
        DateFormat timeFormat = DateFormat.getTimeInstance();

        for (DataPoint dp : dataSet.getDataPoints()) {
            Log.e(TAG, "Data point:");
            Log.e(TAG, "\tType: " + dp.getDataType().getName());
            Log.e(TAG, "\tStart: " + dateFormat.format(dp.getStartTime(TimeUnit.MILLISECONDS)) + " " + timeFormat.format(dp.getStartTime(TimeUnit.MILLISECONDS)));
            Log.e(TAG, "\tEnd: " + dateFormat.format(dp.getEndTime(TimeUnit.MILLISECONDS)) + " " + timeFormat.format(dp.getStartTime(TimeUnit.MILLISECONDS)));
            for(Field field : dp.getDataType().getFields()) {
                Log.e(TAG, "\tField: " + field.getName() +
                        " Value: " + dp.getValue(field));
            }
        }
    }
}
