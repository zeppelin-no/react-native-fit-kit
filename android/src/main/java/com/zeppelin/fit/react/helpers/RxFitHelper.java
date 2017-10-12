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

// helpers:
import com.zeppelin.fit.react.helpers.LogH;

public class RxFitHelper {

  private static RxFit rxFit = null;
  private static boolean connected = false;

  public static void initFitKit(ReadableMap options, final Promise promise, Context context) {
    LogH.empty();
    LogH.empty();
    LogH.breakerTop();
    LogH.i("initFitKit");
    LogH.breaker();

    if (connected && rxFit != null) {
      promise.resolve(true);
      return;
    }

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

      LogH.i("check connection");

      rxFitConnection
        .doOnCompleted(new Action0() {
          @Override
          public void call() {
            LogH.i("success!!!!");
            connected = true;
            promise.resolve(true);
          }
        })
        .doOnError(new Action1<Throwable>() {
          @Override
          public void call(Throwable error) {
            LogH.e("error, probably canceled");
            error.printStackTrace();
            connected = false;
            promise.reject("could not init fit kit", error);
          }
        })
        .onErrorComplete()
        .subscribe();

    } catch(Exception e) {
      rxFit = null;
      LogH.i("error setting up Google Fit connection");
      e.printStackTrace();
      promise.reject("could not init fit kit", e);
    }
  }

  public static RxFit get() {
    return rxFit;
  }
}
