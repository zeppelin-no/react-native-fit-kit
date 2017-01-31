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

public class FitReactModule extends ReactContextBaseJavaModule {
    private Context context;

    private final static String REACT_MODULE_NAME = "FitKit";
    public static final String TAG = "RCTFitKit";

    public static final int MSG_UNCOLOUR_START = 0;
    public static final int MSG_UNCOLOUR_STOP = 1;
    public static final int MSG_SERVICE_OBJ = 2;

    private RxFit rxFit;

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
        Log.i(TAG, "");
        Log.i(TAG, "");
        Log.i(TAG, "=========================================");
        Log.i(TAG, "");
        Log.i(TAG, "");
        Log.i(TAG, "initFitKit");

        try {
            rxFit = new RxFit(
                context,
                new Api[] {
                    Fitness.HISTORY_API,
                    Fitness.SESSIONS_API,
                    Fitness.SENSORS_API,
                },
                new Scope[] {
                    new Scope(Scopes.FITNESS_LOCATION_READ_WRITE),
                    new Scope(Scopes.FITNESS_ACTIVITY_READ_WRITE),
                    new Scope(Scopes.FITNESS_BODY_READ_WRITE),
                }
            );
            Completable rxFitConnection = rxFit.checkConnection();

            Log.i(TAG, "check connection");

            rxFitConnection
                .doOnCompleted(new Action0() {
                    @Override
                    public void call() {
                        Log.i(TAG, "success!!!!");
                        promise.resolve(true);
                    }
                })
                .doOnError(new Action1<Throwable>() {
                    @Override
                    public void call(Throwable error) {
                        Log.e(TAG, "error!!!!");
                        error.printStackTrace();
                        promise.reject("could not init fit kit", error);
                    }
                })
                .onErrorComplete()
                .subscribe();

        } catch(Exception e) {
            rxFit = null;
            Log.i(TAG, "error setting up Google Fit connection");
            e.printStackTrace();
            promise.reject("could not init fit kit", e);
        }
    }

    private long getStartDate(String startDateString) {
        long startDate = 1;

        Log.e(TAG, startDateString);

        try {
            DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
            dateFormat.setTimeZone(TimeZone.getTimeZone("GMT"));
            Date parsedDate = dateFormat.parse(startDateString);

            startDate = parsedDate.getTime();
        } catch(Exception e) {
            Log.e(TAG, "error with the startDate string, using 1");
            e.printStackTrace();
        }

        return startDate;
    }

    @ReactMethod
    public void getActivities(ReadableMap options, Promise promise) {
        Log.i(TAG, "getActivities");

        if (rxFit != null) {
            new FitActivitiesService(rxFit, promise, context, options);
        } else {
            promise.reject("must init first");
        }
    }

    @ReactMethod
    public void getDailySteps(ReadableMap options, Promise promise) {
        Log.i(TAG, "getDailySteps");

        if (rxFit != null) {
            new FitStepService(rxFit, promise, context, options);
        } else {
            promise.reject("must init first");
        }
    }

    private FitStepObserver mStepObserver = null;

    @ReactMethod
    public void initStepCountObserver(ReadableMap options, Promise promise) {
        Log.i(TAG, "initStepCountObserver");

        if (rxFit != null) {
            mStepObserver = new FitStepObserver(rxFit, promise, context);
        } else {
            promise.reject("must init first");
        }
    }

    @ReactMethod
    public void removeStepObserver(ReadableMap options, Promise promise) {
      Log.i(TAG, "removeStepObserver");

      if (mStepObserver != null) {
        mStepObserver.removeStepObserver();
      }

      promise.resolve("subscripton probably removed");
    }

    private FitBodyMetricsService mReceiver;

    @ReactMethod
    public void getBodyMetrics(ReadableMap options, Promise promise) {
        Log.i(TAG, "getBodyMetrics");

        long startDate = 1;

        if (options.hasKey("startDate") && !options.isNull("startDate")) {
            startDate = getStartDate(options.getString("startDate"));
        }

        if (rxFit != null) {
            new FitBodyMetricsService(rxFit, context)
                .readBodyMetrics(promise, startDate);
        } else {
            promise.reject("must init first");
        }
    }

    @ReactMethod
    public void saveWeight(ReadableMap options, Promise promise) {
        Log.i(TAG, "saveWeight");

        if (rxFit != null) {
            new FitBodyMetricsService(rxFit, context)
                .saveWeight(options, promise);
        } else {
            promise.reject("must init first");
        }
    }

    @ReactMethod
    public void saveHeight(ReadableMap options, Promise promise) {
        Log.i(TAG, "saveHeight");

        if (rxFit != null) {
            new FitBodyMetricsService(rxFit, context)
                .saveHeight(options, promise);
        } else {
            promise.reject("must init first");
        }
    }

    @ReactMethod
    public void getBiologicalSex(ReadableMap options, Promise promise) {
        Log.i(TAG, "getBiologicalSex");

        promise.reject("comming");
    }

    @ReactMethod
    public void getLatestHeight(ReadableMap options, Promise promise) {
        Log.i(TAG, "getLatestHeight");

        promise.reject("comming");
    }

    @ReactMethod
    public void getLatestWeight(ReadableMap options, Promise promise) {
        Log.i(TAG, "getLatestWeight");

        promise.reject("comming");
    }
}
