package com.zeppelin.fit.react;

import android.content.Context;
import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReactContextBaseJavaModule;
import com.facebook.react.bridge.ReactMethod;
import com.facebook.react.bridge.ReadableMap;
import com.facebook.react.bridge.Promise;

import android.util.Log;

// date related:
import java.util.Date;
import java.text.SimpleDateFormat;
import java.text.DateFormat;
import java.sql.Timestamp;
import java.util.TimeZone;

// background service:
import android.content.Intent;
import android.os.AsyncTask;
import android.app.job.JobParameters;
import android.os.Messenger;
import android.os.Message;
import android.os.Handler;
import android.os.Looper;
import android.app.job.JobInfo;
import android.content.ComponentName;

// FitKit:
import com.zeppelin.fit.react.services.FitBodyMetricsService;
import com.zeppelin.fit.react.services.FitActivitiesService;
import com.zeppelin.fit.react.services.FitStepService;
import com.zeppelin.fit.react.services.FitStepsDatapointsService;
import com.zeppelin.fit.react.observers.FitStepObserver;

// rx fit:
import com.google.android.gms.fitness.Fitness;
import com.google.android.gms.common.api.Api;
import com.google.android.gms.common.Scopes;
import com.google.android.gms.common.api.Scope;
import com.patloew.rxfit.RxFit;
import rx.Completable;
import rx.Observer;
import rx.functions.Action0;
import rx.functions.Action1;

// helpers:
import com.zeppelin.fit.react.helpers.RxFitHelper;
import com.zeppelin.fit.react.helpers.LogH;

public class FitReactModule extends ReactContextBaseJavaModule {
  private Context context;

  private final static String REACT_MODULE_NAME = "FitKit";

  public FitReactModule(ReactApplicationContext reactContext) {
    super(reactContext);
    this.context = reactContext;
  }

  /**
   * @return the name of this module. This will be the name used to {@code require()} this module
   * from javascript.
   */
  @Override
  public String getName() {
    return REACT_MODULE_NAME;
  }

  @ReactMethod
  public void initFitKit(ReadableMap options, final Promise promise) {
    RxFitHelper.initFitKit(options, promise, context);
  }

  private long getStartDate(String startDateString) {
    long startDate = 1;

    LogH.i(startDateString);

    try {
      DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
      dateFormat.setTimeZone(TimeZone.getTimeZone("GMT"));
      Date parsedDate = dateFormat.parse(startDateString);

      startDate = parsedDate.getTime();
    } catch(Exception e) {
      LogH.e("error with the startDate string, using 1");
      e.printStackTrace();
    }

    return startDate;
  }

  @ReactMethod
  public void getActivities(ReadableMap options, Promise promise) {
    RxFit rxFit = RxFitHelper.get();
    if (rxFit != null) {
      new FitActivitiesService(rxFit, promise, context, options);
    } else {
      promise.reject("must init first");
    }
  }

  @ReactMethod
  public void getDailySteps(ReadableMap options, Promise promise) {
    RxFit rxFit = RxFitHelper.get();
    if (rxFit != null) {
      new FitStepService(rxFit, promise, context, options);
    } else {
      promise.reject("must init first");
    }
  }


  @ReactMethod
  public void getStepsDataPoints(ReadableMap options, Promise promise) {
    RxFit rxFit = RxFitHelper.get();
    if (rxFit != null) {
      new FitStepsDatapointsService(rxFit, promise, context, options);
    } else {
      promise.reject("must init first");
    }
  }

  private FitStepObserver mStepObserver = null;

  @ReactMethod
  public void initStepCountObserver(ReadableMap options, Promise promise) {
    RxFit rxFit = RxFitHelper.get();
    if (rxFit != null) {
      mStepObserver = new FitStepObserver(rxFit, promise, context);
    } else {
      promise.reject("must init first");
    }
  }

  @ReactMethod
  public void removeStepObserver(ReadableMap options, Promise promise) {
    LogH.i("removeStepObserver");
    LogH.i("observer is: " + mStepObserver);

    if (mStepObserver != null) {
      mStepObserver.removeStepObserver();
    }

    promise.resolve("subscripton probably removed");
  }

  private FitBodyMetricsService mReceiver;

  @ReactMethod
  public void getBodyMetrics(ReadableMap options, Promise promise) {
    LogH.i("getBodyMetrics");

    long startDate = 1;

    if (options.hasKey("startDate") && !options.isNull("startDate")) {
      startDate = getStartDate(options.getString("startDate"));
    }

    RxFit rxFit = RxFitHelper.get();
    if (rxFit != null) {
      new FitBodyMetricsService(rxFit, promise, context).readBodyMetrics(promise, startDate);
    } else {
      promise.reject("must init first");
    }
  }

  @ReactMethod
  public void saveWeight(ReadableMap options, Promise promise) {
    RxFit rxFit = RxFitHelper.get();
    if (rxFit != null) {
      new FitBodyMetricsService(rxFit, promise, context).saveWeight(options, promise);
    } else {
      promise.reject("must init first");
    }
  }

  @ReactMethod
  public void saveHeight(ReadableMap options, Promise promise) {
    RxFit rxFit = RxFitHelper.get();
    if (rxFit != null) {
      new FitBodyMetricsService(rxFit, promise, context).saveHeight(options, promise);
    } else {
      promise.reject("must init first");
    }
  }

  @ReactMethod
  public void getBiologicalSex(ReadableMap options, Promise promise) {
    LogH.i("getBiologicalSex");

    promise.reject("comming");
  }

  @ReactMethod
  public void getLatestHeight(ReadableMap options, Promise promise) {
    LogH.i("getLatestHeight");

    promise.reject("comming");
  }

  @ReactMethod
  public void getLatestWeight(ReadableMap options, Promise promise) {
    LogH.i("getLatestWeight");

    promise.reject("comming");
  }
}
