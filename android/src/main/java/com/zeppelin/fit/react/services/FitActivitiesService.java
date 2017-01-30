package com.zeppelin.fit.react.services;

import android.util.Log;
import android.content.Context;

// react-native-bridge:
import com.facebook.react.bridge.Promise;
import com.facebook.react.bridge.WritableArray;
import com.facebook.react.bridge.WritableMap;
import com.facebook.react.bridge.Arguments;
import com.facebook.react.bridge.ReadableMap;

// rx-fit:
import com.patloew.rxfit.RxFit;
import rx.Observable;
import rx.Observer;
import rx.functions.Func1;

// google-fit:
import com.google.android.gms.fitness.request.DataReadRequest;
import com.google.android.gms.fitness.result.DataReadResult;
import com.google.android.gms.fitness.data.DataPoint;
import com.google.android.gms.fitness.Fitness;
import com.google.android.gms.common.Scopes;
import com.google.android.gms.common.api.Scope;
import com.google.android.gms.fitness.data.DataType;
import com.google.android.gms.fitness.data.DataSet;
import com.google.android.gms.fitness.data.Field;
import com.google.android.gms.common.api.Api;

import java.util.concurrent.TimeUnit;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Calendar;

// activity
import com.google.android.gms.fitness.data.Session;
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

// response reading:
import java.util.List;
import java.text.SimpleDateFormat;
import java.text.DateFormat;
import static java.text.DateFormat.getTimeInstance;

// helpers:
import com.zeppelin.fit.react.helpers.TimeBounds;

public class FitActivitiesService {

  public static final String TAG = "RCTFitKit";
  private RxFit rxFit;
  private Map<String, String> googleFitToFitKitActivityMap = new HashMap<String, String>();

  public FitActivitiesService(RxFit rxFit, Promise promise, Context context, ReadableMap options) {
    this.rxFit = rxFit;
    initGFToFKMap();
    long[] timeBounds = TimeBounds.getTimeBounds(options);
    readActivities(promise, context, timeBounds);
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

  private String getActivityName(String gfActivityName) {
    String mappedActivity = googleFitToFitKitActivityMap.get(gfActivityName.toUpperCase());
    if (mappedActivity == null) {
      return gfActivityName.toUpperCase();
    }
    return mappedActivity;
  }

  private SessionReadRequest readFitnessSession(long[] timeBounds) {
    Log.i(TAG, "Reading History API results for sessions: ");

    SessionReadRequest readRequest = new SessionReadRequest.Builder()
      .setTimeInterval(timeBounds[0], timeBounds[1], TimeUnit.MILLISECONDS)
      .read(DataType.TYPE_DISTANCE_DELTA)
      // .read(DataType.AGGREGATE_ACTIVITY_SUMMARY)
      .readSessionsFromAllApps()
      .build();

    return readRequest;
  }

  private WritableMap handleDataSet(DataSet dataSet) {
      WritableMap dataSetMap = Arguments.createMap();

      for (DataPoint dp : dataSet.getDataPoints()) {
          DateFormat dateFormat = getTimeInstance();

          for(Field field : dp.getDataType().getFields()) {

              WritableMap fieldMap = Arguments.createMap();

              switch (field.getName()) {
                  case "distance":
                      fieldMap.putString("unit", "km");
                      fieldMap.putDouble("value", dp.getValue(field).asFloat() / 1000);
                      dataSetMap.putMap("distance", fieldMap);
                      break;
                  default:
                      fieldMap.putString("value", dp.getValue(field).toString());
                      dataSetMap.putMap(field.getName(), fieldMap);
              }
          }
      }

      return dataSetMap;
  }

  private void readActivities(final Promise promise, Context context, long[] timeBounds) {
    SessionReadRequest readRequest = readFitnessSession(timeBounds);

    final WritableArray activities = Arguments.createArray();

    rxFit.sessions().read(readRequest)
      .subscribe(new Observer<SessionReadResult>() {
        @Override
        public void onCompleted() {
          Log.i(TAG, "Observable done!");
          promise.resolve(activities);
        }

        @Override
        public void onError(Throwable e) {
          Log.e(TAG, "ooops error");
          e.printStackTrace();
          promise.reject("getActivities error!");
        }

        @Override
        public void onNext(SessionReadResult sessionReadResult) {

          for (Session session : sessionReadResult.getSessions()) {
            String activityName = getActivityName(session.getActivity());

            if (activityName != "IGNORE") {
              List<DataSet> dataSets = sessionReadResult.getDataSet(session);
              WritableMap dataSetMap = Arguments.createMap();
              for (DataSet dataSet : dataSets) {
                dataSetMap.merge(handleDataSet(dataSet));
              }

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
              if (startTime != endTime) {
                WritableMap duration = Arguments.createMap();
                duration.putString("unit", "sec");
                duration.putInt("value", (int) (long) (session.getEndTime(TimeUnit.SECONDS) - session.getStartTime(TimeUnit.SECONDS)));
                activityBody.putMap("duration", duration);

                WritableMap timeInterval = Arguments.createMap();
                timeInterval.putString("start_date_time", startTime);
                timeInterval.putString("end_date_time", endTime);
                effectiveTimeFrame.putMap("time_interval", timeInterval);
              } else {
                effectiveTimeFrame.putString("date_time", startTime);
              }
              activityBody.putMap("effective_time_frame", effectiveTimeFrame);
              activityBody.merge(dataSetMap);

              activity.putMap("header", activityHeader);
              activity.putMap("body", activityBody);

              activities.pushMap(activity);
            }
          }
        }
      });
  }
}
