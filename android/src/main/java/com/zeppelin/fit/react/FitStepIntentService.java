package com.zeppelin.fit.react;

import android.app.IntentService;
import android.content.Intent;
import android.util.Log;
// import android.widget.Toast;

import com.google.android.gms.fitness.data.DataPoint;
import com.google.android.gms.fitness.data.Field;
import com.google.android.gms.fitness.data.Value;

public class FitStepIntentService extends IntentService {

    public static final String TAG = "RCTFitKit";

    public FitStepIntentService() {
        // Use the TAG to name the worker thread.
        super(FitStepIntentService.class.getSimpleName());
        Log.i(TAG, "================================");
        Log.i(TAG, "FitStepIntentService constructorr");
        // Toast.makeText(getApplicationContext(), "Pending intent FitStepIntentService", Toast.LENGTH_LONG).show();
        // super(TAG);
    }

    @Override
    public void onCreate() {
        super.onCreate();
        Log.i(TAG, "onCreate");
        // Toast.makeText(getApplicationContext(), "Pending intent onCreate", Toast.LENGTH_LONG).show();
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        Log.i(TAG, "onHandleIntent");
        DataPoint dataPoint = DataPoint.extract(intent);
        for (Field field : dataPoint.getDataType().getFields()) {
            Value val = dataPoint.getValue(field);
            Log.i(TAG, "Detected DataPoint field: " + field.getName());
            Log.i(TAG, "Detected DataPoint value: " + val);
        }

        // Toast.makeText(getApplicationContext(), "Pending intent onHandleIntent", Toast.LENGTH_LONG).show();

        // GeofencingEvent geofencingEvent = GeofencingEvent.fromIntent(intent);
        // if (geofencingEvent.hasError()) {
        //     String errorMessage = GeofenceErrorMessages.getErrorString(this,
        //             geofencingEvent.getErrorCode());
        //     Log.e(TAG, errorMessage);
        //     return;
        // }

        // // Get the transition type.
        // int geofenceTransition = geofencingEvent.getGeofenceTransition();

        // // Test that the reported transition was of interest.
        // if (geofenceTransition == Geofence.GEOFENCE_TRANSITION_ENTER ||
        //         geofenceTransition == Geofence.GEOFENCE_TRANSITION_EXIT) {

        //     // Get the geofences that were triggered. A single event can trigger multiple geofences.
        //     List<Geofence> triggeringGeofences = geofencingEvent.getTriggeringGeofences();

        //     // Get the transition details as a String.
        //     String geofenceTransitionDetails = getGeofenceTransitionDetails(
        //             this,
        //             geofenceTransition,
        //             triggeringGeofences
        //     );

        //     // Send notification and log the transition details.
        //     sendNotification(geofenceTransitionDetails);
        //     Log.i(TAG, geofenceTransitionDetails);
        // } else {
        //     // Log the error.
        //     Log.e(TAG, getString(R.string.geofence_transition_invalid_type, geofenceTransition));
        // }
    }
}
