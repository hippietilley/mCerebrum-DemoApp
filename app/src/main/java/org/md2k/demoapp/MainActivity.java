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
import android.content.pm.ActivityInfo;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Switch;
import android.widget.TextView;

// Java imports
import java.util.ArrayList;

// DataKitAPI imports
import org.md2k.datakitapi.DataKitAPI;
import org.md2k.datakitapi.datatype.DataType;
import org.md2k.datakitapi.datatype.DataTypeDoubleArray;
import org.md2k.datakitapi.datatype.DataTypeLong;
import org.md2k.datakitapi.exception.DataKitException;
import org.md2k.datakitapi.messagehandler.OnConnectionListener;
import org.md2k.datakitapi.messagehandler.OnReceiveListener;
import org.md2k.datakitapi.source.application.Application;
import org.md2k.datakitapi.source.application.ApplicationBuilder;
import org.md2k.datakitapi.source.datasource.DataSourceBuilder;
import org.md2k.datakitapi.source.datasource.DataSourceClient;
import org.md2k.datakitapi.source.datasource.DataSourceType;
import org.md2k.datakitapi.time.DateTime;

/**
 * This application demonstrates how to connect to and make API calls against DataKit via DataKitAPI.
 */
public class MainActivity extends AppCompatActivity implements SensorEventListener {

    // Variables for accelerometer data
    private SensorManager mSensorManager;
    private Sensor mSensor = null;
    private long lastSaved;
    private double minSampleTime = 100; // 100 milliseconds
    public static final double GRAVITY = 9.81;

    // Variables for DataKit objects
    private DataKitAPI datakitapi;
    private DataSourceClient regDataSourceClient = null;
    private DataSourceClient subDataSourceClient = null;
    private ArrayList<DataType> dataTypeQuery = null;
    private DataTypeDoubleArray dataTypeDoubleArray;
    private DataTypeLong querySize;
    private Boolean isHF;

    // Variables for the user view
    private TextView conButton;
    private TextView regButton;
    private TextView subButton;
    private TextView insButton;
    private TextView output;
    private TextView subOutput;
    private Switch hfSwitch;

    /**
     * Upon creation, the buttons, <code>SensorManager</code>, <code>Sensor</code>, and current datetime
     * are initialized. An instance of DataKit is also retrieved/created.
     * @param savedInstanceState Previous state of the application if available.
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        setContentView(R.layout.activity_main);

        // Initializes button variables
        conButton = findViewById(R.id.conButton);
        regButton = findViewById(R.id.regButton);
        subButton = findViewById(R.id.subButton);
        insButton = findViewById(R.id.insButton);
        output = findViewById(R.id.outputTextView);
        subOutput = findViewById(R.id.subTextView);
        hfSwitch = findViewById(R.id.hfSwitch);
        isHF = hfSwitch.isChecked();

        // Gets sensor service
        mSensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);

        // Sets the desired sensor
        mSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        lastSaved = DateTime.getDateTime();

        datakitapi = datakitapi.getInstance(this);
    }

    /**
     * Builds a new application object. This object represents this application and helps
     * identify data within the database.
     * @return Application datatype
     */
    public Application buildApplication() {
        return new ApplicationBuilder().setId(MainActivity.this.getPackageName()).build();
    }

    /**
     * Builds a data source object representing the sensor and application creating the data source.
     * This demo application only uses the accelerometer, but any available sensor, hardware or software
     * based, can be used.
     * @param application Application object representing this application.
     * @return A data source builder
     */
    public DataSourceBuilder buildDataSource(Application application) {
        return new DataSourceBuilder().setType(DataSourceType.ACCELEROMETER).setApplication(application);
    }

    /**
     * Builds a data source object representing the sensor. In this application this is only used for
     * registering the data source. It could be used to find all data sources of the set type independent
     * from the application.
     * @return A data source builder
     */
    public DataSourceBuilder buildDataSource() {
        return new DataSourceBuilder().setType(DataSourceType.ACCELEROMETER);
    }

    /**
     * Switch mechanism for switching between <code>insertData()</code> and <code>insertHFData</code>.
     * This is not necessary in a typical application, but used to demonstrate the difference between
     * these two data insert methods.
     * @param view hfSwitch
     */
    public void setHFSwitch(View view) {
        isHF = hfSwitch.isChecked();
    }

