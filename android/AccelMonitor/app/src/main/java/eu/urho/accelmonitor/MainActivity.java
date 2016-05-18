package eu.urho.accelmonitor;

import android.content.Context;
import android.graphics.Color;
import android.os.Handler;
import android.support.design.widget.TabLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;

import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewOverlay;
import android.widget.EditText;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.UUID;

import com.getpebble.android.kit.PebbleKit;
import com.getpebble.android.kit.util.PebbleDictionary;
import com.getpebble.android.kit.util.PebbleTuple;

public class MainActivity extends AppCompatActivity {

  private static final String TAG = MainActivity.class.getName();
  // Pebble watch app uuid
  final UUID appUuid = UUID.fromString("3d28cf77-f133-46c4-bbd4-6da608d8ae9d");
  // is Pebble connected?
  boolean isConnected = false;
  // buttons
  FloatingActionButton newData = null;
  FloatingActionButton saveData = null;

  // dictionary keys as per defined in the watchapp
  private final static int STATUS_KEY = 0;
  private final static int COMMAND_KEY = 1;
  private final static int MESSAGE_KEY = 2;
  private final static int BROADCAST_KEY = 3;
  // commands from watch to phone
  private final static int TOGGLE_MEASURING_FROM_WATCH = 41;

  // send command to watch
  protected WatchCommander watchCommander;

  // counter drawable overlay
  // fix show()
  //protected Counter counter = new Counter("Hello", Color.WHITE, Color.GRAY);

  /**
   * for commands between Pebble and the phone
   */
  private PebbleKit.PebbleDataReceiver dataReceiver;
  private Handler handler;

  /**
   * @link PebbleKit.PebbleDataLogReceiver to get accel data from Pebble
   */
  private PebbleKit.PebbleDataLogReceiver dataloggingReceiver = null;

  /**
   * The {@link ViewPager} that will host the section contents.
   */
  private ViewPager viewPager;

  // rows counter in the table
  // todo: could be replaced with dataList.getChildCount
  private int rows = 0;

  // CSV data store in memory
  List<String> currentData = new ArrayList<>();

