package com.mcerebrumdemoapp;

import com.facebook.react.bridge.Promise;
import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReactContextBaseJavaModule;
import com.facebook.react.bridge.ReactMethod;

public class HelloWorldModule extends ReactContextBaseJavaModule {
  public HelloWorldModule(ReactApplicationContext reactContext) {
    super(reactContext); // Required by React native
  }

  @Override
  public String getName() {
    return "HelloWorld"; // Defines how this module will be reffered to from react native
  }

  @ReactMethod
  public void HelloWorld(Promise promise) { // This method is called from JS
    promise.resolve("Hello World!");
  }
}
