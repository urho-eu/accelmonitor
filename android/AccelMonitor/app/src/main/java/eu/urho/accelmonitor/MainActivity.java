package eu.urho.accelmonitor;

import android.content.Context;
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
import android.widget.EditText;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import com.getpebble.android.kit.PebbleKit;

public class MainActivity extends AppCompatActivity {

  private static final String TAG = MainActivity.class.getName();
  // Pebble watch app uuid
  final UUID appUuid = UUID.fromString("3d28cf77-f133-46c4-bbd4-6da608d8ae9d");
  // is Pebble connected?
  boolean isConnected = false;
  // buttons
  FloatingActionButton newData = null;
  FloatingActionButton saveData = null;

  /**
   * @link PebbleKit.PebbleDataLogReceiver to get accel data from Pebble
   */
  private PebbleKit.PebbleDataLogReceiver dataloggingReceiver = null;

  /**
   * The {@link ViewPager} that will host the section contents.
   */
  private ViewPager viewPager;

  // the rows in the readings table
  private int rows = 0;

  // reading details
  CharSequence ts = "";

  // local data storage
  List<String> currentData = new ArrayList<>();

  //
  EditText filename;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.main);

    // init buttons
    newData = (FloatingActionButton) findViewById(R.id.newData);
    saveData = (FloatingActionButton) findViewById(R.id.saveData);
    saveData.setVisibility(View.INVISIBLE);

    filename = (EditText) findViewById(R.id.filename);

    Context context = getApplicationContext();

    // check Pebble connection
    isConnected = PebbleKit.isWatchConnected(context);
    if (isConnected) {
      PebbleKit.startAppOnPebble(context, appUuid);
      Toast.makeText(context, getString(R.string.connected), Toast.LENGTH_LONG).show();
    } else {
      Toast.makeText(context, getString(R.string.disconnected), Toast.LENGTH_LONG).show();
    }

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

    /**
     * Start a new measurement by clicking the left button
     */
    newData.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View view) {
        clearCurrentData();
      }
     });

    /**
     * Start a new measurement by clicking the left button
     */
    saveData.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View view) {
        Log.d(TAG, "Save CSV now");
      }
    });
  }

  @Override
  protected void onResume() {
    super.onResume();

    if (dataloggingReceiver == null) {
      // Define data reception behavior
      dataloggingReceiver = new PebbleKit.PebbleDataLogReceiver(appUuid) {

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

          ts = AccelMonitorUtils.getFormattedDate(timestamp);
          Log.d("MainActivity", "Data received: " + ts + ", tag: " + tag);

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
          Log.d("MainActivity", "Log session finished: " + AccelMonitorUtils.getFormattedDate(timestamp) + ", tag: " + tag + ", rows: " + rows);

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
    if (dataloggingReceiver != null) {
      unregisterReceiver(dataloggingReceiver);
      dataloggingReceiver = null;
    }
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
