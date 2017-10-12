package com.zeppelin.fit.react.services;

// import android.app.IntentService;
// import android.content.Intent;
import com.facebook.react.bridge.Promise;
import com.facebook.react.bridge.WritableArray;
import com.facebook.react.bridge.WritableMap;
import com.facebook.react.bridge.Arguments;
import com.facebook.react.bridge.ReadableMap;
import com.facebook.react.bridge.ReadableType;

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
import java.util.TimeZone;

import com.google.android.gms.fitness.data.DataSource;
import com.google.android.gms.common.api.Status;

// helpers:
import com.zeppelin.fit.react.helpers.LogH;

public class FitBodyMetricsService {

  private RxFit rxFit;
  private Context context;

  public FitBodyMetricsService(RxFit rxFit, Promise promise, Context context) {
    this.rxFit = rxFit;
    this.context = context;
  }

  public void readBodyMetrics(final Promise promise, long startTime) {
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
    final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
    dateFormat.setTimeZone(TimeZone.getTimeZone("GMT"));

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
          LogH.i("bodyMetrics observable done!");
          bodyMetrics.putArray("bodySamples", bodySamples);
          bodyMetrics.putString("endDate", dateFormat.format(endTime));
          promise.resolve(bodyMetrics);
        }

        @Override
        public void onError(Throwable e) {
          LogH.e("ooops error");
          LogH.e(Log.getStackTraceString(e));
          promise.reject("getBodyMetrics error!", e);
        }

        @Override
        public void onNext(DataSet dataSet) {
          LogH.i("Data returned for readBodyMetrics");

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

  private DataSet createDataForRequest(
    DataType dataType,
    int dataSourceType,
    Object values,
    TimeUnit timeUnit
  ) {
    Calendar cal = Calendar.getInstance();
    Date now = new Date();
    cal.setTime(now);
    long endTime = cal.getTimeInMillis();
    cal.add(Calendar.DAY_OF_YEAR, -1);
    long startTime = cal.getTimeInMillis();


    DataSource dataSource = new DataSource.Builder()
      .setAppPackageName(context.getPackageName())
      .setDataType(dataType)
      .setStreamName(LogH.TAG + " - ass count")
      .setType(dataSourceType)
      .build();


    DataSet dataSet = DataSet.create(dataSource);

    DataPoint dataPoint = dataSet.createDataPoint()
      .setTimestamp(endTime, timeUnit);

    if (values instanceof Integer) {
      dataPoint = dataPoint.setIntValues((Integer)values);
    } else {
      dataPoint = dataPoint.setFloatValues((Float)values);
      // dataPoint = dataPoint.getValue().setFloat((Float)values);
    }

    dataSet.add(dataPoint);

    return dataSet;
  }

  public void saveWeight(ReadableMap options, final Promise promise) {
    Float weight;
    try {
      weight = Float.valueOf(String.valueOf(options.getDouble("value")));
      DataSet dataSet = createDataForRequest(
        DataType.TYPE_WEIGHT,
        DataSource.TYPE_RAW,
        weight,
        TimeUnit.MILLISECONDS
      );

      rxFit.history().insert(dataSet)
        .subscribe(new Observer<Status>() {
          @Override
          public void onCompleted() {
            LogH.i("save waight observable done!");
            promise.resolve("");
          }

          @Override
          public void onError(Throwable e) {
            LogH.e("ooops error");
            LogH.e(Log.getStackTraceString(e));
            promise.reject("saveWeight error!", e);
          }

          @Override
          public void onNext(Status status) {
            LogH.i("Data returned for readBodyMetrics");
          }
        });
    } catch (Exception e) {
      promise.reject("saveWeight error", e);
    }
  }

  public void saveHeight(ReadableMap options, final Promise promise) {
    Float height;
    try {
      height = Float.valueOf(String.valueOf(options.getDouble("value")));
      DataSet dataSet = createDataForRequest(
        DataType.TYPE_HEIGHT,
        DataSource.TYPE_RAW,
        height,
        TimeUnit.MILLISECONDS
      );

      rxFit.history().insert(dataSet)
        .subscribe(new Observer<Status>() {
          @Override
          public void onCompleted() {
            LogH.i("save height observable done!");
            promise.resolve("");
          }

          @Override
          public void onError(Throwable e) {
            LogH.e("ooops error");
            LogH.e(Log.getStackTraceString(e));
            promise.reject("saveHeight error!", e);
          }

          @Override
          public void onNext(Status status) {
            LogH.i("Data returned for readBodyMetrics");
          }
        });
    } catch (Exception e) {
      promise.reject("saveHeight error", e);
    }
  }
}
