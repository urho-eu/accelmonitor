package eu.urho.accelmonitor;

import android.content.Context;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.widget.TableRow;

/**
 * Created by sopi on 5/13/16.
 */
public class AccRow extends TableRow {

  public AccRow(Context context) {
    super(context);
  }

  public AccRow(Context context, Integer rows, CharSequence valueTs, Integer valueX, Integer valueY, Integer valueZ) {
    super(context);

    int textColor, bgColor;

    Log.d("AccRow", "rows: " + rows.toString());

    this.setLayoutParams(new TableRow.LayoutParams(
        TableRow.LayoutParams.WRAP_CONTENT,
        TableRow.LayoutParams.WRAP_CONTENT));

    if (rows % 2 == 0) {
      textColor = ContextCompat.getColor(context, R.color.valueTextEvenRow);
      bgColor = ContextCompat.getColor(context, R.color.valueBgEvenRow);
    } else {
      textColor = ContextCompat.getColor(context, R.color.valueTextOddRow);
      bgColor = ContextCompat.getColor(context, R.color.valueBgOddRow);
    }

    Log.d("AccRow", "textColor: " + String.valueOf(textColor) + ", bgColor: " + String.valueOf(bgColor));

    AccRowValue ts = new AccRowValue(context, valueTs, textColor, bgColor, TEXT_ALIGNMENT_CENTER);
    AccRowValue x = new AccRowValue(context, valueX.toString(), textColor, bgColor, TEXT_ALIGNMENT_VIEW_START);
    AccRowValue y = new AccRowValue(context, valueY.toString(), textColor, bgColor, TEXT_ALIGNMENT_VIEW_START);
    AccRowValue z = new AccRowValue(context, valueZ.toString(), textColor, bgColor, TEXT_ALIGNMENT_VIEW_START);

    this.addView(ts);
    this.addView(x);
    this.addView(y);
    this.addView(z);
  }
}
