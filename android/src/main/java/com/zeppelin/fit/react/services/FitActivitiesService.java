package com.zeppelin.fit.react.services;

import android.util.Log;
import android.content.Context;

// react-native-bridge:
import com.facebook.react.bridge.Promise;
import com.facebook.react.bridge.WritableArray;
import com.facebook.react.bridge.WritableMap;
import com.facebook.react.bridge.Arguments;
import com.facebook.react.bridge.ReadableMap;
import com.facebook.react.bridge.ReadableType;

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
import com.google.android.gms.fitness.FitnessActivities;
import com.google.android.gms.fitness.data.DataType;
import com.google.android.gms.fitness.data.DataSet;
import com.google.android.gms.fitness.data.Field;
import com.google.android.gms.common.api.Api;

import java.util.concurrent.TimeUnit;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Calendar;
import java.util.List;

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
import com.google.android.gms.fitness.data.Bucket;

// response reading:
import java.util.List;
import java.text.SimpleDateFormat;
import java.text.DateFormat;
import static java.text.DateFormat.getTimeInstance;

// helpers:
import com.zeppelin.fit.react.helpers.TimeBounds;
import com.zeppelin.fit.react.helpers.LogH;

public class FitActivitiesService {

  private RxFit rxFit;
  private Map<String, String> googleFitToFitKitActivityMap = new HashMap<String, String>();
  private Map<String, String> googleFitToFitKitAutomaticActivityMap = new HashMap<String, String>();
  private Promise mActivityPromise;
  private final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ");
  private int autoActivitiesMinDuration = 10;

  public FitActivitiesService(RxFit rxFit, Promise promise, Context context, ReadableMap options) {
    this.rxFit = rxFit;

    LogH.breaker();
    LogH.i("starting FitActivitiesService");

    initGFToFKMap();
    initGFAutomaticToFKMap();

    boolean autoActivities = false;
    if (options.hasKey("gf_autoActivities") && options.getType("gf_autoActivities") == ReadableType.Boolean) {
      autoActivities = options.getBoolean("gf_autoActivities");
    }

    if (options.hasKey("gf_autoActivities_min_duration") && options.getType("gf_autoActivities_min_duration") == ReadableType.Number) {
      this.autoActivitiesMinDuration = options.getInt("gf_autoActivities_min_duration");
    }

    long[] timeBounds = TimeBounds.getTimeBounds(options, false);
    readActivities(promise, timeBounds, autoActivities);
  }

  private void initGFAutomaticToFKMap() {
    googleFitToFitKitAutomaticActivityMap.put("STILL", "IGNORE");
    googleFitToFitKitAutomaticActivityMap.put("UNKNOWN", "IGNORE");
    googleFitToFitKitAutomaticActivityMap.put("WALKING", "WALKING");
    googleFitToFitKitAutomaticActivityMap.put("RUNNING", "RUNNING");
    googleFitToFitKitAutomaticActivityMap.put("ON_FOOT", "WALKING");
    googleFitToFitKitAutomaticActivityMap.put("ON_BICYCLE", "CYCLING");
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
      googleFitToFitKitActivityMap.put("STRENGTH_TRAINING", "TRADITIONAL_STRENGTH_TRAINING");
      googleFitToFitKitActivityMap.put("SURFING", "SURFING_SPORTS");
      googleFitToFitKitActivityMap.put("SWIMMING.OPEN_WATER", "SWIMMING");
      googleFitToFitKitActivityMap.put("SWIMMING.POOL", "SWIMMING");
      googleFitToFitKitActivityMap.put("TEAM_SPORTS", "OTHER");
      googleFitToFitKitActivityMap.put("TREADMILL", "RUNNING");
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
      googleFitToFitKitActivityMap.put("UNKNOWN", "OTHER");
      googleFitToFitKitActivityMap.put("STILL", "IGNORE");

      // automatic
      // googleFitToFitKitActivityMap.put("UNKNOWN", "IGNORE");
      // googleFitToFitKitActivityMap.put("WALKING", "WALKING");
      // googleFitToFitKitActivityMap.put("RUNNING", "RUNNING");
      // googleFitToFitKitActivityMap.put("ON_FOOT", "WALKING");
      // googleFitToFitKitActivityMap.put("ON_BICYCLE", "CYCLING");
  }

  private String getActivityName(String gfActivityName) {
    String mappedActivity = googleFitToFitKitActivityMap.get(gfActivityName.toUpperCase());
    // if (mappedActivity == null) {
    //   return gfActivityName.toUpperCase();
    // }
    return mappedActivity;
  }

  private String getAutoActivityName(String gfActivityName) {
    String mappedActivity = googleFitToFitKitAutomaticActivityMap.get(gfActivityName.toUpperCase());
    // if (mappedActivity == null) {
    //   return gfActivityName.toUpperCase();
    // }
    return mappedActivity;
  }

