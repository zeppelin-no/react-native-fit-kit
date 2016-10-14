package com.zeppelin.fit.react;

import android.content.Context;

import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReactContextBaseJavaModule;
import com.facebook.react.bridge.ReactMethod;
import com.facebook.react.bridge.Callback;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.GoogleApiClient.ConnectionCallbacks;
import com.google.android.gms.common.api.Scope;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.Scopes;
import com.google.android.gms.fitness.Fitness;
import com.google.android.gms.fitness.data.DataSet;
import com.google.android.gms.fitness.data.DataSource;
import com.google.android.gms.fitness.data.DataPoint;
import com.google.android.gms.fitness.data.Field;
import com.google.android.gms.fitness.data.DataType;
import com.google.android.gms.fitness.data.Value;
import com.google.android.gms.fitness.result.DataSourcesResult;
import com.google.android.gms.fitness.request.DataReadRequest;
import com.google.android.gms.fitness.request.DataReadRequest.Builder;
import com.google.android.gms.fitness.request.SensorRequest;
import com.google.android.gms.fitness.request.OnDataPointListener;
import com.google.android.gms.fitness.request.DataSourcesRequest;
import android.content.IntentSender.SendIntentException;

import android.support.v4.app.FragmentActivity;
import java.text.DateFormat;
import java.util.concurrent.TimeUnit;
import android.app.Activity;
import android.support.v4.content.ContextCompat;
import android.support.v4.app.ActivityCompat;
import android.Manifest;
import android.content.pm.PackageManager;
import android.util.Log;
import android.R;
import android.view.View;
import android.support.design.widget.Snackbar;
import android.os.Bundle;

import android.content.Intent;

class FitReactModule extends ReactContextBaseJavaModule {
    private Context context;

    private final static String REACT_MODULE_NAME = "FitKit";
    public static final String TAG = "RCTFitKit";
    private static final int REQUEST_PERMISSIONS_REQUEST_CODE = 1;
    private GoogleApiClient googleApiFitnessClient = null;
    private OnDataPointListener mListener;
    private static final int REQUEST_CODE_RESOLVE_ERR = 9000;
    private GoogleApiClient _apiClient;

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
    public void haha(Callback onSuccess, Callback onFailure) {
        onSuccess.invoke("Hello World!");
    }

    @ReactMethod
    public void initFit(Callback onSuccess, Callback onFailure) {
        Log.i(TAG, "initFitAPI");

        Activity activity = getCurrentActivity();
        Intent intent = new Intent(activity, FitSessionActivity.class);
        activity.startActivity(intent);
        onSuccess.invoke("inited??!");
    }
}
