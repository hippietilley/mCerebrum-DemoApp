/*
 * Copyright (c) 2018, The University of Memphis, MD2K Center of Excellence
 *
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * * Redistributions of source code must retain the above copyright notice, this
 * list of conditions and the following disclaimer.
 *
 * * Redistributions in binary form must reproduce the above copyright notice,
 * this list of conditions and the following disclaimer in the documentation
 * and/or other materials provided with the distribution.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE
 * FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL
 * DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER
 * CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
 * OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package org.md2k.demoapp;

// Android imports
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.ToggleButton;

// Java imports
import java.util.ArrayList;

// DataKitAPI imports
import org.md2k.datakitapi.DataKitAPI;
import org.md2k.datakitapi.datatype.DataType;
import org.md2k.datakitapi.datatype.DataTypeDoubleArray;
import org.md2k.datakitapi.exception.DataKitException;
import org.md2k.datakitapi.messagehandler.OnConnectionListener;
import org.md2k.datakitapi.messagehandler.OnReceiveListener;
import org.md2k.datakitapi.source.application.Application;
import org.md2k.datakitapi.source.application.ApplicationBuilder;
import org.md2k.datakitapi.source.datasource.DataSourceBuilder;
import org.md2k.datakitapi.source.datasource.DataSourceClient;
import org.md2k.datakitapi.source.datasource.DataSourceType;
import org.md2k.datakitapi.time.DateTime;

public class MainActivity extends AppCompatActivity implements SensorEventListener {

    // Variables for accelerometer data
    private SensorManager mSensorManager;
    private Sensor mSensor = null;
    private long lastSaved;
    private double minSampleTime = 100; // 100 milliseconds
    public static final double GRAVITY = 9.81;

    // Variables for DataKit objects
    private DataKitAPI datakitapi;
    private DataSourceClient dataSourceClientRegister = null;
    private DataSourceClient dataSourceClientSubscribe = null;
    private DataTypeDoubleArray dataTypeSubscribe = null;
    private ArrayList<DataType> dataTypeQuery = null;
    private DataTypeDoubleArray dataTypeDoubleArray;
    private Boolean isHF;

    // Variables for the user view
    private TextView conButton;
    private TextView regButton;
    private TextView subButton;
    private TextView insButton;
    private TextView output;
    private TextView insOutput;
    private ToggleButton hfSwitch;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Initializes button variables
        conButton = findViewById(R.id.conButton);
        regButton = findViewById(R.id.regButton);
        subButton = findViewById(R.id.subButton);
        insButton = findViewById(R.id.insButton);
        output = findViewById(R.id.outputTextView);
        insOutput = findViewById(R.id.insertTextView);
        hfSwitch = findViewById(R.id.hfSwitch);
        setHFSwitch();
        printMessage(isHF.toString(), output);

        // Gets sensor service
        mSensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);

        // Sets the desired sensor
        mSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        lastSaved = DateTime.getDateTime();

        datakitapi = datakitapi.getInstance(this);
    }

    public Application buildApplication() {
        return new ApplicationBuilder().setId(MainActivity.this.getPackageName()).build();
    }

    public DataSourceBuilder buildDataSource(Application application) {
        return new DataSourceBuilder().setType(DataSourceType.ACCELEROMETER).setApplication(application);
    }

    public DataSourceBuilder buildDataSource() {
        return new DataSourceBuilder().setType(DataSourceType.ACCELEROMETER);
    }

    public void setHFSwitch(View view) {
        hfSwitch.toggle();
        printMessage(isHF.toString(), output);
    }

    public void setHFSwitch() {
        hfSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                if (b)
                    isHF = true;
                else
                    isHF = false;
                printMessage(isHF.toString(), output);
            }
        });
    }

    public void connectButton(View view) {
        try {
            if (datakitapi.isConnected()) {
                disconnectDataKit();
            } else
                datakitapi.connect(new OnConnectionListener() {
                @Override
                public void onConnected() {
                    printMessage(R.string.dataKitConnected, output);
                    conButton.setText(R.string.disconnect_button);
                }
            });
        } catch (DataKitException ignored) {}
    }

    public void disconnectDataKit() {
        unsubscribeDataSource();
        unregisterButton(false);
        datakitapi.disconnect();
        dataSourceClientRegister = null;
        dataSourceClientSubscribe = null;
        dataTypeSubscribe = null;
        dataTypeQuery = null;
        printMessage(R.string.dataKitDisconnected, output);
        conButton.setText(R.string.connect_button);
    }

    public void registerButton(View view) {
        try {
            if (!(datakitapi.isConnected())) {
                printMessage(R.string.errorNotConnected, output);
            }
            else if (dataSourceClientRegister == null) {
                dataSourceClientRegister = datakitapi.register(buildDataSource());
                regButton.setText(R.string.unregister_button);
                printMessage(dataSourceClientRegister.getDataSource().getType() +
                        " registration successful", output);
            } else {
                unregisterButton(false);
            }
        } catch (DataKitException ignored) {
            unregisterButton(true);
        }
    }

    public void unregisterButton(boolean failed) {
        try {
            unregisterListener();
            unsubscribeDataSource();
            datakitapi.unregister(dataSourceClientRegister);
            dataSourceClientRegister = null;
            regButton.setText(R.string.register_button);

        } catch (DataKitException ignored){}
        if (failed)
            printMessage(dataSourceClientRegister.getDataSource().getType() +
                    " registration failed", output);
        else
            printMessage(R.string.dataSourceUnregistered, output);
    }

    public void unregisterListener() {
        mSensorManager.unregisterListener(this, mSensor);
        insButton.setText(R.string.insert_button);
        insOutput.setText("");
    }

    public void subscribeButton (View view){
        ArrayList<DataSourceClient> dataSourceClients;
        try {
            if (dataSourceClientSubscribe == null) {
                dataSourceClients = datakitapi.find(buildDataSource(buildApplication()));
                if(dataSourceClients.size() == 0) {
                    printMessage(R.string.errorNotRegistered, output);
                } else {
                    dataSourceClientSubscribe = dataSourceClients.get(0);
                    // gets index 0 because there should only be one in this application
                    datakitapi.subscribe(dataSourceClientSubscribe, subscribeListener);
                    subButton.setText(R.string.unsubscribe_button);
                    printMessage(R.string.dataSourceSubscribed, output);
                }
            } else {
                unsubscribeDataSource();
            }
        } catch (DataKitException ignored) {
            subButton.setText(R.string.subscribe_button);
            dataSourceClientSubscribe = null;
            dataTypeSubscribe = null;
        }
    }

    public OnReceiveListener subscribeListener = new OnReceiveListener() {
        @Override
        public void onReceived(DataType dataType) {
            printSample((DataTypeDoubleArray) dataType, output);
        }
    };

    public void unsubscribeDataSource() {
        try {
            datakitapi.unsubscribe(dataSourceClientSubscribe);
            dataSourceClientSubscribe = null;
            dataTypeSubscribe = null;
            subButton.setText(R.string.subscribe_button);
            printMessage(R.string.dataSourceUnsubscribed, output);
        } catch (DataKitException ignored) {}
    }

    public void insertButton (View view){
        if(dataSourceClientRegister != null) {
            mSensorManager.registerListener(this, mSensor, SensorManager.SENSOR_DELAY_NORMAL);
            insButton.setText(R.string.inserting);
        } else {
            printMessage(R.string.errorNotRegistered, output);
        }
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        long curTime = DateTime.getDateTime();
        if ((double)(curTime - lastSaved) > minSampleTime) {
            lastSaved = curTime;
            double[] samples = new double[3];
            samples[0] = event.values[0] / GRAVITY; // X axis
            samples[1] = event.values[1] / GRAVITY; // Y axis
            samples[2] = event.values[2] / GRAVITY; // Z axis
            dataTypeDoubleArray = new DataTypeDoubleArray(curTime, samples);
            if (isHF)
                insertHFData(dataTypeDoubleArray);
            else
                insertData(dataTypeDoubleArray);
        }
    }

    public void insertData(DataTypeDoubleArray data) {
        try {
            datakitapi.insert(dataSourceClientRegister, data);
            printSample(data, insOutput);
        } catch (DataKitException ignored) {
            Log.e("database insert", ignored.getMessage());
        }
    }

    public void insertHFData(DataTypeDoubleArray data) {
        try {
            datakitapi.insertHighFrequency(dataSourceClientRegister, data);
            printSample(data, insOutput);
        } catch (DataKitException ignored) {
            Log.e("hf data insert", ignored.getMessage());
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int x) {}

    public void queryButton (View view){
        datakitapi = datakitapi.getInstance(this);
        unsubscribeDataSource(); // Without this it was crashing
        try {
            ArrayList<DataSourceClient> dataSourceClients = datakitapi.find(buildDataSource(buildApplication()));
            if (dataSourceClients.size() == 0) {
                printMessage(R.string.errorNotRegistered, output);
            } else {
                dataTypeQuery = datakitapi.query(dataSourceClients.get(0), 3);
                if (dataTypeQuery.size() == 0) {
                    printMessage("query size zero", output);
                } else
                    printQuery(dataTypeQuery);
            }
        } catch (DataKitException ignored) {
            Log.e("query", ignored.getMessage());
            dataTypeQuery = null;
        }
    }

    public void printQuery (ArrayList<DataType> query) {
        StringBuilder message = new StringBuilder();
        message.append("[X axis, Y axis, Z axis]\n");
        for (DataType data : query) {
            if (data instanceof DataTypeDoubleArray) {
                DataTypeDoubleArray dataArray = (DataTypeDoubleArray)data;
                double[] sample = dataArray.getSample();
                message.append("[" + sample[0] + ", " + sample[1] + ", " + sample[2] + "]\n");
            }
        }
        printMessage(message.toString(), output);
    }

    public void printSample(DataTypeDoubleArray data, TextView output) {
        double[] sample = data.getSample();
        printMessage("[" + sample[0] + ", " + sample[1] + ", " + sample[2] + "]", output);
    }

    public void printMessage (int message, TextView output) {
        output.setText(message);
    }
    public void printMessage (String message, TextView output) {
        output.setText(message);
    }
}