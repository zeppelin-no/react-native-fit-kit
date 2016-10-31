package com.zeppelin.fit.react;

import android.content.Context;
import com.facebook.react.bridge.WritableArray;
import com.facebook.react.bridge.WritableMap;
import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReactContextBaseJavaModule;
import com.facebook.react.bridge.ReactMethod;
import com.facebook.react.bridge.ReadableMap;
import com.facebook.react.bridge.Promise;
import com.facebook.react.bridge.Arguments;

// ^ is cleaned

import android.content.pm.PackageManager;
import android.support.annotation.NonNull;

import com.facebook.react.bridge.Callback; // TODO: remove

import com.facebook.react.bridge.JSApplicationCausedNativeException;

import android.app.Activity;
import android.support.v4.content.ContextCompat;
import android.support.v4.app.ActivityCompat;

import android.Manifest;
import android.content.pm.PackageManager;
import android.util.Log;
import android.R;
import android.view.View;
import android.os.Bundle;

import android.content.Intent;

// permission
import android.support.design.widget.Snackbar;

// Fitness related:
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.GoogleApiClient.ConnectionCallbacks;
import com.google.android.gms.common.api.GoogleApiClient.OnConnectionFailedListener;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.fitness.Fitness;
import com.google.android.gms.common.Scopes;
import com.google.android.gms.common.api.Scope;

import android.os.Bundle;
import android.content.IntentSender;

// read session related:
import com.google.android.gms.fitness.request.SessionReadRequest;
import com.google.android.gms.fitness.data.Session;
import java.util.Calendar;
import android.os.AsyncTask;
import com.google.android.gms.fitness.result.SessionReadResult;
import java.util.Date;
import com.google.android.gms.fitness.data.DataType;
import java.text.SimpleDateFormat;
import java.util.Map;
import java.util.HashMap;
// import java.time.format.DateTimeFormatter;
import java.util.List;
import com.google.android.gms.fitness.data.DataPoint;

// Log related:
import java.text.DateFormat;
import com.google.android.gms.fitness.data.Field;
import java.util.concurrent.TimeUnit;
import com.google.android.gms.fitness.data.DataSet;
import static java.text.DateFormat.getTimeInstance;


// bodymetrics
// import rx.Single;

import java.sql.Timestamp;
import java.util.TimeZone;

// background service
import android.app.job.JobParameters;
import android.os.Messenger;
import android.os.Message;
import android.os.Handler;
import android.os.Looper;

import android.app.job.JobScheduler;
import android.app.job.JobInfo;
import android.content.ComponentName;
import android.content.Context;
import android.widget.Toast;

import com.zeppelin.fit.react.FitJobService;
import com.zeppelin.fit.react.FitBodyMetricsService;
import com.zeppelin.fit.react.FitActivitiesService;

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

class FitReactModule extends ReactContextBaseJavaModule {
    private Context context;

    private final static String REACT_MODULE_NAME = "FitKit";
    public static final String TAG = "RCTFitKit";
    private static final int REQUEST_PERMISSIONS_REQUEST_CODE = 34;

    static final int FIT_API_REQUEST_CODE = 4;
    static final int RESULT_OK = 100;

    public static final int MSG_SERVICE_OBJ = 2;
    public static final int MSG_UNCOLOUR_START = 0;
    public static final int MSG_UNCOLOUR_STOP = 1;

    private GoogleApiClient mClient = null;
    private boolean authInProgress = false;

    private Promise getActivitiesPromise = null;

    private RxFit rxFit;

