package eu.urho.accelmonitor;

import android.app.Application;

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
   * @param timestamp since EPOCH in ms
   * @return
   */
  public static String getFormattedDate(Long timestamp) {
    String format = "HH:mm:ss.SSS";
    return getFormattedDate(timestamp, format);
  }

  /**
   *
   * @param timestamp since EPOCH in ms
   * @param format
   * @return
   */
  public static String getFormattedDate(Long timestamp, String format) {
    if (format == "") {
      format = "HH:mm:ss.SSS";
    }
    final DateFormat DATE_FORMAT = new SimpleDateFormat(format);
    return DATE_FORMAT.format(new Date(timestamp)).toString();
  }
}
