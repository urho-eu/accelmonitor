package eu.urho.accelmonitor;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by sopi on 5/13/16.
 */
public class AccelMonitorUtils {
  /**
   * Array of bytes -> an integer
   * @param bytes Array of bytes (in big endian)
   */
  public static long bytesToInt(byte[] bytes) {
    long val = bytes[0];
    for (int i = 1; i < bytes.length; i++) {
      val <<= 8;
      val |= bytes[i] & 0x000000FF;
    }
    return val;
  }

  /**
   *
   * @param timestamp
   * @return
   */
  public static String getLongAsTimestamp(Long timestamp) {
    final DateFormat DATE_FORMAT = new SimpleDateFormat("HH:mm:ss");
    return DATE_FORMAT.format(new Date(timestamp.longValue() * 1000L)).toString();
  }
}