    /**
     * Controls behavior of connecting and disconnecting from DataKit. DataKit must be connected before
     * any other methods are called. Not doing so will result in <code>DataKitException</code>s which
     * must be handled. <code>DataKitAPI.connect(new OnConnectionListener()</code> registers a callback
     * interface so that this application and DataKit can communicate.
     * @param view conButton
     */
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
        } catch (DataKitException ignored) {
            printMessage(ignored.getMessage(), output);
        }
    }

    /**
     * Before DataKit is disconnected, all data sources must be unsubscribed and unregistered.
     * Exception checking is not required when calling <code>DataKitAPI.disconnect()</code>.
     */
    public void disconnectDataKit() {
        unsubscribeDataSource();
        unregisterButton(false);
        datakitapi.disconnect();
        regDataSourceClient = null;
        subDataSourceClient = null;
        dataTypeQuery = null;
        printMessage(R.string.dataKitDisconnected, output);
        conButton.setText(R.string.connect_button);
    }

    /**
     * Registers the data source with DataKit. DataKit can not receive data from a data source until
     * that data source is registered.
     * @param view regButton
     */
    public void registerButton(View view) {
        try {
            if (!(datakitapi.isConnected())) {
                printMessage(R.string.errorNotConnected, output);
            }
            else if (regDataSourceClient == null) {
                regDataSourceClient = datakitapi.register(buildDataSource());
                regButton.setText(R.string.unregister_button);
                printMessage(regDataSourceClient.getDataSource().getType() +
                        " registration successful", output);
            } else {
                unregisterButton(false);
            }
        } catch (DataKitException ignored) {
            unregisterButton(true);
            printMessage(ignored.getMessage(), output);
        }
    }

    /**
     * Unregistering the sensor listener stops the data collection. Unsubscibing the data source removes
     * any remaining callbacks. Then unregistering the data source from DataKit can be done. It is possible
     * to unregister a subset of registered data sources, but <code>DataKitAPI.unregister()</code> only
     * takes one <code>DataSourceClient</code> as a parameter so the method would need to be called
     * individually for each data source.
     * @param failed Whether this method was called because data source registration failed or not.
     *               Only used for error message handling.
     */
    public void unregisterButton(boolean failed) {
        try {
            unregisterListener();
            unsubscribeDataSource();
            datakitapi.unregister(regDataSourceClient);
            regDataSourceClient = null;
            regButton.setText(R.string.register_button);
        } catch (DataKitException ignored){
            printMessage(ignored.getMessage(), output);
        }
        if (failed)
            printMessage(regDataSourceClient.getDataSource().getType() +
                    " registration failed", output);
        else
            printMessage(R.string.dataSourceUnregistered, output);
    }

    /**
     * Unregisters the sensor listener. To pass <code>this</code> as the sensor listener, the class
     * must implement <code>SensorEventListener</code>.
     */
    public void unregisterListener() {
        mSensorManager.unregisterListener(this, mSensor);
        insButton.setText(R.string.insert_button);
        subOutput.setText("");
    }

    /**
     * Subscribing a data source registers a callback interface that returns the data received by
     * the database. In this implementation <code>dataSourceClients</code> only has one node because
     * the only sensor that is registered is the accelerometer. In production the resulting arraylist
     * is likely to have many more nodes.
     * @param view subButton
     */
    public void subscribeButton (View view){
        ArrayList<DataSourceClient> dataSourceClients;
        try {
            if (subDataSourceClient == null) {
                dataSourceClients = datakitapi.find(buildDataSource(buildApplication()));
                if(dataSourceClients.size() == 0) {
                    printMessage(R.string.errorNotRegistered, output);
                } else {
                    subDataSourceClient = dataSourceClients.get(0);
                    // gets index 0 because there should only be one in this application
                    datakitapi.subscribe(subDataSourceClient, subscribeListener);
                    subButton.setText(R.string.unsubscribe_button);
                    printMessage(R.string.dataSourceSubscribed, output);
                }
            } else {
                unsubscribeDataSource();
            }
        } catch (DataKitException ignored) {
            subButton.setText(R.string.subscribe_button);
            subDataSourceClient = null;
            printMessage(ignored.getMessage(), output);
        }
    }

    /**
     * <code>OnReceiveListener</code> used for subscription. This demo application simply displays the
     * data to an output text view.
     */
    public OnReceiveListener subscribeListener = new OnReceiveListener() {
        @Override
        public void onReceived(DataType dataType) {
            printSample((DataTypeDoubleArray) dataType, subOutput);
        }
    };

    /**
     * Unsubscribing a data source only unregisters the callback interface. Data is still collected
     * and inserted. Nullifying the associated <code>DataSourceClient</code> variable prevents conflicts
     * if the data source is subscribed again.
     */
    public void unsubscribeDataSource() {
        try {
            datakitapi.unsubscribe(subDataSourceClient);
            subDataSourceClient = null;
            subButton.setText(R.string.subscribe_button);
            printMessage(R.string.dataSourceUnsubscribed, output);
        } catch (DataKitException ignored) {
            printMessage(ignored.getMessage(), output);
        }
    }

    /**
     * In this implementation, pressing the insert button only registers the sensor listener. All data
     * collection occurs in the overridden <code>onSensorChanged()</code> method.
     * @param view insButton
     */
    public void insertButton (View view){
        if(regDataSourceClient != null) {
            mSensorManager.registerListener(this, mSensor, SensorManager.SENSOR_DELAY_NORMAL);
            insButton.setText(R.string.inserting);
        } else {
            printMessage(R.string.errorNotRegistered, output);
        }
    }

    /**
     * To limit the frequency of samples a minimum sample time of 100 milliseconds. The accelerometer
     * is sampled at 10 hertz and is adjusted for gravity before being passed into a new
     * <code>DataTypeDoubleArray</code>. An appropriate <code>DataType</code> for the sensor should
     * be used. For example, motion sensors should use <code>DataTypeDoubleArray</code> because they
     * return an array of double values. The proximity sensor and other environmental sensors should
     * use <code>DataTypeDouble</code>, as they return an array with only one value.
     * @param event Value of the new accelerometer data.
     */
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

    /**
     * Data is inserted into the database using the associated <code>DataSourceClient</code>, which
     * provides relevant metadata, and the data sample itself. This demo also displays the data that
     * is inserted, but that is not necessary. The standard insertion method adds rows to a database
     * that is stored in <code>Android/Data/org.md2k.datakit/files/database.db</code> by default.
     * Using <code>insertHighFrequency()</code> is recommended for sensors that produce a lot of data,
     * such as the accelerometer, to help manage the size of the database.
     * @param data Data to insert into the database
     */
    public void insertData(DataTypeDoubleArray data) {
        try {
            datakitapi.insert(regDataSourceClient, data);
        } catch (DataKitException ignored) {
            Log.e("database insert", ignored.getMessage());
            printMessage(ignored.getMessage(), output);
        }
    }

    /**
     * Data is passed to <code>insertHighFrequency()</code> similarly to <code>insert()</code>. The
     * difference is that the high frequency data is stored in a gzipped csv file that is stored in
     * <code>Android/Data/org.md2k.datakit/files/raw/</code> by default. Recording the data in this
     * way helps reduce the amount of resources DataKit requires.
     * @param data Data to record.
     */
    public void insertHFData(DataTypeDoubleArray data) {
        try {
            datakitapi.insertHighFrequency(regDataSourceClient, data);
            printSample(data, subOutput);
        } catch (DataKitException ignored) {
            Log.e("hf data insert", ignored.getMessage());
            printMessage(ignored.getMessage(), output);
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int x) {}

    /**
     * Queries the database for data matching the given data source and parameters.
     *
     * This demo application only shows one query method. In this example, an arraylist of
     * <code>DataSourceClient</code>s matching the desired data source is produced using the
     * <code>DataKitAPI.find()</code> method. <code>DataKitAPI.find()</code> takes a
     * <code>DataSourceBuilder</code> object as a parameter. This <code>DataSourceBuilder</code> can
     * be configured for a particular data source or application. The query call is performed by passing
     * a <code>DataSourceClient</code> and an integer representing the "last n samples" that were collected.
     * Using this query method returns the last n rows matching the given data source in the database,
     * where n is the number of samples to return.
     * <p>
     * Other query methods allow queries for a given time window:
     * <p><code>DataKitAPI.query(DataSourceClient dataSourceClient, long starttimestamp, long endtimestamp)</code></p>
     * Or querying via primary key, where lastSyncedKey is the primary key and limit is the number of
     * rows to return:
     * <p><code>queryFromPrimaryKey(DataSourceClient dataSourceClient, long lastSyncedKey, int limit)</code></p>
     * </p>
     * All <code>DataKitAPI.query()</code> methods return an arraylist of <code>DataType</code> objects.
     * <code>DataKitAPI.queryFromPrimaryKey</code> returns an arraylist of <code>RowObject</code>s.
     *
     * <p>
     * Another useful method demonstrated here is <code>DataKitAPI.querySize()</code> which returns
     * the number of rows in the database as a <code>DataTypeLong</code> object.
     * </p>
     * @param view queButton
     */
    public void queryButton (View view){
        try {
            ArrayList<DataSourceClient> dataSourceClients = datakitapi.find(buildDataSource(buildApplication()));
            if (dataSourceClients.size() == 0) {
                printMessage(R.string.errorNotRegistered, output);
            } else {
                querySize = datakitapi.querySize();
                dataTypeQuery = datakitapi.query(dataSourceClients.get(0), 3);
                if (dataTypeQuery.size() == 0) {
                    printMessage("query size zero", output);
                } else
                    printQuery(dataTypeQuery);
            }
        } catch (DataKitException ignored) {
            Log.e("query", ignored.getMessage());
            dataTypeQuery = null;
            printMessage(ignored.getMessage(), output);
        }
    }

    /**
     * This is an example of how a query result might be printed.
     * @param query Query result
     */
    public void printQuery (ArrayList<DataType> query) {
        StringBuilder message = new StringBuilder();
        message.append("Query Size is ").append(querySize.getSample()).append("\n");
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

    /**
     * Prints a data sample. Used for displaying inserted data and data received when a data source
     * is subscribed.
     * @param data Data sample to print.
     * @param output TextView to print to.
     */
    public void printSample(DataTypeDoubleArray data, TextView output) {
        double[] sample = data.getSample();
        printMessage("[" + sample[0] + ", " + sample[1] + ", " + sample[2] + "]", output);
    }

    /**
     * Prints a message defined in the application resources.
     * @param message Message to print. Should be a resource defined in <code>R.strings</code>.
     * @param output TextView to print to.
     */
    public void printMessage (int message, TextView output) {
        output.setText(message);
    }

    /**
     * Prints the string passed to it.
     * @param message Message to print.
     * @param output TextView to print to.
     */
    public void printMessage (String message, TextView output) {
        output.setText(message);
    }
}