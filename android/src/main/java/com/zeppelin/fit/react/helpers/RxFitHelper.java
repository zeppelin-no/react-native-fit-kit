
package com.zeppelin.fit.react.helpers;

import android.content.Context;
import android.util.Log;

import com.facebook.react.bridge.Promise;
import com.facebook.react.bridge.ReadableMap;

import com.google.android.gms.fitness.Fitness;
import com.google.android.gms.common.api.Api;
import com.google.android.gms.common.Scopes;
import com.google.android.gms.common.api.Scope;
import com.patloew.rxfit.RxFit;
import rx.Completable;
import rx.Observer;
import rx.functions.Action0;
import rx.functions.Action1;

public class RxFitHelper {

  private static RxFit rxFit = null;
  private static final String TAG = "RCTFitKit";

  public static void initFitKit(ReadableMap options, final Promise promise, Context context) {
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
            Log.e(TAG, "error, probably canceled");
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

  public static RxFit get() {
    return rxFit;
  }
}
