package com.dvrnsunmidevices;

import android.app.Activity;
import android.app.PendingIntent;
import android.content.Intent;
import android.nfc.NdefMessage;
import android.nfc.NdefRecord;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.nfc.tech.Ndef;
import android.nfc.tech.NdefFormatable;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.dvrnsunmidevices.managers.BridgeManager;
import com.dvrnsunmidevices.managers.HardwareManager;
import com.facebook.react.bridge.ActivityEventListener;
import com.facebook.react.bridge.Arguments;
import com.facebook.react.bridge.LifecycleEventListener;
import com.facebook.react.bridge.Promise;
import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReactContext;
import com.facebook.react.bridge.ReactContextBaseJavaModule;
import com.facebook.react.bridge.ReactMethod;
import com.facebook.react.bridge.ReadableMap;
import com.facebook.react.bridge.WritableMap;
import com.facebook.react.module.annotations.ReactModule;
import com.facebook.react.modules.core.DeviceEventManagerModule;

import java.io.IOException;

@ReactModule(name = DvRnSunmiDevicesModule.NAME)
public class DvRnSunmiDevicesModule extends ReactContextBaseJavaModule implements ActivityEventListener, LifecycleEventListener {
  private final ReactApplicationContext reactContext;
  public static final String NAME = "DvRnSunmiDevices";

  private final BridgeManager bridgeManager;

  private NfcAdapter mNfcAdapter;
  private Tag tag;
  private final String CHIP_EVENT = "CHIP_LOADED";

  public DvRnSunmiDevicesModule(ReactApplicationContext reactContext) {
    super(reactContext);
    this.bridgeManager = new BridgeManager(reactContext);
    this.reactContext = reactContext;
    this.reactContext.addActivityEventListener(this);
    this.reactContext.addLifecycleEventListener(this);

  }

  @Override
  @NonNull
  public String getName() {
    return NAME;
  }

  @ReactMethod
  public void printCustomHTMl(String htmlToConvert, Promise promise) {
    bridgeManager.printCustomHTMl(htmlToConvert, promise);
  }

  @ReactMethod
  public void showTwoLineText(String firstRow, String secondRow, Promise promise) {
    bridgeManager.showTwoLineText(firstRow, secondRow, promise);
  }

  @ReactMethod
  public void writeNFCTag(ReadableMap data, Promise promise) {
    bridgeManager.writeNFCTag(data, tag, promise);
  }


  @Override
  public void onActivityResult(Activity activity, int requestCode, int resultCode, Intent data) {

  }

  @Override
  public void onNewIntent(Intent intent) {
    try {
      tag = intent.getParcelableExtra(NfcAdapter.EXTRA_TAG);
      if (tag != null) {

        WritableMap credentials = HardwareManager.getInstance().readTagData(tag);
        System.out.println("Output from Tag: " + credentials);
        sendEvent(this.reactContext, CHIP_EVENT, credentials);
      }
    } catch (IOException e) {
      e.printStackTrace();
      sendEvent(this.reactContext, CHIP_EVENT, null);
    }
  }

  private void sendEvent(ReactContext reactContext,
                         String eventName,
                         @Nullable WritableMap params) {
    reactContext
      .getJSModule(DeviceEventManagerModule.RCTDeviceEventEmitter.class)
      .emit(eventName, params);
  }


  @Override
  public void onHostResume() {
    if (mNfcAdapter != null) {
      setupForegroundDispatch(getCurrentActivity(), mNfcAdapter);
    } else {
      mNfcAdapter = NfcAdapter.getDefaultAdapter(this.reactContext);
      setupForegroundDispatch(getCurrentActivity(), mNfcAdapter);
    }
  }

  @Override
  public void onHostPause() {
    if (mNfcAdapter != null)
      stopForegroundDispatch(getCurrentActivity(), mNfcAdapter);
  }

  @Override
  public void onHostDestroy() {
    // Activity `onDestroy`
  }

  public static void setupForegroundDispatch(final Activity activity, NfcAdapter adapter) {
    final Intent intent = new Intent(activity.getApplicationContext(), activity.getClass());
    intent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);

    final PendingIntent pendingIntent = PendingIntent.getActivity(activity.getApplicationContext(), 0, intent, 0);
    if (adapter != null && adapter.isEnabled()) {
      adapter.enableForegroundDispatch(activity, pendingIntent, null, null);
    }
  }

  public static void stopForegroundDispatch(final Activity activity, NfcAdapter adapter) {
    adapter.disableForegroundDispatch(activity);
  }


}
