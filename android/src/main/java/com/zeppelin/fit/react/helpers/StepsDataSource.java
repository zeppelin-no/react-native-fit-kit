package com.zeppelin.fit.react.helpers;

import com.google.android.gms.fitness.data.DataSource;
import com.google.android.gms.fitness.data.DataType;

public class StepsDataSource {

  private static DataSource ESTIMATED_STEP_DELTAS;

  private static void buildStepsDataSource () {
    ESTIMATED_STEP_DELTAS = new DataSource.Builder()
      .setDataType(DataType.TYPE_STEP_COUNT_DELTA)
      .setType(DataSource.TYPE_DERIVED)
      .setStreamName("estimated_steps")
      .setAppPackageName("com.google.android.gms")
      .build();
  }

  public static DataSource get() {
    if (ESTIMATED_STEP_DELTAS == null) {
      buildStepsDataSource();
    }

    return ESTIMATED_STEP_DELTAS;
  }
}
