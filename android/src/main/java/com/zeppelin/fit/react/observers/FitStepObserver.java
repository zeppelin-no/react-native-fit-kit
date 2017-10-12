package com.zeppelin.fit.react.observers;

// import android.content.Intent;
import com.facebook.react.bridge.Promise;
import com.facebook.react.bridge.WritableArray;
import com.facebook.react.bridge.WritableMap;
import com.facebook.react.bridge.Arguments;
import android.util.Log;
import android.content.Context;

// rx fit:
import com.patloew.rxfit.RxFit;
import rx.Observable;
import rx.Observer;
import rx.functions.Func1;
import rx.Subscription;

// react;
import com.facebook.react.bridge.ReactContext;
import com.facebook.react.uimanager.events.RCTEventEmitter;
import com.facebook.react.bridge.Arguments;
import com.facebook.react.bridge.WritableMap;
import com.facebook.react.modules.core.DeviceEventManagerModule;

// google fit:
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

import com.google.android.gms.fitness.data.Value;

// date:
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

// helpers:
import com.zeppelin.fit.react.helpers.LogH;

public class FitStepObserver {

  private RxFit rxFit;
  private Context context;
  private Subscription listner = null;

  public FitStepObserver(RxFit rxFit, Promise promise, Context context) {
    this.rxFit = rxFit;
    this.context = context;
    init(promise, context);
  }

  private void registerFitnessDataListener(DataSource dataSource, DataType dataType) {
    LogH.breakerTop();
    LogH.i("registerFitnessDataListener!!");

    SensorRequest sensorRequest = new SensorRequest.Builder()
      .setDataSource(dataSource)
      .setDataType(dataType)
      .setSamplingRate(1, TimeUnit.SECONDS)
      .build();

    listner = rxFit.sensors().getDataPoints(sensorRequest)
      .subscribe(new Observer<DataPoint>() {
        @Override
        public void onCompleted() {
          LogH.i("getDataPoints(sensorRequest) Observable done! :(");
        }

        @Override
        public void onError(Throwable e) {
          LogH.e("ooops error");
          LogH.e(Log.getStackTraceString(e));
        }

        @Override
        public void onNext(DataPoint dataPoint) {
          WritableMap eventData = Arguments.createMap();

          ReactContext reactContext = (ReactContext) context;

          LogH.i(Long.toString(dataPoint.getEndTime(TimeUnit.MILLISECONDS)));
          LogH.i(Long.toString(dataPoint.getStartTime(TimeUnit.MILLISECONDS)));
          // Log.i("getDataPoints(sensorRequest) Observable done! :(");

          for (Field field : dataPoint.getDataType().getFields()) {
              Value val = dataPoint.getValue(field);
              LogH.i("Detected DataPoint field: " + field.getName());
              LogH.i("Detected DataPoint value: " + val);
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

  public void removeStepObserver() {
    if (listner != null) {
      listner.unsubscribe();
    }
  }

  private void init(final Promise promise, Context context) {
    LogH.i("init FitStepObserver");

    DataSourcesRequest dataSourcesRequest = new DataSourcesRequest.Builder()
      .setDataSourceTypes(DataSource.TYPE_DERIVED)
      // .setDataSourceTypes(DataSource.TYPE_RAW)
      .setDataTypes(DataType.TYPE_STEP_COUNT_DELTA)
      .build();

    rxFit.sensors().findDataSources(dataSourcesRequest)
      .subscribe(new Observer<DataSource>() {
        @Override
        public void onCompleted() {
          LogH.i("step listner observable done!");
          promise.resolve("observable stepSource found");
        }

        @Override
        public void onError(Throwable e) {
          LogH.e("ooops error");
          LogH.e(Log.getStackTraceString(e));
          promise.reject("error livetracking!", e);
        }

        @Override
        public void onNext(DataSource dataSource) {
          LogH.i("Data returned for Data type: " + dataSource.getDataType());

          if (dataSource.getDataType().equals(DataType.TYPE_STEP_COUNT_DELTA)) {
            registerFitnessDataListener(dataSource, dataSource.getDataType());
          }
        }
      });
  }
}
