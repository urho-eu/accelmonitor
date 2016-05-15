package eu.urho.accelmonitor;

import android.content.Context;
import android.os.Handler;
import android.support.design.widget.TabLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;

import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TableLayout;
import android.widget.Toast;

import java.util.UUID;

import com.getpebble.android.kit.PebbleKit;

public class MainActivity extends AppCompatActivity {

  private static final String TAG = "MainActivity";
  // Pebble watch app uuid
  final UUID appUuid = UUID.fromString("3d28cf77-f133-46c4-bbd4-6da608d8ae9d");
  // is Pebble connected?
  boolean isConnected = false;

  /**
   * @link PebbleKit.PebbleDataLogReceiver to get accel data from Pebble
   */
  private PebbleKit.PebbleDataLogReceiver dataloggingReceiver = null;

  /**
   * The {@link android.support.v4.view.PagerAdapter} that will provide
   * fragments for each of the sections. We use a
   * {@link FragmentPagerAdapter} derivative, which will keep every
   * loaded fragment in memory. If this becomes too memory intensive, it
   * may be best to switch to a
   * {@link android.support.v4.app.FragmentStatePagerAdapter}.
   */
  private SectionsPagerAdapter sectionsPagerAdapter;

  /**
   * The {@link ViewPager} that will host the section contents.
   */
  private ViewPager viewPager;

  // the rows in the readings table
  private int rows = 0;

  // reading details
  CharSequence ts = "";
  int x = 0;
  int y = 0;
  int z = 0;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.main);

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
    sectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager(), sections);

    // Set up the ViewPager with the sections adapter.
    viewPager = (ViewPager) findViewById(R.id.container);
    viewPager.setAdapter(sectionsPagerAdapter);

    TabLayout tabLayout = (TabLayout) findViewById(R.id.tabs);
    tabLayout.setupWithViewPager(viewPager);

    /**
     * Start a new measurement by clicking the left button
     */
    FloatingActionButton newReading = (FloatingActionButton) findViewById(R.id.newReading);
    newReading.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View view) {
        Snackbar.make(view, "Start a new measurement", Snackbar.LENGTH_LONG)
            .setAction("Action", null).show();
      }
    });
  }

  @Override
  protected void onResume() {
    super.onResume();

    final TableLayout dataList = (TableLayout) findViewById(R.id.table);
    final Handler handler = new Handler();

    if (dataloggingReceiver == null) {
      // Define data reception behavior
      dataloggingReceiver = new PebbleKit.PebbleDataLogReceiver(appUuid) {
        @Override
        public void receiveData(Context context, UUID appUuid, Long timestamp, Long tag, byte[] data) {

          ts = AccelMonitorUtils.getLongAsTimestamp(timestamp);

          Log.d("MainActivity", "Data received: " + ts);

          data[0] = (byte) (data[0] << 25 >> 25);
          x = (int) AccelMonitorUtils.bytesToInt(new byte[]{data[0], data[1]});
          y = (int) AccelMonitorUtils.bytesToInt(new byte[]{data[2], data[3]});
          z = (int) AccelMonitorUtils.bytesToInt(new byte[]{data[4], data[5]});

          handler.post(new Runnable() {
            final Context context = getApplicationContext();
            @Override
            public void run() {
              rows++;
              AccRow row = new AccRow(context, rows, ts, x, y, z);
              if (row != null && dataList != null) {
                dataList.addView(row);
              }
            }
          });
        }

        @Override
        public void onFinishSession(Context context, UUID appUuid, Long timestamp, Long tag) {
          super.onFinishSession(context, appUuid, timestamp, tag);
        }
      };

      PebbleKit.registerDataLogReceiver(this, dataloggingReceiver);
      PebbleKit.requestDataLogsForApp(this, appUuid);
    }
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