  // CSV file
  EditText filename;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.main);

    // init buttons
    newData = (FloatingActionButton) findViewById(R.id.newData);
    saveData = (FloatingActionButton) findViewById(R.id.saveData);
    if (saveData != null) {
      saveData.setVisibility(View.INVISIBLE);
    }

    filename = (EditText) findViewById(R.id.filename);

    Context context = getApplicationContext();

    // check Pebble connection
    displayConnectionInfo(context);

    Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
    setSupportActionBar(toolbar);

    // Create the adapter that will return a fragment for each sections of the activity.
    String[] sections = getResources().getStringArray(R.array.sections);

    /*
    The {@link android.support.v4.view.PagerAdapter} that will provide
    fragments for each of the sections. We use a
    {@link FragmentPagerAdapter} derivative, which will keep every
    loaded fragment in memory. If this becomes too memory intensive, it
    may be best to switch to a
    {@link android.support.v4.app.FragmentStatePagerAdapter}.
    */
    SectionsPagerAdapter sectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager(), sections);

    // Set up the ViewPager with the sections adapter.
    ViewPager viewPager = (ViewPager) findViewById(R.id.container);
    viewPager.setAdapter(sectionsPagerAdapter);

    TabLayout tabLayout = (TabLayout) findViewById(R.id.tabs);
    tabLayout.setupWithViewPager(viewPager);

    if (watchCommander == null) {
      watchCommander = new WatchCommander(context, appUuid);
    }

    /**
     * Start a new measurement by clicking the left button
     */
    newData.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View view) {
        clearCurrentData();
        watchCommander.toggleMeasuring();
      }
     });

    /**
     * Start a new measurement by clicking the left button
     */
    saveData.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View view) {
        Log.d(TAG, "Save CSV now");
        watchCommander.shortPulse();
      }
    });
  }

  @Override
  protected void onResume() {
    super.onResume();
    startWatchApp(getApplicationContext());

    // fix it
    //counter.show(findViewById(R.id.main_content));

    if (dataReceiver == null) {
      final Handler handler = new Handler();

      if (watchCommander == null) {
        watchCommander = new WatchCommander(getApplicationContext(), appUuid);
      }

      dataReceiver = new PebbleKit.PebbleDataReceiver(appUuid) {
        @Override
        public void receiveData(final Context context, final int transactionId, final PebbleDictionary data) {
          final String logTag = "receiveData";

          handler.post(new Runnable() {
            @Override
            public void run() {
              // All data received from the Pebble must be ACK'd, otherwise you'll hit time-outs in the
              // watch-app which will cause the watch to feel "laggy" during periods of frequent
              // communication.
              PebbleKit.sendAckToPebble(context, transactionId);
              PebbleTuple tuple;

              for (int i = 0; i < data.size(); i++) {
                try {
                  tuple = data.iterator().next();
                }
                catch (NoSuchElementException e) {
                  // bail out
                  return;
                }

                int key = tuple.key;
                String value = tuple.value.toString();
                Log.d(logTag, i + ". tuple: " + key + " -> " + value);

                switch (key) {
                  case COMMAND_KEY:
                    int command = 0;
                    try {
                      command = Integer.parseInt(value);
                    }
                    catch (NumberFormatException e) {
                      Log.d(logTag, "invalid command:" + value);
                    }
                    switch (command) {
                      case TOGGLE_MEASURING_FROM_WATCH:
                        clearCurrentData();
                        break;
                      default:
                        Log.d(logTag, "unknown command:" + tuple.value.toString());
                    }
                    break;
                  case MESSAGE_KEY:
                    filename.setEnabled(false);
                    filename.setText(value);
                    break;
                  default:
                    Log.d(logTag, "unknown key" + tuple.key + " value: " + tuple.value.toString());
                }
              }
            }
          });
        }
      };
      PebbleKit.registerReceivedDataHandler(this, dataReceiver);
    }

    if (dataloggingReceiver == null) {
      // Define data reception behavior
      dataloggingReceiver = new PebbleKit.PebbleDataLogReceiver(appUuid) {
        final String logTag = "dataloggingReceiver";

        @Override
        public void receiveData(Context context, UUID appUuid, Long timestamp, Long tag, byte[] data) {
          // drop malformed data
          if (data.length % 15 != 0 || data.length < 15) {
            return;
          }

          if (rows == 0) {
            clearCurrentData();
          }
          TableLayout dataList = (TableLayout) findViewById(R.id.table);

          Log.d(logTag, "Data received: " + AccelMonitorUtils.getFormattedDate(timestamp) + ", tag: " + tag);

          for (AccelData reading : AccelData.fromDataArray(data)) {
            rows++;
            currentData.add(reading.toCsv(','));
            AccRow row = new AccRow(context, rows, reading);
            if (dataList != null) {
              dataList.addView(row);
            }
          }
        }

        @Override
        public void onFinishSession(Context context, UUID appUuid, Long timestamp, Long tag) {
          super.onFinishSession(context, appUuid, timestamp, tag);
          Log.d(logTag, "Log session finished: " + AccelMonitorUtils.getFormattedDate(timestamp) + ", tag: " + tag + ", rows: " + rows);

          filename.setText(getString(R.string.csv_prefix) + "_" + timestamp + "." + getString(R.string.csv_extension));
          filename.setFocusableInTouchMode(true);
          filename.setActivated(true);
          filename.setEnabled(true);
          filename.requestFocus();

          saveData.setVisibility(View.VISIBLE);
          rows = 0;
        }
      };

      PebbleKit.registerDataLogReceiver(this, dataloggingReceiver);
      PebbleKit.requestDataLogsForApp(this, appUuid);
    }
  }

  /**
   * Clears the current data from the table
   */
  private void clearCurrentData() {
    rows = 0;
    TableLayout dataList = (TableLayout) findViewById(R.id.table);

    // fix show() first
    // counter.hide();

    if (dataList != null && dataList.getChildCount() > 0) {
      TableRow head = (TableRow) dataList.getChildAt(0);
      dataList.removeAllViews();
      dataList.addView(head);
    }

    currentData.clear();

    filename.setText(getString(R.string.waiting));
    filename.setFocusableInTouchMode(false);
    filename.setActivated(false);
    filename.setEnabled(false);
    filename.clearFocus();

    saveData.setVisibility(View.INVISIBLE);
  }

  /**
   * Save the current measurements into a new CSV file
   * @param name
   * @return
   */
  public boolean saveCurrentData(String name) {
    if (currentData.size() == 0) return false;

    String header = "";
    char separator = getString(R.string.csv_separator).charAt(0);

    TableLayout dataList = (TableLayout) findViewById(R.id.table);

    if (dataList != null && dataList.getChildCount() > 0) {
      // create and open file for writing
      // todo
      // determine the 1st line of the CSV file based on the table's head
      TableRow head = (TableRow) dataList.getChildAt(0);
      for (int i = 0; i < head.getChildCount(); i++) {
        AccRowValue value = (AccRowValue) head.getChildAt(i);
        header += value.getText();
        if (i < head.getChildCount()) {
          header += separator;
        }
      }

      // iterate through the saved data
      for (int i = 0; i < currentData.size(); i++) {
        Log.d("sessionFinnish", currentData.get(i));
      }

    }
    return true;
  }

  @Override
  protected void onPause() {
    super.onPause();
    stopWatchApp(getApplicationContext());
    // Always deregister any Activity-scoped BroadcastReceivers when the Activity is paused
    if (dataloggingReceiver != null) {
      unregisterReceiver(dataloggingReceiver);
      dataloggingReceiver = null;
    }
    if (dataReceiver != null) {
      unregisterReceiver(dataReceiver);
      dataReceiver = null;
    }

  }

  /**
   * Displays a toast about Pebble connection
   * @param context
   */
  public void displayConnectionInfo(Context context) {
    isConnected = PebbleKit.isWatchConnected(context);
    if (isConnected) {
      Toast.makeText(context, getString(R.string.connected), Toast.LENGTH_LONG).show();
    } else {
      Toast.makeText(context, getString(R.string.disconnected), Toast.LENGTH_LONG).show();
    }
  }

  /**
   * Start the watch app on Pebble
   * @param context
   */
  public void startWatchApp(Context context) {
    isConnected = PebbleKit.isWatchConnected(context);
    if (! isConnected) return;

    PebbleKit.startAppOnPebble(context, appUuid);
  }

  /**
   * Close the watch app on Pebble
   * @param context
   */
  public void stopWatchApp(Context context) {
    isConnected = PebbleKit.isWatchConnected(context);
    if (! isConnected) return;

    PebbleKit.closeAppOnPebble(context, appUuid);
  }

  @Override
  public boolean onCreateOptionsMenu(Menu menu) {
    // Inflate the menu; this adds items to the action bar if it is present.
    getMenuInflater().inflate(R.menu.menu_main, menu);
    return true;
  }

  @Override
  public boolean onOptionsItemSelected(MenuItem item) {
    // Handle action bar item clicks here. The action bar will
    // automatically handle clicks on the Home/Up button, so long
    // as you specify a parent activity in AndroidManifest.xml.
    int id = item.getItemId();

    //noinspection SimplifiableIfStatement
    if (id == R.id.action_settings) {
      return true;
    }

    return super.onOptionsItemSelected(item);
  }
}