    private Map<String, String> googleFitToFitKitActivityMap = new HashMap<String, String>();

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
                },
                new Scope[] {
                    new Scope(Scopes.FITNESS_LOCATION_READ_WRITE),
                    new Scope(Scopes.FITNESS_ACTIVITY_READ_WRITE),
                    new Scope(Scopes.FITNESS_ACTIVITY_READ),
                    new Scope(Scopes.FITNESS_BODY_READ)
                }
            );
            // Completable lol = rxFit.checkConnection();

            // lol.fromAction(new Action0() {
            //     @Override
            //     public void call() {
            //         Log.i(TAG, "doOnCompleted");
            //         promise.resolve(true);
            //     }
            // }).subscribe();
            promise.resolve(true);
        } catch(Exception e) {
            e.printStackTrace();
            promise.reject("could not init fit kit");
        }
    }

    @ReactMethod
    public void getActivities(ReadableMap options, Promise promise) {
        Log.i(TAG, "getActivities");

        long startDate = 1;

        if (options.hasKey("startDate") && !options.isNull("startDate")) {
            startDate = getStartDate(options.getString("startDate"));
        }

        if (rxFit != null) {
            new FitActivitiesService(rxFit, promise, startDate, context);
        } else {
            promise.reject("must init first");
        }
    }

    private long getStartDate(String startDateString) {
        long startDate = 1;
        try {
            DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ");
            // DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS");
            // dateFormat.setTimeZone(TimeZone.getTimeZone("GMT"));
            Date parsedDate = dateFormat.parse(startDateString);
            Timestamp timestamp = new Timestamp(parsedDate.getTime());
            startDate = timestamp.getTime();
        } catch(Exception e) {
            Log.e(TAG, "error with the startDate string, using 1");
            e.printStackTrace();
        }

        return startDate;
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
            new FitBodyMetricsService(rxFit, promise, startDate, context);
        } else {
            promise.reject("must init first");
        }
    }

    public void onReceivedStartJob(JobParameters params) {
        Log.i(TAG, "onReceivedStartJob");
    }

    public void onReceivedStopJob() {
        Log.i(TAG, "onReceivedStopJob");
    }

    AsyncBackgroundSyncHandler yolo;

    @ReactMethod
    public void nextSync(ReadableMap options, Promise promise) {
        promise.reject("comming l8er");
        if (false) {
            Log.i(TAG, "nextSync");
            if (yolo != null) {
                yolo.nextSync(promise);
            }
        }
    }

    @ReactMethod
    public void startBackgroundSync(ReadableMap options, Promise promise) {
        promise.reject("comming l8er");
        if (false) {
            Log.i(TAG, "startBackgroundSync");

            yolo = new AsyncBackgroundSyncHandler(promise, context);

            yolo.execute();
        }
    }

    private class AsyncBackgroundSyncHandler extends AsyncTask<Void, Void, Void> {

        private Promise promise;
        private long startTime = 1;
        private Context context;
        private FitJobService mFitJobService;
        private static final int MYJOBID = 1;

        public AsyncBackgroundSyncHandler(Promise promise, Context context) {
            this.promise = promise;
            this.context = context;
        }

        public void nextSync(Promise promise) {
            mFitJobService.nextSync(promise);
        }

        Handler mHandler = new Handler(/* default looper */) {
            @Override
            public void handleMessage(Message msg) {
                switch (msg.what) {
                    case MSG_UNCOLOUR_START:
                        Log.i(TAG, "MSG_UNCOLOUR_START");
                        // mShowStartView.setBackgroundColor(defaultColor);
                        break;
                    case MSG_UNCOLOUR_STOP:
                        Log.i(TAG, "MSG_UNCOLOUR_STOP");
                        // mShowStopView.setBackgroundColor(defaultColor);
                        break;
                    case MSG_SERVICE_OBJ:
                        Log.i(TAG, "MSG_SERVICE_OBJ");
                        mFitJobService = (FitJobService) msg.obj;
                        ComponentName jobService = new ComponentName(context, FitJobService.class.getName());
                        JobInfo jobInfo = new JobInfo.Builder(MYJOBID, jobService).setPeriodic(10000).build();
                        mFitJobService.scheduleJob(jobInfo, promise, startTime);
                        // mTestService.setUiCallback(MainActivity.this);
                        break;
                }
            }
        };

        protected Void doInBackground(Void... params) {
            Log.i(TAG, "doing backgorun stuff");

            Intent startServiceIntent = new Intent(context, FitJobService.class);
            startServiceIntent.putExtra("messenger", new Messenger(mHandler));
            context.startService(startServiceIntent);


            // ComponentName jobService = new ComponentName(context, FitJobService.class.getName());
            // JobInfo jobInfo = new JobInfo.Builder(MYJOBID, jobService).setPeriodic(10000).build();

            // JobScheduler jobScheduler = (JobScheduler) context.getSystemService(Context.JOB_SCHEDULER_SERVICE);

            // int jobId = jobScheduler.schedule(jobInfo);
            // Log.i(TAG, "jobId " + jobId);

            return null;
        }
    }
}
