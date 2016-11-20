package com.zeppelin.fit.react;

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

import com.google.android.gms.fitness.request.DataSourcesRequest;
import com.google.android.gms.fitness.data.DataSource;



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

// sensor:
import android.content.Intent;
import android.app.PendingIntent;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.fitness.request.SensorRequest;
import rx.Single;
import rx.functions.Action1;
import com.google.android.gms.fitness.request.OnDataPointListener;
import android.app.IntentService;

// import com.zeppelin.fit.react.FitStepIntentService;
// import android.widget.Toast;

import com.google.android.gms.fitness.data.Value;

// react event
import com.facebook.react.bridge.ReactContext;
import com.facebook.react.uimanager.events.RCTEventEmitter;
import com.facebook.react.bridge.Arguments;
import com.facebook.react.bridge.WritableMap;
import com.facebook.react.modules.core.DeviceEventManagerModule;

public class FitStepObserver {

    private RxFit rxFit;
    private Context context;
    public static final String TAG = "RCTFitKit";

    public FitStepObserver(RxFit rxFit, Promise promise, long startTime, Context context) {
        this.rxFit = rxFit;
        this.context = context;
        init(promise, startTime, context);
    }

    private void registerFitnessDataListener(DataSource dataSource, DataType dataType) {
        Log.i(TAG, "registerFitnessDataListener!!");

        SensorRequest sensorRequest = new SensorRequest.Builder()
        .setDataSource(dataSource)
        .setDataType(dataType)
        .setSamplingRate(1, TimeUnit.SECONDS)
        .build();

        rxFit.sensors().getDataPoints(sensorRequest)
        .subscribe(new Observer<DataPoint>() {
            @Override
            public void onCompleted() {
                Log.i(TAG, "getDataPoints(sensorRequest) Observable done! :(");
            }

            @Override
            public void onError(Throwable e) {
                Log.e(TAG, "ooops error");
                Log.e(TAG, Log.getStackTraceString(e));
            }

            @Override
            public void onNext(DataPoint dataPoint) {
                WritableMap eventData = Arguments.createMap();

                ReactContext reactContext = (ReactContext) context;

                for (Field field : dataPoint.getDataType().getFields()) {
                    Value val = dataPoint.getValue(field);
                    Log.i(TAG, "Detected DataPoint field: " + field.getName());
                    Log.i(TAG, "Detected DataPoint value: " + val);
                    switch (field.getName()) {
                        case "steps":
                        eventData.putInt("steps", val.asInt());
                        break;
                    }
                    reactContext
                        .getJSModule(DeviceEventManagerModule.RCTDeviceEventEmitter.class)
                        .emit("FitKitStepEvent", eventData);
                }
            }
        });
    }

    private void init(final Promise promise, long startTime, Context context) {
        Log.i(TAG, "init FitStepObserver");

        DataSourcesRequest dataSourcesRequest = new DataSourcesRequest.Builder()
        .setDataSourceTypes(DataSource.TYPE_DERIVED)
        // .setDataSourceTypes(DataSource.TYPE_RAW)
        .setDataTypes(DataType.TYPE_STEP_COUNT_DELTA)
        .build();

        rxFit.sensors().findDataSources(dataSourcesRequest)
        .subscribe(new Observer<DataSource>() {
            @Override
            public void onCompleted() {
                Log.i(TAG, "Observable done!");
                promise.resolve("observable stepSource found");
            }

            @Override
            public void onError(Throwable e) {
                Log.e(TAG, "ooops error");
                Log.e(TAG, Log.getStackTraceString(e));
                promise.reject("error livetracking!", e);
            }

            @Override
            public void onNext(DataSource dataSource) {
                Log.i(TAG, "Data returned for Data type: " + dataSource);

                if (dataSource.getDataType().equals(DataType.TYPE_STEP_COUNT_DELTA)) {
                    Log.i(TAG, "daym!!");
                    registerFitnessDataListener(dataSource, dataSource.getDataType());
                }
            }
        });
    }
}
