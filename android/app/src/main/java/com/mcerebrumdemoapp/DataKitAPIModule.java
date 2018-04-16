package com.mcerebrumdemoapp;

import com.facebook.react.bridge.Promise;
import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReactContextBaseJavaModule;
import com.facebook.react.bridge.ReactMethod;

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


public class DataKitAPIModule extends ReactContextBaseJavaModule {

    // Variables for DataKit objects
    private DataKitAPI datakitapi;
    private DataSourceClient regDataSourceClient = null;
    private DataSourceClient subDataSourceClient = null;
    private ArrayList<DataType> dataTypeQuery = null;
    private DataTypeDoubleArray dataTypeDoubleArray;
    private DataTypeLong querySize;
    private Boolean isHF;


  public DataKitAPIModule(ReactApplicationContext reactContext) {
    super(reactContext); // Required by React native
    datakitapi = datakitapi.getInstance(reactContext);
  }
  @Override
  public String getName() {
    return "DataKitAPI"; // Defines how this module will be reffered to from react native
  }

  OnConnectionListener connectListener = new OnConnectionListener() {
    @Override
    public void onConnected() {
      connectSuccess();
    }
  };

  @ReactMethod
  public void connectDataKit(Promise promise) { // This method is called from JS
    try {
      if (datakitapi.isConnected()) {
        disconnectDataKit();
      } else
        datakitapi.connect(connectListener);
    } catch (DataKitException ignored) {
      promise.resolve(ignored.getMessage());
    }
  }

  public void disconnectDataKit() {
    datakitapi.disconnect();
  }

  public void connectSuccess(Promise promise) {
    promise.resolve("Datakit Connected");
  }
}