  private SessionReadRequest readFitnessSession(long[] timeBounds) {
    SessionReadRequest readRequest = new SessionReadRequest.Builder()
      .setTimeInterval(timeBounds[0], timeBounds[1], TimeUnit.MILLISECONDS)
      .read(DataType.TYPE_DISTANCE_DELTA)
      .read(DataType.AGGREGATE_CALORIES_EXPENDED)
      // .read(DataType.AGGREGATE_ACTIVITY_SUMMARY)
      .readSessionsFromAllApps()
      .enableServerQueries()
      .build();

    return readRequest;
  }

  private DataReadRequest readFitnessHistory(long[] timeBounds) {
    LogH.i("Reading History API results for sessions: ");

    DataReadRequest readRequest = new DataReadRequest.Builder()
      .setTimeRange(timeBounds[0], timeBounds[1], TimeUnit.MILLISECONDS)
      .aggregate(DataType.TYPE_ACTIVITY_SEGMENT, DataType.AGGREGATE_ACTIVITY_SUMMARY)
      // .read(DataType.TYPE_DISTANCE_DELTA)
      // .read(DataType.AGGREGATE_CALORIES_EXPENDED)
      // .bucketBySession(1, TimeUnit.MILLISECONDS)
      .bucketByActivitySegment(autoActivitiesMinDuration, TimeUnit.MINUTES)

      // .aggregate(DataType.TYPE_ACTIVITY_SAMPLES, DataType.AGGREGATE_ACTIVITY_SUMMARY)
      // .bucketByActivityType(1, TimeUnit.MILLISECONDS)

      // .bucketByTime(1, TimeUnit.DAYS)
      // .bucketByTime(1, TimeUnit.MINUTES)
      // .read(DataType.TYPE_ACTIVITY_SEGMENT)
      // .read(DataType.TYPE_DISTANCE_DELTA)
      // .read(DataType.AGGREGATE_CALORIES_EXPENDED)
      // .read(DataType.AGGREGATE_ACTIVITY_SUMMARY)
      .enableServerQueries()
      .build();

    return readRequest;
  }

  private WritableMap handleDataSet(DataSet dataSet) {
    WritableMap dataSetMap = Arguments.createMap();

    double totalDistance = 0;

    for (DataPoint dp : dataSet.getDataPoints()) {
      DateFormat dateFormat = getTimeInstance();

      for(Field field : dp.getDataType().getFields()) {

        WritableMap fieldMap = Arguments.createMap();

        switch (field.getName()) {
          case "distance":
            totalDistance += dp.getValue(field).asFloat() / 1000;
            break;
          case "calories":
            fieldMap.putString("unit", "kcal");
            fieldMap.putString("value", dp.getValue(field).toString());
            dataSetMap.putMap("calories", fieldMap);
            break;
          default:
            fieldMap.putString("value", dp.getValue(field).toString());
            dataSetMap.putMap(field.getName(), fieldMap);
        }
      }
    }

    if (totalDistance > 0) {
      WritableMap distanceMap = Arguments.createMap();
      distanceMap.putString("unit", "km");
      distanceMap.putDouble("value", totalDistance);
      dataSetMap.putMap("distance", distanceMap);
    }

    return dataSetMap;
  }

  private boolean sessionReadDone = false;
  private boolean historyReadDone = false;
  private int notStill = 0;

