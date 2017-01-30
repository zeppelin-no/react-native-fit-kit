package com.zeppelin.fit.react.services;

import android.app.job.JobInfo;
import android.app.job.JobScheduler;
import android.app.job.JobParameters;
import android.app.job.JobService;
import android.content.Context;
import android.content.Intent;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.util.Log;

import com.zeppelin.fit.react.FitReactModule;

import java.util.LinkedList;

import com.facebook.react.bridge.Promise;

/**
 * Service to handle callbacks from the JobScheduler. Requests scheduled with the JobScheduler
 * ultimately land on this service's "onStartJob" method. Currently all this does is post a message
 * to the app's main activity to change the state of the UI.
 *
 * TODO: implement service
 *
 */
public class FitJobService extends JobService {
    private static final String TAG = "RCTFitKit";

    private Promise promise;
    private long startDate;


    // @Override
    // public void onCreate() {
    //     super.onCreate();
    //     Log.i(TAG, "Service created");
    // }

    // @Override
    // public void onDestroy() {
    //     super.onDestroy();
    //     Log.i(TAG, "Service destroyed");
    // }

    /**
     * When the app's MainActivity is created, it starts this service. This is so that the
     * activity and this service can communicate back and forth. See "setUiCalback()"
     */
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.i(TAG, "onStartCommand");

        try {
            Messenger callback = intent.getParcelableExtra("messenger");
            Message m = Message.obtain();
            m.what = FitReactModule.MSG_SERVICE_OBJ;
            m.obj = this;
            try {
                callback.send(m);
            } catch (Exception e) {
                Log.e(TAG, "error with the messenger");
            }
        } catch (Exception e) {
            Log.e(TAG, "Error passing service object back to activity.");
        }
        return START_NOT_STICKY;
    }

    @Override
    public boolean onStartJob(JobParameters params) {
        // We don't do any real 'work' in this sample app. All we'll
        // do is track which jobs have landed on our service, and
        // update the UI accordingly.
        // jobParamsMap.add(params);
        // if (mActivity != null) {
        //     mActivity.onReceivedStartJob(params);
        // }

        if (promise != null) {
            Log.i(TAG, "has a promise: " + promise);
            // new FitBodyMetricsService(promise, startDate, this);
        } else {
            Log.i(TAG, "aint got a promise, bor");
        }
        Log.i(TAG, "on start job: " + params.getJobId());
        return false;
    }

    @Override
    public boolean onStopJob(JobParameters params) {
        // Stop tracking these job parameters, as we've 'finished' executing.
        // jobParamsMap.remove(params);
        // if (mActivity != null) {
        //     mActivity.onReceivedStopJob();
        // }
        Log.i(TAG, "on stop job: " + params.getJobId());
        return false;
    }

    public void nextSync(Promise promise) {
        Log.d(TAG, "setting ny promise, lol" + promise);
        this.promise = promise;
    }

    // FitReactModule mActivity;
    // private final LinkedList<JobParameters> jobParamsMap = new LinkedList<JobParameters>();

    /** Send job to the JobScheduler. */
    public void scheduleJob(JobInfo t, Promise promise, long startDate) {
        Log.d(TAG, "Scheduling job");
        Log.d(TAG, "promise: " + promise);
        this.promise = promise;
        this.startDate = startDate;
        JobScheduler tm = (JobScheduler) getSystemService(Context.JOB_SCHEDULER_SERVICE);
        tm.schedule(t);
    }

    /**
     * Not currently used, but as an exercise you can hook this
     * up to a button in the UI to finish a job that has landed
     * in onStartJob().
     */
    // public boolean callJobFinished() {
    //     JobParameters params = jobParamsMap.poll();
    //     if (params == null) {
    //         return false;
    //     } else {
    //         jobFinished(params, false);
    //         return true;
    //     }
    // }

}
