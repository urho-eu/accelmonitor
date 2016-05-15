package eu.urho.accelmonitor;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.widget.TextView;

/**
 * Created by sopi on 5/13/16.
 */
public class AccRowValue extends TextView {

  public AccRowValue(Context context)
  {
    super(context);
  }

  public AccRowValue(Context context, CharSequence text, int textColor, int bgColor, int alignment) {
    super(context);

    this.setText(text);
    this.setTextColor(textColor);
    this.setBackgroundColor(bgColor);

    if (alignment == 0) alignment = TEXT_ALIGNMENT_CENTER;
    this.setTextAlignment(alignment);

    int p = getResources().getDimensionPixelSize(R.dimen.value_pad);
    this.setPadding(p, p, p, p);
  }
}
