package eu.urho.accelmonitor;

import android.content.Context;

import com.getpebble.android.kit.PebbleKit;
import com.getpebble.android.kit.util.PebbleDictionary;

import java.util.UUID;

/**
 * Created by sopi on 5/17/16.
 */
public class WatchCommander {

  private final Context context;
  private final UUID appUuid;

  // command key and all the commands (same as in the watch app)
  private final static int COMMAND_KEY = 01;
  private final static int TOGGLE_MEASURING_FROM_PHONE = 52;
  private final static int SHORT_PULSE = 61;

  public WatchCommander(Context context, UUID appUuid) {
    this.context = context;
    this.appUuid = appUuid;
  }

  /**
   * Fires up a short pulse on the watch
   */
  public void shortPulse() {
    PebbleDictionary data = new PebbleDictionary();
    data.addUint8(COMMAND_KEY, (byte) SHORT_PULSE);
    PebbleKit.sendDataToPebble(context, appUuid, data);
  }

  /**
   * Starts / stops measuring from the phone app
   */
  public void toggleMeasuring() {
    PebbleDictionary data = new PebbleDictionary();
    data.addUint8(COMMAND_KEY, (byte) TOGGLE_MEASURING_FROM_PHONE);
    PebbleKit.sendDataToPebble(context, appUuid, data);
  }
}
