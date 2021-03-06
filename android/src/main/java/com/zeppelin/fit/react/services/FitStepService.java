package com.zeppelin.fit.react.services;

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
import java.util.TimeZone;

// helpers:
import com.zeppelin.fit.react.helpers.TimeBounds;
import com.zeppelin.fit.react.helpers.LogH;

public class FitStepService {

  private RxFit rxFit;
  private final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
  private final SimpleDateFormat dateFormatSimple = new SimpleDateFormat("yyyy-MM-dd");

  public FitStepService(RxFit rxFit, Promise promise, Context context, ReadableMap options) {
    this.rxFit = rxFit;
    LogH.breaker();
    LogH.i("FitStepService");

    dateFormat.setTimeZone(TimeZone.getTimeZone("GMT"));

    long[] timeBounds = TimeBounds.getTimeBounds(options);
    getDailySteps(promise, timeBounds);
  }

  private void getDailySteps(final Promise promise, long[] timeBounds) {
    LogH.i("getDailySteps");

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
          LogH.i("getDailySteps completed");
          stepsData.putArray("stepSamples", stepSamples);
          promise.resolve(stepsData);
        }

        @Override
        public void onError(Throwable e) {
          LogH.e("getDailySteps error");
          LogH.e(Log.getStackTraceString(e));
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
      for(Field field : dp.getDataType().getFields()) {
        switch (field.getName()) {
          case "steps":
            step.putString("date", dateFormatSimple.format(dp.getStartTime(TimeUnit.MILLISECONDS)));
            step.putInt("value", dp.getValue(field).asInt());
            break;
        }
      }
    }

    return step;
  }
}
