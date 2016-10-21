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

import com.facebook.react.bridge.ActivityEventListener;
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

// Log related:
import java.text.DateFormat;
import com.google.android.gms.fitness.data.DataPoint;
import com.google.android.gms.fitness.data.Field;
import java.util.concurrent.TimeUnit;
import com.google.android.gms.fitness.data.DataSet;
import java.util.List;
import static java.text.DateFormat.getTimeInstance;

class FitReactModule extends ReactContextBaseJavaModule implements ActivityEventListener {
    private Context context;

    private final static String REACT_MODULE_NAME = "FitKit";
    public static final String TAG = "RCTFitKit";
    private static final int REQUEST_PERMISSIONS_REQUEST_CODE = 34;

    static final int FIT_API_REQUEST_CODE = 4;
    static final int RESULT_OK = 100;

    private GoogleApiClient mClient = null;
    private boolean authInProgress = false;

    private Promise getActivitiesPromise = null;

    private Map<String, String> googleFitToFitKitActivityMap = new HashMap<String, String>();

    public FitReactModule(ReactApplicationContext reactContext) {
        super(reactContext);
        this.context = reactContext;
        reactContext.addActivityEventListener(this);
        initGFToFKMap();
    }

    private void initGFToFKMap() {
        googleFitToFitKitActivityMap.put("AEROBICS", "MIXED_METABOLIC_CARDIO_TRAINING");
        googleFitToFitKitActivityMap.put("BIATHLON", "CROSS_COUNTRY_SKIING");
        googleFitToFitKitActivityMap.put("BIKING", "CYCLING");
        googleFitToFitKitActivityMap.put("BIKING.HAND", "CYCLING");
        googleFitToFitKitActivityMap.put("BIKING.MOUNTAIN", "CYCLING");
        googleFitToFitKitActivityMap.put("BIKING.ROAD", "CYCLING");
        googleFitToFitKitActivityMap.put("BIKING.SPINNING", "CYCLING");
        googleFitToFitKitActivityMap.put("BIKING.STATIONARY", "CYCLING");
        googleFitToFitKitActivityMap.put("BIKING.UTILITY", "CYCLING");
        googleFitToFitKitActivityMap.put("CALISTHENICS", "CROSS_TRAINING");
        googleFitToFitKitActivityMap.put("CIRCUIT_TRAINING", "HIGH_INTENSITY_INTERVAL_TRAINING");
        googleFitToFitKitActivityMap.put("CROSSFIT", "CROSS_TRAINING");
        googleFitToFitKitActivityMap.put("DANCING", "DANCE");
        googleFitToFitKitActivityMap.put("DIVING", "SWIMMING");
        googleFitToFitKitActivityMap.put("ELEVATOR", "IGNORE");
        googleFitToFitKitActivityMap.put("ERGOMETER", "ROWING");
        googleFitToFitKitActivityMap.put("ESCALATOR", "IGNORE");
        googleFitToFitKitActivityMap.put("FOOTBALL.AMERICAN", "AMERICAN_FOOTBALL");
        googleFitToFitKitActivityMap.put("FOOTBALL.AUSTRALIAN", "AUSTRALIAN_FOOTBALL");
        googleFitToFitKitActivityMap.put("FOOTBALL.SOCCER", "SOCCER");
        googleFitToFitKitActivityMap.put("FRISBEE_DISC", "OTHER");
        googleFitToFitKitActivityMap.put("GARDENING", "IGNORE");
        googleFitToFitKitActivityMap.put("HORSEBACK_RIDING", "OTHER");
        googleFitToFitKitActivityMap.put("HOUSEWORK", "IGNORE");
        googleFitToFitKitActivityMap.put("ICE_SKATING", "SKATING_SPORTS");
        googleFitToFitKitActivityMap.put("INTERVAL_TRAINING", "HIGH_INTENSITY_INTERVAL_TRAINING");
        googleFitToFitKitActivityMap.put("IN_VEHICLE", "IGNORE");
        googleFitToFitKitActivityMap.put("KAYAKING", "PADDLE_SPORTS");
        googleFitToFitKitActivityMap.put("KETTLEBELL_TRAINING", "TRADITIONAL_STRENGTH_TRAINING");
        googleFitToFitKitActivityMap.put("KICK_SCOOTER", "OTHER");
        googleFitToFitKitActivityMap.put("KITESURFING", "SURFING_SPORTS");
        googleFitToFitKitActivityMap.put("MEDITATION", "MIND_AND_BODY");
        googleFitToFitKitActivityMap.put("MIXED_MARTIAL_ARTS", "MARTIAL_ARTS");
        googleFitToFitKitActivityMap.put("ON_FOOT", "WALKING");
        googleFitToFitKitActivityMap.put("P90X", "HIGH_INTENSITY_INTERVAL_TRAINING");
        googleFitToFitKitActivityMap.put("PARAGLIDING", "SURFING_SPORTS");
        googleFitToFitKitActivityMap.put("POLO", "OTHER");
        googleFitToFitKitActivityMap.put("ROCK_CLIMBING", "CLIMBING");
        googleFitToFitKitActivityMap.put("ROWING.MACHINE", "ROWING");
        googleFitToFitKitActivityMap.put("RUNNING.JOGGING", "RUNNING");
        googleFitToFitKitActivityMap.put("RUNNING.SAND", "RUNNING");
        googleFitToFitKitActivityMap.put("RUNNING.TREADMILL", "RUNNING");
        googleFitToFitKitActivityMap.put("SCUBA_DIVING", "SWIMMING");
        googleFitToFitKitActivityMap.put("SKATEBOARDING", "OTHER");
        googleFitToFitKitActivityMap.put("SKATING", "SKATING_SPORTS");
        googleFitToFitKitActivityMap.put("SKATING.CROSS", "SKATING_SPORTS");
        googleFitToFitKitActivityMap.put("SKATING.INDOOR", "SKATING_SPORTS");
        googleFitToFitKitActivityMap.put("SKATING.INLINE", "SKATING_SPORTS");
        googleFitToFitKitActivityMap.put("SKIING", "CROSS_COUNTRY_SKIING");
        googleFitToFitKitActivityMap.put("SKIING.BACK_COUNTRY", "CROSS_COUNTRY_SKIING");
        googleFitToFitKitActivityMap.put("SKIING.CROSS_COUNTRY", "CROSS_COUNTRY_SKIING");
        googleFitToFitKitActivityMap.put("SKIING.DOWNHILL", "DOWNHILL_SKIING");
        googleFitToFitKitActivityMap.put("SKIING.KITE", "SNOW_SPORTS");
        googleFitToFitKitActivityMap.put("SKIING.ROLLER", "OTHER");
        googleFitToFitKitActivityMap.put("SLEDDING", "SNOW_SPORTS");
        googleFitToFitKitActivityMap.put("SLEEP", "IGNORE");
        googleFitToFitKitActivityMap.put("SLEEP.AWAKE", "IGNORE");
        googleFitToFitKitActivityMap.put("SLEEP.DEEP", "IGNORE");
        googleFitToFitKitActivityMap.put("SLEEP.LIGHT", "IGNORE");
        googleFitToFitKitActivityMap.put("SLEEP.REM", "IGNORE");
        googleFitToFitKitActivityMap.put("SNOWMOBILE", "SNOW_SPORTS");
        googleFitToFitKitActivityMap.put("SNOWSHOEING", "SNOW_SPORTS");
        googleFitToFitKitActivityMap.put("STAIR_CLIMBING.MACHINE", "STAIR_CLIMBING");
        googleFitToFitKitActivityMap.put("STANDUP_PADDLEBOARDING", "PADDLE_SPORTS");
        googleFitToFitKitActivityMap.put("STILL", "IGNORE");
        googleFitToFitKitActivityMap.put("STRENGTH_TRAINING", "TRADITIONAL_STRENGTH_TRAINING");
        googleFitToFitKitActivityMap.put("SURFING", "SURFING_SPORTS");
        googleFitToFitKitActivityMap.put("SWIMMING.OPEN_WATER", "SWIMMING");
        googleFitToFitKitActivityMap.put("SWIMMING.POOL", "SWIMMING");
        googleFitToFitKitActivityMap.put("TEAM_SPORTS", "OTHER");
        googleFitToFitKitActivityMap.put("TREADMILL", "RUNNING");
        googleFitToFitKitActivityMap.put("UNKNOWN", "OTHER");
        googleFitToFitKitActivityMap.put("VOLLEYBALL.BEACH", "VOLLEYBALL");
        googleFitToFitKitActivityMap.put("VOLLEYBALL.INDOOR", "VOLLEYBALL");
        googleFitToFitKitActivityMap.put("WAKEBOARDING", "WATER_SPORTS");
        googleFitToFitKitActivityMap.put("WALKING.FITNESS", "WALKING");
        googleFitToFitKitActivityMap.put("WALKING.NORDIC", "WALKING");
        googleFitToFitKitActivityMap.put("WALKING.STROLLER", "WALKING");
        googleFitToFitKitActivityMap.put("WALKING.TREADMILL", "WALKING");
        googleFitToFitKitActivityMap.put("WATER_POLO", "WATER_SPORTS");
        googleFitToFitKitActivityMap.put("WEIGHTLIFTING", "TRADITIONAL_STRENGTH_TRAINING");
        googleFitToFitKitActivityMap.put("WHEELCHAIR", "WHEELCHAIR_WALK_PACE");
        googleFitToFitKitActivityMap.put("WINDSURFING", "WATER_SPORTS");
        googleFitToFitKitActivityMap.put("ZUMBA", "HIGH_INTENSITY_INTERVAL_TRAINING");
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

    private void buildFitnessClient() {
        Log.i(TAG, "building fitnesse client");

        // Create the Google API Client
        if (mClient == null) {
            Log.i(TAG, "ready to get crackin'");

            mClient = new GoogleApiClient.Builder(getReactApplicationContext())
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
                                new InsertAndVerifySessionTask().execute();
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
                    .addOnConnectionFailedListener(
                        new GoogleApiClient.OnConnectionFailedListener() {
                            @Override
                            public void onConnectionFailed(ConnectionResult connectionResult) {
                                // An unresolvable error has occurred and a connection to Google APIs
                                // could not be established. Display an error message, or handle
                                // the failure silently

                                Log.i(TAG, "connection failed, will do cool stuff");
                                if( !authInProgress ) {
                                    try {
                                        Log.i(TAG, connectionResult.toString());

                                        authInProgress = true;
                                        connectionResult.startResolutionForResult(getCurrentActivity(), FIT_API_REQUEST_CODE);
                                    } catch(IntentSender.SendIntentException e ) {

                                    }
                                } else {
                                    Log.e(TAG, "authInProgress");
                                }
                            }
                        }
                    )
                    .build();
            mClient.connect();
        }
    }

    @Override
    public void onActivityResult(Activity activity, int requestCode, int resultCode, Intent data) {
        Log.i(TAG, requestCode + " requestCode");
        Log.i(TAG, resultCode  + " resultCode");

        if (requestCode == FIT_API_REQUEST_CODE) {
            if (resultCode == getCurrentActivity().RESULT_OK) {
                mClient.connect();
            }
        }
    }

    @Override
    public void onNewIntent(Intent intent) { }

    private String getActivityName(String gfActivityName) {
        String mappedActivity = googleFitToFitKitActivityMap.get(gfActivityName.toUpperCase());
        if (mappedActivity == null) {
            return gfActivityName.toUpperCase();
        }
        return mappedActivity;
    }

    private class InsertAndVerifySessionTask extends AsyncTask<Void, Void, Void> {
        protected Void doInBackground(Void... params) {

            SessionReadRequest readRequest = readFitnessSession();

            SessionReadResult sessionReadResult = Fitness.SessionsApi.readSession(mClient, readRequest).await(1, TimeUnit.MINUTES);

            // Get a list of the sessions that match the criteria to check the result.
            Log.i(TAG, "Session read. Number of returned sessions is: "
                    + sessionReadResult.getSessions().size());

            WritableArray activities = Arguments.createArray();

            for (Session session : sessionReadResult.getSessions()) {
                dumpSession(session);
                List<DataSet> dataSets = sessionReadResult.getDataSet(session);
                // for (DataSet dataSet : dataSets) {
                //     dumpDataSet(dataSet);
                // }

                String activityName = getActivityName(session.getActivity());

                if (activityName != "IGNORE") {
                    WritableMap activity = Arguments.createMap();
                    WritableMap activityHeader = Arguments.createMap();
                    WritableMap activityBody = Arguments.createMap();
                    SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ");
                    String startTime = dateFormat.format(session.getStartTime(TimeUnit.MILLISECONDS));
                    String endTime = dateFormat.format(session.getEndTime(TimeUnit.MILLISECONDS));

                    // header
                    WritableMap schemaId = Arguments.createMap();
                    activityHeader.putString("id", session.getIdentifier());
                    activityHeader.putString("creation_date_time", startTime);
                    schemaId.putString("name", "physical-activity");
                    schemaId.putString("namespace", "omh");
                    schemaId.putString("version", "1.2");
                    activityHeader.putMap("schema_id", schemaId);

                    // body
                    WritableMap effectiveTimeFrame = Arguments.createMap();
                    activityBody.putString("activity_name", activityName);
                    if (session.hasActiveTime()) {
                        WritableMap duration = Arguments.createMap();
                        duration.putString("unit", "sec");
                        duration.putInt("value", (int) (long) session.getActiveTime(TimeUnit.SECONDS));
                        activityBody.putMap("duration", duration);

                        WritableMap timeInterval = Arguments.createMap();
                        timeInterval.putString("start_date_time", startTime);
                        timeInterval.putString("end_date_time", endTime);
                        effectiveTimeFrame.putMap("time_interval", timeInterval);
                    } else {
                        effectiveTimeFrame.putString("date_time", startTime);
                    }
                    activityBody.putMap("effective_time_frame", effectiveTimeFrame);

                    activity.putMap("header", activityHeader);
                    activity.putMap("body", activityBody);

                    activities.pushMap(activity);
                }
            }

            getActivitiesPromise.resolve(activities);

            return null;
        }
    }

    private SessionReadRequest readFitnessSession() {
        Log.i(TAG, "Reading History API results for sessions: ");
        // [START build_read_session_request]
        // Set a start and end time for our query, using a start time of 1 week before this moment.
        Calendar cal = Calendar.getInstance();
        Date now = new Date();
        cal.setTime(now);
        long endTime = cal.getTimeInMillis();
        cal.add(Calendar.WEEK_OF_YEAR, -10);
        long startTime = cal.getTimeInMillis();

        // Build a session read request
        SessionReadRequest readRequest = new SessionReadRequest.Builder()
                .setTimeInterval(startTime, endTime, TimeUnit.MILLISECONDS)
                .read(DataType.TYPE_DISTANCE_DELTA)
                .read(DataType.AGGREGATE_ACTIVITY_SUMMARY)
                .readSessionsFromAllApps()
                .build();
        // [END build_read_session_request]

        return readRequest;
    }

    private void dumpDataSet(DataSet dataSet) {
        Log.i(TAG, "Data returned for Data type: " + dataSet.getDataType().getName());
        for (DataPoint dp : dataSet.getDataPoints()) {
            DateFormat dateFormat = getTimeInstance();
            Log.i(TAG, "Data point:");
            Log.i(TAG, "\tType: " + dp.getDataType().getName());
            Log.i(TAG, "\tStart: " + dateFormat.format(dp.getStartTime(TimeUnit.MILLISECONDS)));
            Log.i(TAG, "\tEnd: " + dateFormat.format(dp.getEndTime(TimeUnit.MILLISECONDS)));
            for(Field field : dp.getDataType().getFields()) {
                Log.i(TAG, "\tField: " + field.getName() + " Value: " + dp.getValue(field));
            }
        }
    }

    private void dumpSession(Session session) {
        DateFormat dateFormat = getTimeInstance();
        Log.i(TAG, "Data returned for Session: " + session.getName()
                + "\n\tDescription: " + session.getDescription()
                + "\n\tStart: " + dateFormat.format(session.getStartTime(TimeUnit.MILLISECONDS))
                + "\n\tEnd: " + dateFormat.format(session.getEndTime(TimeUnit.MILLISECONDS)));
    }

    @ReactMethod
    public void getActivities(ReadableMap options, Promise promise) {
        Log.i(TAG, "getActivities");

        try {
            Activity activity = getCurrentActivity();

            if (activity == null) {
                promise.reject("activity null!?");
                return;
            }

            getActivitiesPromise = promise;

            if (mClient == null) {
                buildFitnessClient();
            } else {
                new InsertAndVerifySessionTask().execute();
            }

        } catch (Error e) {
            promise.reject("getActivities error");
        }
    }
}