  private void readActivities(final Promise promise, long[] timeBounds, final boolean autoActivities) {
    mActivityPromise = promise;

    final WritableArray activities = Arguments.createArray();

    if (autoActivities == true) {
      DataReadRequest readRequestHistory = readFitnessHistory(timeBounds);

      rxFit.history().read(readRequestHistory)
        .flatMapObservable(new Func1<DataReadResult, Observable<Bucket>>() {
            @Override
            public Observable<Bucket> call(DataReadResult dataReadResult) {
              List<Bucket> buckets = dataReadResult.getBuckets();
              LogH.i("Auto-activity-buckets: " + buckets.size());
              return Observable.from(dataReadResult.getBuckets());
            }
          })
        .subscribe(new Observer<Bucket>() {
          @Override
          public void onCompleted() {
            LogH.i("read autoActivities observable done!");
            if (sessionReadDone) {
              if (mActivityPromise != null) {
                mActivityPromise.resolve(activities);
                mActivityPromise = null;
              }
            }

            historyReadDone = true;
          }

          @Override
          public void onError(Throwable e) {
            LogH.e("readActivities observable error");
            e.printStackTrace();
            if (mActivityPromise != null) {
              mActivityPromise.reject("getActivities error!", e);
              mActivityPromise = null;
            }

            historyReadDone = true;
          }

          // @Override
          public void onNext(Bucket bucket) {
            LogH.breakerSmall();
            // LogH.i("autoActivities onNext");
            //
            // LogH.i("autoActivities getActivity: " + bucket.getActivity());
            String activityName = getAutoActivityName(bucket.getActivity());

            if (activityName != null && activityName != "IGNORE") {
              long startTime = bucket.getStartTime(TimeUnit.MILLISECONDS);
              long endTime = bucket.getEndTime(TimeUnit.MILLISECONDS);

              String identifier = startTime + "-" + bucket.hashCode();

              WritableMap activity = Arguments.createMap();
              WritableMap activityHeader = makeHeader(identifier, startTime);
              WritableMap activityBody = Arguments.createMap();

              WritableMap timeFrame = makeEffectiveTimeFrameMap(startTime, endTime);
              activityBody.merge(timeFrame);

              for (DataSet dataSet : bucket.getDataSets()) {
                LogH.breaker();
                LogH.e("a dataSet");

                for (DataPoint dataPoint : dataSet.getDataPoints()) {
                  LogH.breakerSmall();
                  LogH.e("a datapoint");

                  DataType dataType = dataPoint.getDataType();
                  for(Field field : dataType.getFields()) {
                    LogH.e("wiiiiii field name: " + field.getName());
                  }
                }
              }

              activityBody.putString("activity_name", activityName);
              activity.putMap("header", activityHeader);
              activity.putMap("body", activityBody);

              if (mActivityPromise != null) {
                activities.pushMap(activity);
              }
            }
          }
        });
    }

    SessionReadRequest readRequest = readFitnessSession(timeBounds);

    rxFit.sessions().read(readRequest)
      .subscribe(new Observer<SessionReadResult>() {
        @Override
        public void onCompleted() {
          LogH.i("readActivities observable done!");
          if (autoActivities == false || historyReadDone) {
            if (mActivityPromise != null) {
              mActivityPromise.resolve(activities);
              mActivityPromise = null;
            }
          }

          sessionReadDone = true;
        }

        @Override
        public void onError(Throwable e) {
          LogH.e("readActivities observable error");
          e.printStackTrace();
          if (mActivityPromise != null) {
            mActivityPromise.reject("getActivities error!", e);
            mActivityPromise = null;
          }

          sessionReadDone = true;
        }

        @Override
        public void onNext(SessionReadResult sessionReadResult) {
          List<Session> sessions = sessionReadResult.getSessions();
          LogH.i("Activity-sessions: " + sessions.size());
          for (Session session : sessions) {
            String activityName = getActivityName(session.getActivity());

            if (activityName != null && activityName != "IGNORE") {
              List<DataSet> dataSets = sessionReadResult.getDataSet(session);
              WritableMap dataSetMap = Arguments.createMap();
              for (DataSet dataSet : dataSets) {
                dataSetMap.merge(handleDataSet(dataSet));
              }

              long startTime = session.getStartTime(TimeUnit.MILLISECONDS);
              long endTime = session.getEndTime(TimeUnit.MILLISECONDS);

              WritableMap activity = Arguments.createMap();
              WritableMap activityHeader = makeHeader(session.getIdentifier(), startTime);
              WritableMap activityBody = Arguments.createMap();


              // body
              WritableMap timeFrame = makeEffectiveTimeFrameMap(startTime, endTime);
              activityBody.merge(timeFrame);
              activityBody.putString("activity_name", activityName);
              activityBody.merge(dataSetMap);

              activity.putMap("header", activityHeader);
              activity.putMap("body", activityBody);

              if (mActivityPromise != null) {
                activities.pushMap(activity);
              }
            }
          }
        }
      });
  }

  private WritableMap makeHeader(String identifier, long startTime) {
    WritableMap activityHeader = Arguments.createMap();

    WritableMap schemaId = Arguments.createMap();
    activityHeader.putString("id", identifier);
    activityHeader.putString("creation_date_time", dateFormat.format(startTime));
    schemaId.putString("name", "physical-activity");
    schemaId.putString("namespace", "omh");
    schemaId.putString("version", "1.2");
    activityHeader.putMap("schema_id", schemaId);

    return activityHeader;
  }

  private WritableMap makeEffectiveTimeFrameMap(long startTime, long endTime) {
    String startTimeFormated = dateFormat.format(startTime);
    String endTimeFormated = dateFormat.format(endTime);

    WritableMap activityBody = Arguments.createMap();

    WritableMap effectiveTimeFrame = Arguments.createMap();

    if (startTime != endTime) {
      WritableMap duration = Arguments.createMap();
      duration.putString("unit", "sec");
      duration.putInt("value", (int) (long) ((endTime - startTime) / 1000));
      activityBody.putMap("duration", duration);

      WritableMap timeInterval = Arguments.createMap();
      timeInterval.putString("start_date_time", startTimeFormated);
      timeInterval.putString("end_date_time", endTimeFormated);
      effectiveTimeFrame.putMap("time_interval", timeInterval);
    } else {
      effectiveTimeFrame.putString("date_time", startTimeFormated);
    }

    activityBody.putMap("effective_time_frame", effectiveTimeFrame);

    return activityBody;
  }
}
