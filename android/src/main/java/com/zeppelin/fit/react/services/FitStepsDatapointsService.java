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
import com.zeppelin.fit.react.helpers.StepsDataSource;

public class FitStepsDatapointsService {

  private RxFit rxFit;
  public static final String TAG = "RCTFitKit";
  private final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
  private final SimpleDateFormat dateFormatSimple = new SimpleDateFormat("yyyy-MM-dd");

  public FitStepsDatapointsService(RxFit rxFit, Promise promise, Context context, ReadableMap options) {
    this.rxFit = rxFit;
    Log.i(TAG, "FitStepsDatapointsService");

    dateFormat.setTimeZone(TimeZone.getTimeZone("GMT"));

    long[] timeBounds = TimeBounds.getTimeBounds(options);
    getDailySteps(promise, timeBounds);
  }

  private void getDailySteps(final Promise promise, long[] timeBounds) {
    Log.i(TAG, "getDailySteps");

    DataSource ESTIMATED_STEP_DELTAS = StepsDataSource.get();

    DataReadRequest dataReadRequest = new DataReadRequest.Builder()
      .read(ESTIMATED_STEP_DELTAS)
      .setTimeRange(timeBounds[0], timeBounds[1], TimeUnit.MILLISECONDS)
      .build();

    final WritableArray stepSamples = Arguments.createArray();
    final WritableMap stepsData = Arguments.createMap();
    stepsData.putString("endDate", dateFormat.format(timeBounds[1]));

    rxFit.history().read(dataReadRequest)
      .subscribe(new Observer<DataReadResult>() {
        @Override
        public void onCompleted() {
          Log.i(TAG, "getStepsDatapoints completed");
          stepsData.putArray("stepSamples", stepSamples);
          promise.resolve(stepsData);
        }

        @Override
        public void onError(Throwable e) {
          Log.e(TAG, "getDailySteps error");
          Log.e(TAG, Log.getStackTraceString(e));
          promise.reject("getBodyMetrics error!", e);
        }

        @Override
        public void onNext(DataReadResult bucket) {
          for (DataSet dataSet : bucket.getDataSets()) {
            for (DataPoint dp : dataSet.getDataPoints()) {
              for(Field field : dp.getDataType().getFields()) {
                stepSamples.pushMap(formatStepData(field, dp));
              }
            }
          }
        }
      });
  }

  private WritableMap formatStepData(Field field, DataPoint dp) {
    WritableMap step = Arguments.createMap();
    switch (field.getName()) {
      case "steps":
        step.putString("startDate", dateFormat.format(dp.getStartTime(TimeUnit.MILLISECONDS)));

        step.putString("endDate", dateFormat.format(dp.getTimestamp(TimeUnit.MILLISECONDS)));

        step.putInt("value", dp.getValue(field).asInt());

        step.putString("blockId", dp.hashCode() + "");
        break;
    }

    return step;
  }
}
