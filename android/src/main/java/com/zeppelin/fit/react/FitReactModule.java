package com.zeppelin.fit.react;

import android.content.Context;

import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReactContextBaseJavaModule;
import com.facebook.react.bridge.ReactMethod;
import com.facebook.react.bridge.ReadableMap;
import com.facebook.react.bridge.Callback;
import com.facebook.react.bridge.Promise;
import com.facebook.react.bridge.JSApplicationCausedNativeException;

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
    public void initFitKit(ReadableMap options, Promise promise) {
        promise.reject("coming!");

        /**
        try {
            promise.resolve("herro");
        } catch (JSApplicationCausedNativeException e) {
            promise.reject(e);
        }
         */
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
