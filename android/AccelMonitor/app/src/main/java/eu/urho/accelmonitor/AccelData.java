package eu.urho.accelmonitor;

import android.util.Log;
import org.json.JSONObject;
import org.json.JSONException;

import java.util.List;
import java.util.Arrays;
import java.util.ArrayList;
import java.util.TimeZone;

/**
 * AccelData was originally written by Mark Whitney
 * https://github.com/kramimus/pebble-accel-analyzer
 */
public class AccelData {
  private static final String TAG = AccelData.class.getSimpleName();

  final private int x;
  final private int y;
  final private int z;

  private long timestamp = 0;
  final private boolean didVibrate;

  public AccelData(byte[] data) {
    /**
     * From the original Pebble AccelData struct definition
     *
     * int16_t x;
     * int16_t y;
     * int16_t z;
     * bool did_vibrate;
     * uint64_t timestamp;
     */
    x = (data[0] & 0xff) | (data[1] << 8);
    y = (data[2] & 0xff) | (data[3] << 8);
    z = (data[4] & 0xff) | (data[5] << 8);
    didVibrate = data[6] != 0;

    for (int i = 0; i < 8; i++) {
      timestamp |= ((long)(data[i+7] & 0xff)) << (i * 8);
    }
  }

  /**
   * Convert the reading to JSON
   * @return JSONObject
   */
  public JSONObject toJson() {
    JSONObject json = new JSONObject();
    try {
      json.put("x", x);
      json.put("y", y);
      json.put("z", z);
      json.put("ts", timestamp);
      json.put("v", didVibrate);
      return json;
    } catch (JSONException e) {
      Log.w(TAG, "problem constructing accel data, skipping " + e);
    }
    return null;
  }

  /**
   * Convert the reading to JSON
   * @param separator the separator character
   * @return JSONObject
   */
  public String toCsv(char separator) {
    if (separator == ' ') separator = ',';
    return (String.valueOf(timestamp) + separator + x + separator + y + separator + z);
  }

  /**
   * Serialize readings into a List
   * @param data the byte array to be converted into List item
   * @return List
   */
  public static List<AccelData> fromDataArray(byte[] data) {
    List<AccelData> accels = new ArrayList<>();
    for (int i = 0; i < data.length; i += 15) {
      accels.add(new AccelData(Arrays.copyOfRange(data, i, i + 15)));
    }
    return accels;
  }

  /**
   * Returns the timestamp of the reading
   * @return long timestamp
   */
  public long getTimestamp() {
    return timestamp;
  }

  /**
   * Adjust timestamp value with the time zone
   * @param tz
   */
  public void applyTimezone(TimeZone tz) {
    timestamp -= tz.getOffset(timestamp);
  }

  /**
   * Returns the X value of the reading
   * @return int X acceleration value
   */
  public int getX() {
    return x;
  }

  /**
   * Returns the Y value of the reading
   * @return int Y acceleration value
   */
  public int getY() {
    return y;
  }

  /**
   * Returns the Z value of the reading
   * @return int Z acceleration value
   */
  public int getZ() {
    return z;
  }

  /**
   * Returns true if the watch vibrated during the reading
   * @return boolean
   */
  public boolean getVibration() {
    return didVibrate;
  }
}