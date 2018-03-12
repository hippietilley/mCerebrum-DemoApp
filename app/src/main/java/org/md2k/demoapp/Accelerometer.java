package org.md2k.demoapp;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorManager;

import org.md2k.datakitapi.source.datasource.DataSource;
import org.md2k.datakitapi.source.datasource.DataSourceClient;
import org.md2k.datakitapi.source.datasource.DataSourceType;
import org.md2k.datakitapi.time.DateTime;

public class Accelerometer extends DataSource {
    // Variables for accelerometer data
    private SensorManager mSensorManager;
    private Sensor mSensor = null;
    long lastSaved = DateTime.getDateTime();
    double minSampleTime = 100; // 100 milliseconds
    public static final double GRAVITY = 9.81;
    Context context;
    private final String dataSourceType;

    public Accelerometer(Context context) {
        this.context = context;
        this.dataSourceType = DataSourceType.ACCELEROMETER;
    }


}
