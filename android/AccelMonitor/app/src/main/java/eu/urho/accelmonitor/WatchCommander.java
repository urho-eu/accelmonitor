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

  // keys (same as in the watch app)
  private final static int STATUS_KEY = 0;
  private final static int COMMAND_KEY = 1;
  private final static int MESSAGE_KEY = 2;
  private final static int BROADCAST_KEY = 3;
  // commands (same as in the watch app)
  private final static int TOGGLE_MEASURING_FROM_PHONE = 52;
  private final static int SHORT_PULSE = 61;
  // statuses (same as in the watch app)
  private final static int DATA_SAVE_OK = 71;
  private final static int DATA_SAVE_FAILED = 72;

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

  /**
   * Starts / stops measuring from the phone app
   */
  public void sendSavingStatus(boolean ok) {
    PebbleDictionary data = new PebbleDictionary();
    if (ok) {
      data.addUint8(STATUS_KEY, (byte) DATA_SAVE_OK);
    } else {
      data.addUint8(STATUS_KEY, (byte) DATA_SAVE_FAILED);
    }
    PebbleKit.sendDataToPebble(context, appUuid, data);
  }

}
