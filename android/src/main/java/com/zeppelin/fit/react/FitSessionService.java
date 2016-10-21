package com.zeppelin.fit.react;

import android.app.IntentService;
import android.content.Intent;
import android.util.Log;
import android.content.Context;

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

public class FitSessionService extends IntentService {

    public static final String TAG = "RCTFitKit";
    private GoogleApiClient mClient = null;

    public FitSessionService() {
        super("FitSessionService");
    }


    private void fitLol() {
        if (mClient == null) {
            Log.i(TAG, "ready to get crackin'");

            mClient = new GoogleApiClient.Builder(this)
                    .addApi(Fitness.HISTORY_API)
                    .addApi(Fitness.SESSIONS_API)
                    .addScope(new Scope(Scopes.FITNESS_LOCATION_READ_WRITE))
                    .addScope(new Scope(Scopes.FITNESS_ACTIVITY_READ_WRITE))
                    .addConnectionCallbacks(
                        new GoogleApiClient.ConnectionCallbacks() {
                            @Override
                            public void onConnected(Bundle bundle) {
                                Log.i(TAG, "Connected!!!");
                                // Now you can make calls to the Fitness APIs.  What to do?
                                // Play with some sessions!!
                                // new InsertAndVerifySessionTask().execute();
                            }

                            @Override
                            public void onConnectionSuspended(int i) {
                                // If your connection to the sensor gets lost at some point,
                                // you'll be able to determine the reason and react to it here.
                                if (i == ConnectionCallbacks.CAUSE_NETWORK_LOST) {
                                    Log.i(TAG, "Connection lost.  Cause: Network Lost.");
                                } else if (i
                                        == ConnectionCallbacks.CAUSE_SERVICE_DISCONNECTED) {
                                    Log.i(TAG,
                                            "Connection lost.  Reason: Service Disconnected");
                                }
                            }
                        }
                    )
                    // .addOnConnectionFailedListener(
                    //     new GoogleApiClient.OnConnectionFailedListener() {
                    //         @Override
                    //         public void onConnectionFailed(ConnectionResult connectionResult) {
                    //             // An unresolvable error has occurred and a connection to Google APIs
                    //             // could not be established. Display an error message, or handle
                    //             // the failure silently

                    //             Log.i(TAG, "connection failed, will do cool stuff");
                    //             if( !authInProgress ) {
                    //                 try {
                    //                     Log.i(TAG, connectionResult.toString());

                    //                     authInProgress = true;
                    //                     connectionResult.startResolutionForResult( FitSessionActivity.this, 4 );
                    //                 } catch(IntentSender.SendIntentException e ) {

                    //                 }
                    //             } else {
                    //                 Log.e( "GoogleFit", "authInProgress" );
                    //             }
                    //         }
                    //     }
                    // )
                    .build();

            mClient.connect();
        }
    }

    @Override
    protected void onHandleIntent(Intent workIntent) {
        Log.i(TAG, "starting service");
        // Gets data from the incoming Intent
        String dataString = workIntent.getType();

        Log.i(TAG, dataString);

        fitLol();
    }
}
