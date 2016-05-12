package eu.urho.accelmonitor;

import android.content.Context;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Toast;
import java.util.UUID;

import com.getpebble.android.kit.PebbleKit;

public class MainActivity extends AppCompatActivity {

  final UUID appUuid = UUID.fromString("3d28cf77-f133-46c4-bbd4-6da608d8ae9d");

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);

    Context context = getApplicationContext();

    boolean isConnected = PebbleKit.isWatchConnected(context);

    if (isConnected) {
      PebbleKit.startAppOnPebble(context, appUuid);
    } else {
      Toast.makeText(context, "Pebble is not connected!", Toast.LENGTH_LONG).show();
    }

    FloatingActionButton check = (FloatingActionButton) findViewById(R.id.check);
    assert check != null;
    check.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View view) {
      }
    });
  }
}
