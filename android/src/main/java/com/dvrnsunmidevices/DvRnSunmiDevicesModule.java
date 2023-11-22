package com.dvrnsunmidevices;

import static com.dvrnsunmidevices.managers.HardwareManager.bytesToHex;

import android.app.Activity;
import android.app.PendingIntent;
import android.content.Intent;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.nfc.tech.MifareUltralight;

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
import com.facebook.react.bridge.BaseActivityEventListener;
import com.facebook.react.module.annotations.ReactModule;
import com.facebook.react.modules.core.DeviceEventManagerModule;
import android.os.Build;

import java.io.IOException;

@ReactModule(name = DvRnSunmiDevicesModule.NAME)
public class DvRnSunmiDevicesModule extends ReactContextBaseJavaModule {
  public static final String NAME = "DvRnSunmiDevices";
  private final String CHIP_EVENT = "CHIP_LOADED";
  private final ReactApplicationContext reactContext;

  private final BridgeManager bridgeManager;
  private NfcAdapter mNfcAdapter;
  private Tag tag;

  public DvRnSunmiDevicesModule(ReactApplicationContext reactContext) {
    super(reactContext);
    this.bridgeManager = new BridgeManager(reactContext);
    this.reactContext = reactContext;
    getReactApplicationContext().addLifecycleEventListener(lifecycleEventListener);
    getReactApplicationContext().addActivityEventListener(mActivityEventListener);
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

  /**
   * @param promise
   */
  @ReactMethod
  public void getPrinterStatus(Promise promise) {
    bridgeManager.updatePrinterState(promise);
  }

  private void sendEvent(ReactContext reactContext,
                         String eventName,
                         @Nullable WritableMap params) {
    reactContext
      .getJSModule(DeviceEventManagerModule.RCTDeviceEventEmitter.class)
      .emit(eventName, params);
  }

  LifecycleEventListener lifecycleEventListener = new LifecycleEventListener() {
    @Override
    public void onHostResume() {
      if (mNfcAdapter != null) {
        setupForegroundDispatch(getCurrentActivity(), mNfcAdapter);
      } else {
        mNfcAdapter = NfcAdapter.getDefaultAdapter(reactContext);
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

    }
  };

  private final ActivityEventListener mActivityEventListener = new BaseActivityEventListener() {
    @Override
    public void onNewIntent(Intent intent) {
      try {
        tag = intent.getParcelableExtra(NfcAdapter.EXTRA_TAG);

        WritableMap credentials = Arguments.createMap();
        if (tag != null) {
          if (MifareUltralight.get(tag) != null)
            credentials = HardwareManager.getInstance().readTagData(tag);
          credentials.putString("nfcId", bytesToHex(tag.getId()));
        }
        sendEvent(reactContext, CHIP_EVENT, credentials);

      } catch (IOException e) {
        e.printStackTrace();
        try {
          MifareUltralight uTag = MifareUltralight.get(tag);
          if (uTag != null)
            uTag.close();
        } catch (IOException err) {
          err.printStackTrace();
        }
        sendEvent(reactContext, CHIP_EVENT, null);
      }
    }
  };


  public static void setupForegroundDispatch(final Activity activity, NfcAdapter adapter) {
    final Intent intent = new Intent(activity.getApplicationContext(), activity.getClass());
    intent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);

    final PendingIntent pendingIntent;
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
        pendingIntent = PendingIntent.getActivity(activity.getApplicationContext(),
                0, intent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

    }else {
        pendingIntent = PendingIntent.getActivity(activity.getApplicationContext(),
                0, intent, PendingIntent.FLAG_UPDATE_CURRENT);

    }
    if (adapter != null && adapter.isEnabled()) {
      adapter.enableForegroundDispatch(activity, pendingIntent, null, null);
    }
  }

  public static void stopForegroundDispatch(final Activity activity, NfcAdapter adapter) {
    adapter.disableForegroundDispatch(activity);
  }
}
